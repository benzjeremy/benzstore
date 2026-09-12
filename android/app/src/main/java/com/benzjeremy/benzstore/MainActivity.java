package com.benzjeremy.benzstore;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.benzjeremy.benzstore.model.AppModel;
import com.benzjeremy.benzstore.model.VersionModel;
import com.benzjeremy.benzstore.util.FeedLoader;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends Activity implements View.OnClickListener, TextWatcher, FeedLoader.FeedCallback {

    private EditText searchBar;
    private TextView statusText;
    private LinearLayout appListContainer;
    private Button btnRefresh;
    private Button catAndroid, catPC, catServer;

    private LinearLayout layoutUpdateBanner;
    private TextView txtUpdateBannerTitle, txtUpdateBannerSub;
    private Button btnBannerShowUpdates;

    private List<AppModel> allApps = new ArrayList<AppModel>();
    private String currentCategory = "android"; // Default category: Apps
    private String currentSearch = "";
    private boolean showOnlyUpdates = false;
    private final Map<String, Bitmap> iconCache = new HashMap<String, Bitmap>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        searchBar = findViewById(R.id.searchBar);
        statusText = findViewById(R.id.statusText);
        appListContainer = findViewById(R.id.appListContainer);
        btnRefresh = findViewById(R.id.btnRefresh);
        catAndroid = findViewById(R.id.catAndroid);
        catPC = findViewById(R.id.catPC);
        catServer = findViewById(R.id.catServer);

        layoutUpdateBanner = findViewById(R.id.layoutUpdateBanner);
        txtUpdateBannerTitle = findViewById(R.id.txtUpdateBannerTitle);
        txtUpdateBannerSub = findViewById(R.id.txtUpdateBannerSub);
        btnBannerShowUpdates = findViewById(R.id.btnBannerShowUpdates);

        btnRefresh.setOnClickListener(this);
        searchBar.addTextChangedListener(this);

        catAndroid.setOnClickListener(this);
        catPC.setOnClickListener(this);
        catServer.setOnClickListener(this);
        btnBannerShowUpdates.setOnClickListener(this);

        updateCategoryButtonStyles();
        loadCatalog();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!allApps.isEmpty()) {
            checkForUpdates();
            filterAndRender();
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btnRefresh) {
            loadCatalog();
        } else if (id == R.id.catAndroid) {
            currentCategory = "android";
            showOnlyUpdates = false;
            updateCategoryButtonStyles();
            filterAndRender();
        } else if (id == R.id.catPC) {
            currentCategory = "pc";
            showOnlyUpdates = false;
            updateCategoryButtonStyles();
            filterAndRender();
        } else if (id == R.id.catServer) {
            currentCategory = "server";
            showOnlyUpdates = false;
            updateCategoryButtonStyles();
            filterAndRender();
        } else if (id == R.id.btnBannerShowUpdates) {
            showOnlyUpdates = !showOnlyUpdates;
            btnBannerShowUpdates.setText(showOnlyUpdates ? "Alle anzeigen" : "Anzeigen");
            filterAndRender();
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        currentSearch = s.toString().trim().toLowerCase();
        filterAndRender();
    }

    @Override
    public void afterTextChanged(Editable s) {}

    private void updateCategoryButtonStyles() {
        int activeBg = getResources().getColor(R.color.accent_blue_dark);
        int inactiveBg = getResources().getColor(R.color.bg_card);
        int activeText = getResources().getColor(R.color.text_primary);
        int inactiveText = getResources().getColor(R.color.text_muted);

        catAndroid.setBackgroundColor("android".equals(currentCategory) ? activeBg : inactiveBg);
        catAndroid.setTextColor("android".equals(currentCategory) ? activeText : inactiveText);

        catPC.setBackgroundColor("pc".equals(currentCategory) ? activeBg : inactiveBg);
        catPC.setTextColor("pc".equals(currentCategory) ? activeText : inactiveText);

        catServer.setBackgroundColor("server".equals(currentCategory) ? activeBg : inactiveBg);
        catServer.setTextColor("server".equals(currentCategory) ? activeText : inactiveText);
    }

    private void loadCatalog() {
        statusText.setText("Synchronisiere mit BenzStore Repository...");
        FeedLoader.loadFeed(this, this);
    }

    @Override
    public void onSuccess(final List<AppModel> apps, final boolean fromCache) {
        runOnUiThread(new SuccessHandler(this, apps, fromCache));
    }

    public static class SuccessHandler implements Runnable {
        private final MainActivity activity;
        private final List<AppModel> apps;
        private final boolean fromCache;
        public SuccessHandler(MainActivity a, List<AppModel> apps, boolean fromCache) {
            this.activity = a;
            this.apps = apps;
            this.fromCache = fromCache;
        }
        @Override
        public void run() {
            activity.allApps = apps;
            activity.statusText.setText(apps.size() + " Anwendungen bereitgestellt " + (fromCache ? "(Lokal / Cache)" : "(Live Feed)"));
            activity.checkForUpdates();
            activity.filterAndRender();
        }
    }

    @Override
    public void onError(final String message) {
        runOnUiThread(new ErrorHandler(this, message));
    }

    public static class ErrorHandler implements Runnable {
        private final MainActivity activity;
        private final String message;
        public ErrorHandler(MainActivity a, String msg) {
            this.activity = a;
            this.message = msg;
        }
        @Override
        public void run() {
            activity.statusText.setText("Status: " + message);
            Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
        }
    }

    public static class CardClickListener implements View.OnClickListener {
        private final MainActivity activity;
        private final AppModel app;
        public CardClickListener(MainActivity a, AppModel app) {
            this.activity = a;
            this.app = app;
        }
        @Override
        public void onClick(View v) {
            AppDetailActivity.currentApp = app;
            Intent intent = new Intent(activity, AppDetailActivity.class);
            activity.startActivity(intent);
        }
    }

    public static class VersionButtonClickListener implements View.OnClickListener {
        private final MainActivity activity;
        private final AppModel app;
        public VersionButtonClickListener(MainActivity a, AppModel app) {
            this.activity = a;
            this.app = app;
        }
        @Override
        public void onClick(View v) {
            AppDetailActivity.currentApp = app;
            Intent intent = new Intent(activity, AppDetailActivity.class);
            activity.startActivity(intent);
        }
    }

    public static int compareVersions(String v1, String v2) {
        if (v1 == null && v2 == null) return 0;
        if (v1 == null) return -1;
        if (v2 == null) return 1;

        String clean1 = v1.trim().replaceFirst("^[vV]", "");
        String clean2 = v2.trim().replaceFirst("^[vV]", "");

        String[] parts1 = clean1.split("[.-]");
        String[] parts2 = clean2.split("[.-]");

        int length = Math.max(parts1.length, parts2.length);
        for (int i = 0; i < length; i++) {
            long p1 = 0;
            if (i < parts1.length) {
                try { p1 = Long.parseLong(parts1[i]); } catch (NumberFormatException ignored) {}
            }
            long p2 = 0;
            if (i < parts2.length) {
                try { p2 = Long.parseLong(parts2[i]); } catch (NumberFormatException ignored) {}
            }
            if (p1 < p2) return -1;
            if (p1 > p2) return 1;
        }
        return 0;
    }

    public static class ActionButtonClickListener implements View.OnClickListener {
        private final MainActivity activity;
        private final AppModel app;
        private final boolean isInstalled;
        public ActionButtonClickListener(MainActivity a, AppModel app, boolean isInstalled) {
            this.activity = a;
            this.app = app;
            this.isInstalled = isInstalled;
        }
        @Override
        public void onClick(View v) {
            if (app.supportsAndroid()) {
                if (isInstalled) {
                    try {
                        PackageInfo pInfo = activity.getPackageManager().getPackageInfo(app.getId(), 0);
                        String installedVer = (pInfo != null && pInfo.versionName != null) ? pInfo.versionName : "";
                        if (MainActivity.compareVersions(app.getLatestVersion(), installedVer) <= 0) {
                            // Already up to date -> Launch directly
                            Intent launchIntent = activity.getPackageManager().getLaunchIntentForPackage(app.getId());
                            if (launchIntent != null) {
                                activity.startActivity(launchIntent);
                                return;
                            }
                        }
                    } catch (Exception ignored) {}
                }
            }
            AppDetailActivity.currentApp = app;
            Intent intent = new Intent(activity, AppDetailActivity.class);
            activity.startActivity(intent);
        }
    }

    public static boolean isServerApp(AppModel app) {
        if (app == null) return false;
        for (String cat : app.getCategories()) {
            if ("server".equalsIgnoreCase(cat)) return true;
        }
        String id = app.getId();
        return id != null && (id.contains("server") || id.contains("plugin"));
    }

    private void checkForUpdates() {
        PackageManager pm = getPackageManager();
        List<String> updatableNames = new ArrayList<String>();

        for (AppModel app : allApps) {
            if (!app.supportsAndroid()) continue;
            try {
                PackageInfo pInfo = pm.getPackageInfo(app.getId(), 0);
                if (pInfo != null) {
                    String installedVer = pInfo.versionName != null ? pInfo.versionName : "";
                    if (compareVersions(app.getLatestVersion(), installedVer) > 0) {
                        updatableNames.add(app.getName() + " (v" + app.getLatestVersion() + ")");
                    }
                }
            } catch (PackageManager.NameNotFoundException ignored) {}
        }

        if (!updatableNames.isEmpty()) {
            layoutUpdateBanner.setVisibility(View.VISIBLE);
            txtUpdateBannerTitle.setText(updatableNames.size() + " Update(s) verfügbar");
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < updatableNames.size(); i++) {
                sb.append(updatableNames.get(i));
                if (i < updatableNames.size() - 1) sb.append(", ");
            }
            txtUpdateBannerSub.setText("Bereit: " + sb.toString());
            postUpdateNotification(updatableNames.size(), sb.toString());
        } else {
            layoutUpdateBanner.setVisibility(View.GONE);
        }
    }

    private void postUpdateNotification(int updateCount, String appListStr) {
        if (Build.VERSION.SDK_INT >= 33) {
            if (checkSelfPermission("android.permission.POST_NOTIFICATIONS") != PackageManager.PERMISSION_GRANTED) {
                return;
            }
        }

        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm == null) return;

        String channelId = "benzstore_updates";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId, "BenzStore Updates", NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("Benachrichtigungen über neue App-Versionen im BenzStore");
            nm.createNotificationChannel(channel);
        }

        Intent intent = new Intent(this, MainActivity.class);
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }
        PendingIntent pi = PendingIntent.getActivity(this, 0, intent, flags);

        Notification.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder = new Notification.Builder(this, channelId);
        } else {
            builder = new Notification.Builder(this);
        }

        builder.setSmallIcon(R.drawable.icon)
                .setContentTitle("BenzStore: " + updateCount + " Update(s) verfügbar")
                .setContentText("Aktualisierungen bereit für: " + appListStr)
                .setContentIntent(pi)
                .setAutoCancel(true);

        nm.notify(1001, builder.build());
    }

    public void filterAndRender() {
        appListContainer.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);
        PackageManager pm = getPackageManager();

        int count = 0;
        for (final AppModel app : allApps) {
            boolean isInstalled = false;
            String installedVersion = "";
            boolean hasUpdate = false;

            try {
                PackageInfo pInfo = pm.getPackageInfo(app.getId(), 0);
                if (pInfo != null) {
                    isInstalled = true;
                    installedVersion = pInfo.versionName != null ? pInfo.versionName : "";
                    if (compareVersions(app.getLatestVersion(), installedVersion) > 0) {
                        hasUpdate = true;
                    }
                }
            } catch (PackageManager.NameNotFoundException ignored) {}

            if (showOnlyUpdates) {
                if (!hasUpdate) continue;
            } else {
                boolean match = false;
                if ("android".equals(currentCategory)) {
                    if (app.supportsAndroid()) match = true;
                } else if ("pc".equals(currentCategory)) {
                    if (app.supportsPC() && !isServerApp(app)) match = true;
                } else if ("server".equals(currentCategory)) {
                    if (isServerApp(app)) match = true;
                }
                if (!match) continue;
            }

            if (!currentSearch.isEmpty()) {
                boolean match = app.getName().toLowerCase().contains(currentSearch)
                        || app.getSummary().toLowerCase().contains(currentSearch)
                        || app.getDescription().toLowerCase().contains(currentSearch);
                if (!match) continue;
            }

            count++;
            View card = inflater.inflate(R.layout.item_app_card, appListContainer, false);

            final ImageView iconView = card.findViewById(R.id.appIcon);
            TextView nameView = card.findViewById(R.id.appName);
            TextView versionBadge = card.findViewById(R.id.appVersionBadge);
            TextView installStatusBadge = card.findViewById(R.id.appInstallStatusBadge);
            TextView summaryView = card.findViewById(R.id.appSummary);
            TextView platformPill = card.findViewById(R.id.appPlatformPill);
            Button btnAction = card.findViewById(R.id.btnCardAction);
            Button btnVersions = card.findViewById(R.id.btnCardVersions);

            nameView.setText(app.getName());
            versionBadge.setText("v" + app.getLatestVersion());
            summaryView.setText(app.getSummary());

            StringBuilder platText = new StringBuilder();
            if (isServerApp(app)) {
                platText.append("Server & Cloud");
            } else {
                if (app.supportsAndroid()) platText.append("Android ");
                if (app.supportsPC()) platText.append("PC/Desktop");
            }
            platformPill.setText(platText.toString().trim());

            if (app.supportsAndroid()) {
                if (isInstalled) {
                    installStatusBadge.setVisibility(View.VISIBLE);
                    int cmp = compareVersions(app.getLatestVersion(), installedVersion);
                    if (cmp > 0) {
                        // Newer version available -> Update
                        installStatusBadge.setText("Installiert: v" + installedVersion + " • Update verfügbar");
                        installStatusBadge.setTextColor(getResources().getColor(R.color.accent_amber));
                        btnAction.setText("Update (v" + app.getLatestVersion() + ")");
                        btnAction.setBackgroundColor(getResources().getColor(R.color.accent_amber));
                    } else {
                        // Same or newer version installed -> Up to date
                        installStatusBadge.setText("✓ Installiert (v" + installedVersion + ")");
                        installStatusBadge.setTextColor(getResources().getColor(R.color.accent_green));
                        btnAction.setText("Öffnen");
                        btnAction.setBackgroundColor(getResources().getColor(R.color.accent_green));
                    }
                } else {
                    installStatusBadge.setVisibility(View.GONE);
                    btnAction.setText("Installieren");
                    btnAction.setBackgroundColor(getResources().getColor(R.color.accent_blue_dark));
                }
            } else if (isServerApp(app)) {
                installStatusBadge.setVisibility(View.GONE);
                btnAction.setText("Server-Paket");
                btnAction.setBackgroundColor(getResources().getColor(R.color.bg_surface));
            } else {
                installStatusBadge.setVisibility(View.GONE);
                btnAction.setText("PC-App");
                btnAction.setBackgroundColor(getResources().getColor(R.color.bg_surface));
            }

            int verCount = app.getVersions() != null ? app.getVersions().size() : 1;
            btnVersions.setText(verCount > 1 ? verCount + " Versionen ▾" : "Versionen ▾");

            loadAppIcon(app.getIconUrl(), iconView);

            card.setOnClickListener(new CardClickListener(this, app));
            btnAction.setOnClickListener(new ActionButtonClickListener(this, app, isInstalled));
            btnVersions.setOnClickListener(new VersionButtonClickListener(this, app));

            appListContainer.addView(card);
        }

        if (count == 0) {
            TextView emptyText = new TextView(this);
            emptyText.setText(showOnlyUpdates ? "Alle installierten Apps sind auf dem neuesten Stand!" : "Keine Anwendungen in dieser Kategorie gefunden.");
            emptyText.setTextColor(getResources().getColor(R.color.text_muted));
            emptyText.setPadding(0, 32, 0, 0);
            appListContainer.addView(emptyText);
        }
    }

    public static class IconTask implements Runnable {
        private final MainActivity activity;
        private final String iconUrl;
        private final ImageView view;
        public IconTask(MainActivity a, String url, ImageView v) {
            this.activity = a;
            this.iconUrl = url;
            this.view = v;
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
                            activity.runOnUiThread(new ApplyBitmapTask(activity, iconUrl, view, bmp));
                        }
                    }
                }
            } catch (Exception ignored) {}
        }
    }

    public static class ApplyBitmapTask implements Runnable {
        private final MainActivity activity;
        private final String iconUrl;
        private final ImageView view;
        private final Bitmap bmp;
        public ApplyBitmapTask(MainActivity a, String url, ImageView v, Bitmap b) {
            this.activity = a;
            this.iconUrl = url;
            this.view = v;
            this.bmp = b;
        }
        @Override
        public void run() {
            activity.iconCache.put(iconUrl, bmp);
            view.setImageBitmap(bmp);
        }
    }

    private void loadAppIcon(String iconUrl, ImageView view) {
        if (iconUrl == null || iconUrl.isEmpty()) return;
        String filename = iconUrl.substring(iconUrl.lastIndexOf('/') + 1);
        if (filename.endsWith(".png")) filename = filename.substring(0, filename.length() - 4);
        String resName = filename.replace('-', '_').toLowerCase();
        int resId = getResources().getIdentifier(resName, "drawable", getPackageName());
        if (resId != 0) {
            view.setImageResource(resId);
        }
        if (iconCache.containsKey(iconUrl)) {
            view.setImageBitmap(iconCache.get(iconUrl));
            return;
        }
        new Thread(new IconTask(this, iconUrl, view)).start();
    }
}
