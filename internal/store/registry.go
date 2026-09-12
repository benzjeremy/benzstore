package store

import (
	"encoding/json"
	"os"
	"path/filepath"
	"time"

	"github.com/benzjeremy/benzstore/internal/config"
)

type InstalledAppInfo struct {
	AppID       string `json:"app_id"`
	Version     string `json:"version"`
	BinaryPath  string `json:"binary_path"`
	InstalledAt string `json:"installed_at"`
}

type InstalledRegistry struct {
	Apps map[string]InstalledAppInfo `json:"apps"`
}

func getRegistryPath() (string, error) {
	cfgDir, err := config.GetStoreConfigDir()
	if err != nil {
		return "", err
	}
	return filepath.Join(cfgDir, "installed.json"), nil
}

func LoadRegistry() (*InstalledRegistry, error) {
	regPath, err := getRegistryPath()
	if err != nil {
		return &InstalledRegistry{Apps: make(map[string]InstalledAppInfo)}, err
	}

	data, err := os.ReadFile(regPath)
	if err != nil {
		return &InstalledRegistry{Apps: make(map[string]InstalledAppInfo)}, nil
	}

	var reg InstalledRegistry
	if err := json.Unmarshal(data, &reg); err != nil {
		return &InstalledRegistry{Apps: make(map[string]InstalledAppInfo)}, nil
	}
	if reg.Apps == nil {
		reg.Apps = make(map[string]InstalledAppInfo)
	}
	return &reg, nil
}

func SaveRegistry(reg *InstalledRegistry) error {
	regPath, err := getRegistryPath()
	if err != nil {
		return err
	}
	if reg.Apps == nil {
		reg.Apps = make(map[string]InstalledAppInfo)
	}
	data, err := json.MarshalIndent(reg, "", "  ")
	if err != nil {
		return err
	}
	return os.WriteFile(regPath, data, 0644)
}

func RegisterInstalledApp(appID, version, binaryPath string) error {
	reg, _ := LoadRegistry()
	if reg == nil {
		reg = &InstalledRegistry{Apps: make(map[string]InstalledAppInfo)}
	}
	reg.Apps[appID] = InstalledAppInfo{
		AppID:       appID,
		Version:     version,
		BinaryPath:  binaryPath,
		InstalledAt: time.Now().Format(time.RFC3339),
	}
	return SaveRegistry(reg)
}

func UnregisterInstalledApp(appID string) error {
	reg, _ := LoadRegistry()
	if reg != nil && reg.Apps != nil {
		delete(reg.Apps, appID)
		return SaveRegistry(reg)
	}
	return nil
}
