package config

import (
	"os"
	"path/filepath"
)

const (
	AppName        = "BenzStore"
	AppVersion    = "1.0"
	DefaultFeedURL = "https://raw.githubusercontent.com/benzjeremy/benzstore/content/feed.json"
)

func GetUserBinDir() (string, error) {
	home, err := os.UserHomeDir()
	if err != nil {
		return "", err
	}
	binDir := filepath.Join(home, ".local", "bin")
	if err := os.MkdirAll(binDir, 0755); err != nil {
		return "", err
	}
	return binDir, nil
}

func GetDesktopEntriesDir() (string, error) {
	home, err := os.UserHomeDir()
	if err != nil {
		return "", err
	}
	appsDir := filepath.Join(home, ".local", "share", "applications")
	if err := os.MkdirAll(appsDir, 0755); err != nil {
		return "", err
	}
	return appsDir, nil
}

func GetStoreCacheDir() (string, error) {
	cache, err := os.UserCacheDir()
	if err != nil {
		home, _ := os.UserHomeDir()
		cache = filepath.Join(home, ".cache")
	}
	storeCache := filepath.Join(cache, "benzstore")
	if err := os.MkdirAll(storeCache, 0755); err != nil {
		return "", err
	}
	return storeCache, nil
}
