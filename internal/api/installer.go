package api

import (
	"archive/tar"
	"archive/zip"
	"bytes"
	"compress/gzip"
	"crypto/sha256"
	"encoding/hex"
	"fmt"
	"io"
	"net/http"
	"os"
	"path/filepath"
	"strings"

	"github.com/benzjeremy/benzstore/internal/config"
	"github.com/benzjeremy/benzstore/internal/store"
)

type Installer struct {
	client *store.Client
}

func NewInstaller(client *store.Client) *Installer {
	return &Installer{client: client}
}

func (inst *Installer) InstallApp(appID, versionStr string, force bool) error {
	feed, err := inst.client.FetchFeed()
	if err != nil {
		return fmt.Errorf("failed to load catalog: %w", err)
	}

	var targetApp *store.App
	for i := range feed.Apps {
		if feed.Apps[i].ID == appID {
			targetApp = &feed.Apps[i]
			break
		}
	}

	if targetApp == nil {
		return fmt.Errorf("app %s not found in store", appID)
	}

	var targetVer *store.Version
	if versionStr == "" || versionStr == "latest" {
		for i := range targetApp.Versions {
			if strings.EqualFold(targetApp.Versions[i].Version, targetApp.LatestVersion) {
				targetVer = &targetApp.Versions[i]
				break
			}
		}
		if targetVer == nil && len(targetApp.Versions) > 0 {
			targetVer = &targetApp.Versions[0]
		}
	} else {
		for i := range targetApp.Versions {
			if strings.EqualFold(targetApp.Versions[i].Version, strings.TrimPrefix(versionStr, "v")) {
				targetVer = &targetApp.Versions[i]
				break
			}
		}
	}

	if targetVer == nil {
		return fmt.Errorf("version %s not found for app %s", versionStr, appID)
	}

	// Safeguard: Check existing installation for duplicate version or downgrade
	reg, _ := store.LoadRegistry()
	if reg != nil {
		if entry, ok := reg.Apps[appID]; ok {
			binDir, _ := config.GetUserBinDir()
			binPath := entry.BinaryPath
			if binPath == "" {
				parts := strings.Split(appID, ".")
				binPath = filepath.Join(binDir, parts[len(parts)-1])
			}
			if fi, err := os.Stat(binPath); err == nil && !fi.IsDir() {
				cmp := store.CompareVersions(targetVer.Version, entry.Version)
				if cmp == 0 && !force {
					return fmt.Errorf("App '%s' ist bereits in Version v%s installiert.", targetApp.Name, entry.Version)
				}
				if cmp < 0 && !force {
					return fmt.Errorf("Downgrade-Warnung: App '%s' ist in Version v%s installiert. Version v%s ist älter!", targetApp.Name, entry.Version, targetVer.Version)
				}
			}
		}
	}

	asset, ok := targetVer.Downloads["linux"]
	if !ok {
		return fmt.Errorf("app %s does not provide a linux binary", appID)
	}

	binDir, err := config.GetUserBinDir()
	if err != nil {
		return fmt.Errorf("failed to get user bin directory: %w", err)
	}

	binName := asset.BinName
	if binName == "" {
		parts := strings.Split(appID, ".")
		binName = parts[len(parts)-1]
	}

	fmt.Printf("==> Lade %s v%s herunter: %s\n", targetApp.Name, targetVer.Version, asset.URL)

	resp, err := http.Get(asset.URL)
	if err != nil {
		return fmt.Errorf("download error: %w", err)
	}
	defer resp.Body.Close()

	if resp.StatusCode != http.StatusOK {
		return fmt.Errorf("server returned HTTP %d for %s", resp.StatusCode, asset.URL)
	}

	data, err := io.ReadAll(resp.Body)
	if err != nil {
		return fmt.Errorf("failed to read response: %w", err)
	}

	hasher := sha256.New()
	hasher.Write(data)
	actualSHA := hex.EncodeToString(hasher.Sum(nil))

	if asset.SHA256 != "" && !strings.HasPrefix(asset.SHA256, "PENDING") {
		if !strings.EqualFold(actualSHA, asset.SHA256) {
			return fmt.Errorf("SECURITY ALERT: SHA-256 checksum mismatch!\nExpected: %s\nActual:   %s", asset.SHA256, actualSHA)
		}
		fmt.Printf("==> [Security] SHA-256 Prüfsumme verifiziert: %s\n", actualSHA)
	}

	targetPath := filepath.Join(binDir, binName)

	if strings.HasSuffix(asset.Filename, ".tar.gz") || asset.Type == "tar.gz" {
		binData, err := extractBinaryFromTarGz(data, binName)
		if err != nil {
			return fmt.Errorf("extraction error: %w", err)
		}
		if err := os.WriteFile(targetPath, binData, 0755); err != nil {
			return fmt.Errorf("failed to write binary: %w", err)
		}
	} else if strings.HasSuffix(asset.Filename, ".zip") || asset.Type == "zip" {
		binData, err := extractBinaryFromZip(data, binName)
		if err != nil {
			return fmt.Errorf("zip extraction error: %w", err)
		}
		if err := os.WriteFile(targetPath, binData, 0755); err != nil {
			return fmt.Errorf("failed to write binary: %w", err)
		}
	} else {
		// Single standalone binary!
		if err := os.WriteFile(targetPath, data, 0755); err != nil {
			return fmt.Errorf("failed to write binary: %w", err)
		}
	}

	fmt.Printf("==> Installiert in: %s\n", targetPath)
	_ = store.RegisterInstalledApp(appID, targetVer.Version, targetPath)

	if asset.DesktopEntry != nil {
		inst.installDesktopEntry(binName, targetApp, asset.DesktopEntry)
	}

	return nil
}

func (inst *Installer) UninstallApp(appID string) error {
	binDir, err := config.GetUserBinDir()
	if err != nil {
		return err
	}

	parts := strings.Split(appID, ".")
	binName := parts[len(parts)-1]

	targetPath := filepath.Join(binDir, binName)
	_ = os.Remove(targetPath)

	desktopDir, _ := config.GetDesktopEntriesDir()
	desktopPath := filepath.Join(desktopDir, binName+".desktop")
	_ = os.Remove(desktopPath)

	_ = store.UnregisterInstalledApp(appID)

	fmt.Printf("==> %s deinstalliert.\n", appID)
	return nil
}

func (inst *Installer) installDesktopEntry(binName string, app *store.App, d *store.DesktopEntryConfig) {
	desktopDir, err := config.GetDesktopEntriesDir()
	if err != nil {
		return
	}

	termStr := "false"
	if d.Terminal {
		termStr = "true"
	}

	content := fmt.Sprintf(`[Desktop Entry]
Type=Application
Name=%s
Comment=%s
Exec=%s
Terminal=%s
Categories=%s
Icon=%s
`, d.Name, d.Comment, binName, termStr, d.Categories, app.Icon)

	desktopFile := filepath.Join(desktopDir, binName+".desktop")
	_ = os.WriteFile(desktopFile, []byte(content), 0644)
	fmt.Printf("==> Desktop-Starter erstellt: %s\n", desktopFile)
}

func extractBinaryFromTarGz(data []byte, binName string) ([]byte, error) {
	gr, err := gzip.NewReader(bytes.NewReader(data))
	if err != nil {
		return nil, err
	}
	defer gr.Close()

	tr := tar.NewReader(gr)
	for {
		header, err := tr.Next()
		if err == io.EOF {
			break
		}
		if err != nil {
			return nil, err
		}

		base := filepath.Base(header.Name)
		if base == binName && (header.Typeflag == tar.TypeReg || header.Typeflag == tar.TypeRegA) {
			return io.ReadAll(tr)
		}
	}

	return nil, fmt.Errorf("binary %s not found in archive", binName)
}

func extractBinaryFromZip(data []byte, binName string) ([]byte, error) {
	zr, err := zip.NewReader(bytes.NewReader(data), int64(len(data)))
	if err != nil {
		return nil, err
	}

	for _, f := range zr.File {
		if filepath.Base(f.Name) == binName || filepath.Base(f.Name) == binName+".exe" {
			rc, err := f.Open()
			if err != nil {
				return nil, err
			}
			defer rc.Close()
			return io.ReadAll(rc)
		}
	}

	return nil, fmt.Errorf("binary %s not found in zip", binName)
}
