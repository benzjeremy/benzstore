package com.benzjeremy.benzstore.util;

import android.content.Context;
import com.benzjeremy.benzstore.model.AppModel;
import com.benzjeremy.benzstore.model.VersionModel;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class FeedLoader {
    public static final String REMOTE_FEED_URL = "https://benzjeremy.github.io/benzstore/api/v1/apps.json";
    private static final String CACHE_FILE_NAME = "cached_feed.json";

    public interface FeedCallback {
        void onSuccess(List<AppModel> apps, boolean fromCache);
        void onError(String message);
    }

    public static class Worker implements Runnable {
        private final Context context;
        private final FeedCallback callback;

        public Worker(Context context, FeedCallback callback) {
            this.context = context;
            this.callback = callback;
        }

        @Override
        public void run() {
            String jsonStr = null;
            boolean fromCache = false;

            // 1. Try remote fetch
            try {
                URL url = new URL(REMOTE_FEED_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(6000);
                conn.setReadTimeout(6000);
                conn.setRequestProperty("User-Agent", "BenzStore-Android/1.0");

                if (conn.getResponseCode() == 200) {
                    try (InputStream in = conn.getInputStream()) {
                        jsonStr = readStream(in);
                        saveToCache(context, jsonStr);
                    }
                }
            } catch (Exception ignored) {}

            // 2. Fallback to local cache
            if (jsonStr == null || jsonStr.isEmpty()) {
                jsonStr = readFromCache(context);
                if (jsonStr != null && !jsonStr.isEmpty()) {
                    fromCache = true;
                }
            }

            // 3. Fallback to bundled assets
            if (jsonStr == null || jsonStr.isEmpty()) {
                jsonStr = readFromAssets(context);
                fromCache = true;
            }

            if (jsonStr == null || jsonStr.isEmpty()) {
                callback.onError("Katalog konnte weder online noch offline geladen werden.");
                return;
            }

            try {
                List<AppModel> apps = parseFeed(jsonStr);
                callback.onSuccess(apps, fromCache);
            } catch (Exception e) {
                callback.onError("Fehler beim Verarbeiten des Store-Katalogs: " + e.getMessage());
            }
        }
    }

    public static void loadFeed(Context context, FeedCallback callback) {
        new Thread(new Worker(context, callback)).start();
    }

    public static List<AppModel> parseFeed(String jsonStr) throws Exception {
        List<AppModel> result = new ArrayList<AppModel>();
        JSONObject root = new JSONObject(jsonStr);
        JSONArray appsArr = root.getJSONArray("apps");

        for (int i = 0; i < appsArr.length(); i++) {
            JSONObject appObj = appsArr.getJSONObject(i);
            String id = appObj.getString("id");
            String name = appObj.getString("name");
            String summary = appObj.optString("summary_de", appObj.optString("summary_en", ""));
            String desc = appObj.optString("description_de", appObj.optString("description_en", ""));
            String author = appObj.optString("author", "Jeremy Benz");
            String license = appObj.optString("license", "GPL-3.0");
            String website = appObj.optString("website", "");
            String source = appObj.optString("source", "");
            String iconUrl = appObj.optString("icon", "");
            String latestVer = appObj.optString("latest_version", "1.0");

            List<String> categories = new ArrayList<String>();
            if (appObj.has("categories")) {
                JSONArray catArr = appObj.getJSONArray("categories");
                for (int c = 0; c < catArr.length(); c++) {
                    categories.add(catArr.getString(c));
                }
            }

            List<String> platforms = new ArrayList<String>();
            if (appObj.has("platforms")) {
                JSONArray platArr = appObj.getJSONArray("platforms");
                for (int p = 0; p < platArr.length(); p++) {
                    platforms.add(platArr.getString(p));
                }
            }

            AppModel app = new AppModel(id, name, summary, desc, author, license, website, source, iconUrl, categories, platforms, latestVer);

            if (appObj.has("versions")) {
                JSONArray verArr = appObj.getJSONArray("versions");
                for (int v = 0; v < verArr.length(); v++) {
                    JSONObject vObj = verArr.getJSONObject(v);
                    String ver = vObj.getString("version");
                    int vCode = vObj.optInt("version_code", 100);
                    String relDate = vObj.optString("release_date", "");
                    String chg = vObj.optString("changelog_de", vObj.optString("changelog_en", ""));

                    String dlUrl = "";
                    String fn = "";
                    String sha = "";
                    long sz = 0;

                    if (vObj.has("downloads")) {
                        JSONObject dlObj = vObj.getJSONObject("downloads");
                        if (dlObj.has("android")) {
                            JSONObject andObj = dlObj.getJSONObject("android");
                            dlUrl = andObj.optString("url", "");
                            fn = andObj.optString("filename", "");
                            sha = andObj.optString("sha256", "");
                            sz = andObj.optLong("size", 0);
                        } else if (dlObj.has("linux")) {
                            JSONObject linObj = dlObj.getJSONObject("linux");
                            dlUrl = linObj.optString("url", "");
                            fn = linObj.optString("filename", "");
                            sha = linObj.optString("sha256", "");
                            sz = linObj.optLong("size", 0);
                        }
                    }

                    VersionModel vm = new VersionModel(ver, vCode, relDate, chg, dlUrl, fn, sha, sz);
                    app.addVersion(vm);
                }
            }

            result.add(app);
        }

        return result;
    }

    private static String readStream(InputStream in) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line).append('\n');
        }
        return sb.toString();
    }

    private static void saveToCache(Context context, String data) {
        try {
            File f = new File(context.getCacheDir(), CACHE_FILE_NAME);
            try (FileOutputStream fos = new FileOutputStream(f)) {
                fos.write(data.getBytes(StandardCharsets.UTF_8));
            }
        } catch (Exception ignored) {}
    }

    private static String readFromCache(Context context) {
        try {
            File f = new File(context.getCacheDir(), CACHE_FILE_NAME);
            if (f.exists()) {
                try (FileInputStream fis = new FileInputStream(f)) {
                    return readStream(fis);
                }
            }
        } catch (Exception ignored) {}
        return null;
    }

    private static String readFromAssets(Context context) {
        try {
            try (InputStream in = context.getAssets().open("default_apps.json")) {
                return readStream(in);
            }
        } catch (Exception ignored) {}
        return null;
    }
}
