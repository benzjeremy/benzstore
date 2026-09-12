package store

import (
	_ "embed"
	"encoding/json"
	"fmt"
	"io"
	"net/http"
	"os"
	"os/exec"
	"path/filepath"
	"strings"
	"time"

	"github.com/benzjeremy/benzstore/internal/config"
)

//go:embed default_feed.json
var embeddedDefaultFeed []byte

type Client struct {
	feedURL    string
	httpClient *http.Client
}

func NewClient(feedURL string) *Client {
	if feedURL == "" {
		feedURL = config.DefaultFeedURL
	}
	return &Client{
		feedURL: feedURL,
		httpClient: &http.Client{
			Timeout: 8 * time.Second,
		},
	}
}

func (c *Client) FetchFeed() (*Feed, error) {
	cacheDir, _ := config.GetStoreCacheDir()
	cacheFile := filepath.Join(cacheDir, "feed_cache.json")

	// 1. Try fetching remote feed
	req, err := http.NewRequest("GET", c.feedURL, nil)
	if err == nil {
		req.Header.Set("User-Agent", "BenzStore-Desktop/1.0")
		resp, err := c.httpClient.Do(req)
		if err == nil && resp.StatusCode == http.StatusOK {
			defer resp.Body.Close()
			data, err := io.ReadAll(resp.Body)
			if err == nil && len(data) > 0 {
				var feed Feed
				if err := json.Unmarshal(data, &feed); err == nil {
					_ = os.WriteFile(cacheFile, data, 0644)
					c.enrichInstalledStatus(&feed)
					return &feed, nil
				}
			}
		}
	}

	// 2. Fallback to cached feed
	if data, err := os.ReadFile(cacheFile); err == nil && len(data) > 0 {
		var feed Feed
		if err := json.Unmarshal(data, &feed); err == nil {
			c.enrichInstalledStatus(&feed)
			return &feed, nil
		}
	}

	// 3. Fallback to embedded default feed
	if len(embeddedDefaultFeed) > 0 {
		var feed Feed
		if err := json.Unmarshal(embeddedDefaultFeed, &feed); err == nil {
			c.enrichInstalledStatus(&feed)
			return &feed, nil
		}
	}

	return nil, fmt.Errorf("feed could not be loaded")
}

func (c *Client) enrichInstalledStatus(feed *Feed) {
	userBin, _ := config.GetUserBinDir()
	reg, _ := LoadRegistry()

	for i := range feed.Apps {
		app := &feed.Apps[i]

		// 1. Check registry first
		if reg != nil {
			if entry, ok := reg.Apps[app.ID]; ok {
				if fi, err := os.Stat(entry.BinaryPath); err == nil && !fi.IsDir() {
					app.IsInstalled = true
					app.BinaryPath = entry.BinaryPath
					app.InstalledVersion = entry.Version
					continue
				}
			}
		}

		// Determine expected binary name
		binName := ""
		for _, v := range app.Versions {
			if dl, ok := v.Downloads["linux"]; ok && dl.BinName != "" {
				binName = dl.BinName
				break
			}
		}
		if binName == "" {
			parts := strings.Split(app.ID, ".")
			binName = parts[len(parts)-1]
		}

		// Check ~/.local/bin/<binName>
		localPath := filepath.Join(userBin, binName)
		if fi, err := os.Stat(localPath); err == nil && !fi.IsDir() {
			app.IsInstalled = true
			app.BinaryPath = localPath
			app.InstalledVersion = getAppVersion(localPath)
			continue
		}

		// Check system PATH
		if sysPath, err := exec.LookPath(binName); err == nil {
			app.IsInstalled = true
			app.BinaryPath = sysPath
			app.InstalledVersion = getAppVersion(sysPath)
		}
	}
}

func getAppVersion(binPath string) string {
	cmd := exec.Command(binPath, "--version")
	out, err := cmd.Output()
	if err == nil {
		fields := strings.Fields(string(out))
		if len(fields) > 0 {
			for _, f := range fields {
				if strings.HasPrefix(f, "v") || strings.Contains(f, ".") {
					return strings.Trim(f, "v,;")
				}
			}
		}
	}
	return "installed"
}
