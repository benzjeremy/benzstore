package com.benzjeremy.benzstore;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.benzjeremy.benzstore.model.AppModel;
import com.benzjeremy.benzstore.model.VersionModel;
import com.benzjeremy.benzstore.util.BenzFileProvider;
import com.benzjeremy.benzstore.util.HashUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class AppDetailActivity extends Activity implements View.OnClickListener, AdapterView.OnItemSelectedListener {
    public static AppModel currentApp;

    private ImageView detailIcon;
    private TextView detailName, detailAuthor, detailPlatforms, detailDescription;
    private Spinner spinnerVersions;
    private TextView selectedVersionChangelog, txtFileSize, txtSha256, txtInstallStatus;
    private ProgressBar installProgress;
    private Button btnInstallVersion, btnBack;

    private VersionModel selectedVersion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        if (currentApp == null) {
            finish();
            return;
        }

        detailIcon = findViewById(R.id.detailIcon);
        detailName = findViewById(R.id.detailName);
        detailAuthor = findViewById(R.id.detailAuthor);
        detailPlatforms = findViewById(R.id.detailPlatforms);
        detailDescription = findViewById(R.id.detailDescription);
        spinnerVersions = findViewById(R.id.spinnerVersions);
        selectedVersionChangelog = findViewById(R.id.selectedVersionChangelog);
        txtFileSize = findViewById(R.id.txtFileSize);
        txtSha256 = findViewById(R.id.txtSha256);
        txtInstallStatus = findViewById(R.id.txtInstallStatus);
        installProgress = findViewById(R.id.installProgress);
        btnInstallVersion = findViewById(R.id.btnInstallVersion);
        btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(this);
        btnInstallVersion.setOnClickListener(this);
        spinnerVersions.setOnItemSelectedListener(this);

        populateUI();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btnBack) {
            finish();
        } else if (id == R.id.btnInstallVersion) {
            if (selectedVersion != null) {
                startInstallationWorkflow();
            }
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (currentApp != null && position >= 0 && position < currentApp.getVersions().size()) {
            selectedVersion = currentApp.getVersions().get(position);
            updateVersionDisplay();
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {}

    private void populateUI() {
        detailName.setText(currentApp.getName());
        detailAuthor.setText(currentApp.getAuthor() + " • Lizenz: " + currentApp.getLicense());
        detailDescription.setText(currentApp.getDescription());

        StringBuilder platStr = new StringBuilder("Plattformen: ");
        for (int i = 0; i < currentApp.getPlatforms().size(); i++) {
            platStr.append(currentApp.getPlatforms().get(i).toUpperCase());
            if (i < currentApp.getPlatforms().size() - 1) platStr.append(", ");
        }
        detailPlatforms.setText(platStr.toString());

        loadIconAsync(currentApp.getIconUrl(), detailIcon);

        if (!currentApp.getVersions().isEmpty()) {
            ArrayAdapter<VersionModel> adapter = new ArrayAdapter<VersionModel>(
                    this, android.R.layout.simple_spinner_dropdown_item, currentApp.getVersions());
            spinnerVersions.setAdapter(adapter);

            selectedVersion = currentApp.getLatestVersionModel();
            updateVersionDisplay();
        } else {
            selectedVersionChangelog.setText("Keine Versionsinformationen verfügbar.");
            btnInstallVersion.setEnabled(false);
        }
    }

    private void updateVersionDisplay() {
        if (selectedVersion == null) return;

        boolean isLatest = selectedVersion.getVersion().equalsIgnoreCase(currentApp.getLatestVersion());
        String badge = isLatest ? " [LATEST]" : "";

        selectedVersionChangelog.setText("Änderungen in v" + selectedVersion.getVersion() + badge + ":\n" + selectedVersion.getChangelog());
        txtFileSize.setText("Download-Größe: " + selectedVersion.getFormattedSize());

        if (selectedVersion.getSha256() != null && !selectedVersion.getSha256().isEmpty()) {
            txtSha256.setText("SHA-256: " + selectedVersion.getSha256());
        } else {
            txtSha256.setText("SHA-256: Manifest wird verifiziert");
        }

        if (currentApp.supportsAndroid()) {
            btnInstallVersion.setText("v" + selectedVersion.getVersion() + " installieren" + (isLatest ? " (Latest)" : ""));
            btnInstallVersion.setEnabled(true);
        } else {
            btnInstallVersion.setText("Nur für PC / Desktop verfügbar");
            btnInstallVersion.setEnabled(false);
        }
    }

    private void startInstallationWorkflow() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!getPackageManager().canRequestPackageInstalls()) {
                showPermissionDialog();
                return;
            }
        }
        executeDownloadAndVerify();
    }

    private static class PermissionDialogListener implements DialogInterface.OnClickListener {
        private final Activity activity;
        public PermissionDialogListener(Activity activity) { this.activity = activity; }
        @Override
        public void onClick(DialogInterface dialog, int which) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES);
            intent.setData(Uri.parse("package:" + activity.getPackageName()));
            activity.startActivity(intent);
        }
    }

    private void showPermissionDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Berechtigung erforderlich")
                .setMessage("Damit BenzStore Apps installieren kann, muss die Berechtigung 'Unbekannte Apps installieren' in den Android-Einstellungen aktiviert werden.")
                .setPositiveButton("Zu den Einstellungen", new PermissionDialogListener(this))
                .setNegativeButton("Abbrechen", null)
                .show();
    }

    public static class DownloadTask implements Runnable {
        private final AppDetailActivity activity;
        private final VersionModel targetVer;

        public DownloadTask(AppDetailActivity activity, VersionModel targetVer) {
            this.activity = activity;
            this.targetVer = targetVer;
        }

        @Override
        public void run() {
            File apkFile = null;
            boolean downloadOk = false;
            String errorMsg = "";

            try {
                URL url = new URL(targetVer.getDownloadUrl());
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(20000);
                conn.setRequestProperty("User-Agent", "BenzStore-Android/1.0");

                if (conn.getResponseCode() == 200) {
                    File cacheDir = activity.getCacheDir();
                    apkFile = new File(cacheDir, targetVer.getFilename());
                    if (apkFile.exists()) {
                        apkFile.delete();
                    }

                    try (InputStream in = conn.getInputStream();
                         FileOutputStream out = new FileOutputStream(apkFile)) {
                        byte[] buffer = new byte[8192];
                        int len;
                        while ((len = in.read(buffer)) != -1) {
                            out.write(buffer, 0, len);
                        }
                    }
                    downloadOk = true;
                } else {
                    errorMsg = "Server meldet HTTP " + conn.getResponseCode();
                }
            } catch (Exception e) {
                errorMsg = e.getMessage();
            }

            activity.runOnUiThread(new DownloadResultHandler(activity, downloadOk, apkFile, errorMsg, targetVer));
        }
    }

    public static class DownloadResultHandler implements Runnable {
        private final AppDetailActivity activity;
        private final boolean success;
        private final File apkFile;
        private final String errorMsg;
        private final VersionModel targetVer;

        public DownloadResultHandler(AppDetailActivity activity, boolean success, File apkFile, String errorMsg, VersionModel targetVer) {
            this.activity = activity;
            this.success = success;
            this.apkFile = apkFile;
            this.errorMsg = errorMsg;
            this.targetVer = targetVer;
        }

        @Override
        public void run() {
            if (!success || apkFile == null || !apkFile.exists()) {
                activity.installProgress.setVisibility(View.GONE);
                activity.txtInstallStatus.setText("Download fehlgeschlagen: " + errorMsg);
                activity.btnInstallVersion.setEnabled(true);
                return;
            }

            activity.txtInstallStatus.setText("Verifiziere SHA-256 Prüfsumme...");
            String computedSha256 = HashUtil.calculateSha256(apkFile);

            if (targetVer.getSha256() != null && !targetVer.getSha256().isEmpty()
                    && !targetVer.getSha256().startsWith("PENDING")) {
                if (!computedSha256.equalsIgnoreCase(targetVer.getSha256())) {
                    apkFile.delete();
                    activity.installProgress.setVisibility(View.GONE);
                    activity.txtInstallStatus.setText("SICHERHEITSWARNUNG: Prüfsummen-Abweichung!");
                    activity.btnInstallVersion.setEnabled(true);

                    new AlertDialog.Builder(activity)
                            .setTitle("Sicherheitsfehler")
                            .setMessage("Die heruntergeladene Datei stimmt kryptografisch NICHT mit der Signatur im Store überein!\n\nErwartet:\n" + targetVer.getSha256() + "\n\nBerechnet:\n" + computedSha256 + "\n\nDie Installation wurde zum Schutz deines Geräts abgebrochen.")
                            .setPositiveButton("OK", null)
                            .show();
                    return;
                }
            }

            activity.installProgress.setVisibility(View.GONE);
            activity.txtInstallStatus.setText("SHA-256 bestätigt. Starte Android-Installation...");
            activity.btnInstallVersion.setEnabled(true);

            activity.launchPackageInstaller(apkFile);
        }
    }

    private void executeDownloadAndVerify() {
        btnInstallVersion.setEnabled(false);
        installProgress.setVisibility(View.VISIBLE);
        txtInstallStatus.setVisibility(View.VISIBLE);
        txtInstallStatus.setText("Lade " + selectedVersion.getFilename() + " herunter...");

        new Thread(new DownloadTask(this, selectedVersion)).start();
    }

    public void launchPackageInstaller(File apkFile) {
        try {
            Uri contentUri = Uri.parse("content://" + BenzFileProvider.AUTHORITY + "/" + apkFile.getName());
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(contentUri, "application/vnd.android.package-archive");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Fehler beim Starten des Installers: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    public static class IconLoader implements Runnable {
        private final AppDetailActivity activity;
        private final String iconUrl;
        private final ImageView imageView;

        public IconLoader(AppDetailActivity activity, String iconUrl, ImageView imageView) {
            this.activity = activity;
            this.iconUrl = iconUrl;
            this.imageView = imageView;
        }

        @Override
        public void run() {
            try {
                URL url = new URL(iconUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(4000);
                conn.setReadTimeout(4000);
                if (conn.getResponseCode() == 200) {
                    try (InputStream in = conn.getInputStream()) {
                        final Bitmap bmp = BitmapFactory.decodeStream(in);
                        if (bmp != null) {
                            activity.runOnUiThread(new SetBitmapTask(imageView, bmp));
                        }
                    }
                }
            } catch (Exception ignored) {}
        }
    }

    public static class SetBitmapTask implements Runnable {
        private final ImageView view;
        private final Bitmap bmp;
        public SetBitmapTask(ImageView view, Bitmap bmp) { this.view = view; this.bmp = bmp; }
        @Override
        public void run() { view.setImageBitmap(bmp); }
    }

    private void loadIconAsync(String iconUrl, ImageView imageView) {
        if (iconUrl == null || iconUrl.isEmpty()) return;
        new Thread(new IconLoader(this, iconUrl, imageView)).start();
    }
}
