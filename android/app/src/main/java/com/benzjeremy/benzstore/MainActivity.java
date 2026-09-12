package com.benzjeremy.benzstore;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
    private Button catAll, catAndroid, catPC, catTools, catEdu;

    private List<AppModel> allApps = new ArrayList<AppModel>();
    private String currentCategory = "all";
    private String currentSearch = "";
    private final Map<String, Bitmap> iconCache = new HashMap<String, Bitmap>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        searchBar = findViewById(R.id.searchBar);
        statusText = findViewById(R.id.statusText);
        appListContainer = findViewById(R.id.appListContainer);
        btnRefresh = findViewById(R.id.btnRefresh);
        catAll = findViewById(R.id.catAll);
        catAndroid = findViewById(R.id.catAndroid);
        catPC = findViewById(R.id.catPC);
        catTools = findViewById(R.id.catTools);
        catEdu = findViewById(R.id.catEdu);

        btnRefresh.setOnClickListener(this);
        searchBar.addTextChangedListener(this);

        catAll.setOnClickListener(this);
        catAndroid.setOnClickListener(this);
        catPC.setOnClickListener(this);
        catTools.setOnClickListener(this);
        catEdu.setOnClickListener(this);

        loadCatalog();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!allApps.isEmpty()) {
            filterAndRender();
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btnRefresh) {
            loadCatalog();
        } else if (id == R.id.catAll) {
            currentCategory = "all";
            updateCategoryButtonStyles();
            filterAndRender();
        } else if (id == R.id.catAndroid) {
            currentCategory = "android";
            updateCategoryButtonStyles();
            filterAndRender();
        } else if (id == R.id.catPC) {
            currentCategory = "pc";
            updateCategoryButtonStyles();
            filterAndRender();
        } else if (id == R.id.catTools) {
            currentCategory = "tools";
            updateCategoryButtonStyles();
            filterAndRender();
        } else if (id == R.id.catEdu) {
            currentCategory = "education";
            updateCategoryButtonStyles();
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

        catAll.setBackgroundColor("all".equals(currentCategory) ? activeBg : inactiveBg);
        catAll.setTextColor("all".equals(currentCategory) ? activeText : inactiveText);

        catAndroid.setBackgroundColor("android".equals(currentCategory) ? activeBg : inactiveBg);
        catAndroid.setTextColor("android".equals(currentCategory) ? activeText : inactiveText);

        catPC.setBackgroundColor("pc".equals(currentCategory) ? activeBg : inactiveBg);
        catPC.setTextColor("pc".equals(currentCategory) ? activeText : inactiveText);

        catTools.setBackgroundColor("tools".equals(currentCategory) ? activeBg : inactiveBg);
        catTools.setTextColor("tools".equals(currentCategory) ? activeText : inactiveText);

        catEdu.setBackgroundColor("education".equals(currentCategory) ? activeBg : inactiveBg);
        catEdu.setTextColor("education".equals(currentCategory) ? activeText : inactiveText);
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

    public void filterAndRender() {
        appListContainer.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);
        PackageManager pm = getPackageManager();

        int count = 0;
        for (final AppModel app : allApps) {
            if (!"all".equals(currentCategory)) {
                boolean match = false;
                if ("android".equals(currentCategory) && app.supportsAndroid()) match = true;
                else if ("pc".equals(currentCategory) && app.supportsPC()) match = true;
                else {
                    for (String cat : app.getCategories()) {
                        if (cat.equalsIgnoreCase(currentCategory)) {
                            match = true;
                            break;
                        }
                    }
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

            nameView.setText(app.getName());
            versionBadge.setText("v" + app.getLatestVersion());
            summaryView.setText(app.getSummary());

            StringBuilder platText = new StringBuilder();
            if (app.supportsAndroid()) platText.append("Android ");
            if (app.supportsPC()) platText.append("PC/Desktop");
            platformPill.setText(platText.toString().trim());

            boolean isInstalled = false;
            String installedVersion = "";
            try {
                PackageInfo pInfo = pm.getPackageInfo(app.getId(), 0);
                if (pInfo != null) {
                    isInstalled = true;
                    installedVersion = pInfo.versionName != null ? pInfo.versionName : "";
                }
            } catch (PackageManager.NameNotFoundException ignored) {}

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
            } else {
                installStatusBadge.setVisibility(View.GONE);
                btnAction.setText("PC-App");
                btnAction.setBackgroundColor(getResources().getColor(R.color.bg_surface));
            }

            loadAppIcon(app.getIconUrl(), iconView);

            card.setOnClickListener(new CardClickListener(this, app));
            btnAction.setOnClickListener(new ActionButtonClickListener(this, app, isInstalled));

            appListContainer.addView(card);
        }

        if (count == 0) {
            TextView emptyText = new TextView(this);
            emptyText.setText("Keine Anwendungen in dieser Kategorie gefunden.");
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
        if (iconCache.containsKey(iconUrl)) {
            view.setImageBitmap(iconCache.get(iconUrl));
            return;
        }
        new Thread(new IconTask(this, iconUrl, view)).start();
    }
}
