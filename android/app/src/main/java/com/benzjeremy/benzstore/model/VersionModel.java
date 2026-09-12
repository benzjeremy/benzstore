package com.benzjeremy.benzstore.model;

import java.io.Serializable;

public class VersionModel implements Serializable {
    private String version;
    private int versionCode;
    private String releaseDate;
    private String changelog;
    private String downloadUrl;
    private String filename;
    private String sha256;
    private long size;

    public VersionModel(String version, int versionCode, String releaseDate, String changelog,
                        String downloadUrl, String filename, String sha256, long size) {
        this.version = version;
        this.versionCode = versionCode;
        this.releaseDate = releaseDate;
        this.changelog = changelog;
        this.downloadUrl = downloadUrl;
        this.filename = filename;
        this.sha256 = sha256;
        this.size = size;
    }

    public String getVersion() { return version; }
    public int getVersionCode() { return versionCode; }
    public String getReleaseDate() { return releaseDate; }
    public String getChangelog() { return changelog; }
    public String getDownloadUrl() { return downloadUrl; }
    public String getFilename() { return filename; }
    public String getSha256() { return sha256; }
    public long getSize() { return size; }

    public String getFormattedSize() {
        if (size <= 0) return "Größe unbekannt";
        if (size < 1024 * 1024) {
            return String.format("%.1f KB", size / 1024.0);
        }
        return String.format("%.2f MB", size / (1024.0 * 1024.0));
    }

    @Override
    public String toString() {
        return "v" + version + " (" + releaseDate + ")";
    }
}
