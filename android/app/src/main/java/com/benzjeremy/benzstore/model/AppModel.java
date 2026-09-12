package com.benzjeremy.benzstore.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class AppModel implements Serializable {
    private String id;
    private String name;
    private String summary;
    private String description;
    private String author;
    private String license;
    private String website;
    private String source;
    private String iconUrl;
    private List<String> categories;
    private List<String> platforms;
    private String latestVersion;
    private List<VersionModel> versions;

    public AppModel(String id, String name, String summary, String description,
                    String author, String license, String website, String source,
                    String iconUrl, List<String> categories, List<String> platforms,
                    String latestVersion) {
        this.id = id;
        this.name = name;
        this.summary = summary;
        this.description = description;
        this.author = author;
        this.license = license;
        this.website = website;
        this.source = source;
        this.iconUrl = iconUrl;
        this.categories = categories != null ? categories : new ArrayList<String>();
        this.platforms = platforms != null ? platforms : new ArrayList<String>();
        this.latestVersion = latestVersion;
        this.versions = new ArrayList<VersionModel>();
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getSummary() { return summary; }
    public String getDescription() { return description; }
    public String getAuthor() { return author; }
    public String getLicense() { return license; }
    public String getWebsite() { return website; }
    public String getSource() { return source; }
    public String getIconUrl() { return iconUrl; }
    public List<String> getCategories() { return categories; }
    public List<String> getPlatforms() { return platforms; }
    public String getLatestVersion() { return latestVersion; }
    public List<VersionModel> getVersions() { return versions; }

    public void addVersion(VersionModel v) {
        this.versions.add(v);
    }

    public VersionModel getLatestVersionModel() {
        if (versions.isEmpty()) return null;
        for (VersionModel vm : versions) {
            if (vm.getVersion().equalsIgnoreCase(latestVersion)) {
                return vm;
            }
        }
        return versions.get(0);
    }

    public boolean supportsAndroid() {
        for (String p : platforms) {
            if ("android".equalsIgnoreCase(p)) return true;
        }
        return false;
    }

    public boolean supportsPC() {
        for (String p : platforms) {
            if ("linux".equalsIgnoreCase(p) || "windows".equalsIgnoreCase(p) || "pc".equalsIgnoreCase(p)) return true;
        }
        return false;
    }
}
