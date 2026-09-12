package store

type DesktopEntryConfig struct {
	Name       string `json:"name"`
	Comment    string `json:"comment"`
	Exec       string `json:"exec"`
	Terminal   bool   `json:"terminal"`
	Categories string `json:"categories"`
}

type DownloadAsset struct {
	URL          string              `json:"url"`
	Filename     string              `json:"filename"`
	BinName      string              `json:"bin_name,omitempty"`
	Type         string              `json:"type,omitempty"`
	SHA256       string              `json:"sha256"`
	Size         int64               `json:"size"`
	DesktopEntry *DesktopEntryConfig `json:"desktop_entry,omitempty"`
}

type Version struct {
	Version      string                   `json:"version"`
	VersionCode  int                      `json:"version_code,omitempty"`
	ReleaseDate  string                   `json:"release_date"`
	ChangelogDE  string                   `json:"changelog_de,omitempty"`
	ChangelogEN  string                   `json:"changelog_en,omitempty"`
	Downloads    map[string]DownloadAsset `json:"downloads"`
}

type App struct {
	ID             string    `json:"id"`
	Name           string    `json:"name"`
	SummaryDE      string    `json:"summary_de,omitempty"`
	SummaryEN      string    `json:"summary_en,omitempty"`
	DescriptionDE  string    `json:"description_de,omitempty"`
	DescriptionEN  string    `json:"description_en,omitempty"`
	Author         string    `json:"author"`
	License        string    `json:"license"`
	Website        string    `json:"website"`
	Source         string    `json:"source"`
	Icon           string    `json:"icon"`
	Categories     []string  `json:"categories"`
	Platforms      []string  `json:"platforms"`
	LatestVersion  string    `json:"latest_version"`
	Versions       []Version `json:"versions"`

	// Client-side computed state
	IsInstalled      bool   `json:"is_installed,omitempty"`
	InstalledVersion string `json:"installed_version,omitempty"`
	BinaryPath       string `json:"binary_path,omitempty"`
}

type StoreInfo struct {
	Name        string `json:"name"`
	Tagline     string `json:"tagline"`
	Description string `json:"description"`
	Version     string `json:"version"`
	Homepage    string `json:"homepage"`
	FeedURL     string `json:"feed_url"`
	Author      string `json:"author"`
	License     string `json:"license"`
	LastUpdated string `json:"last_updated"`
}

type Category struct {
	ID     string `json:"id"`
	NameDE string `json:"name_de"`
	NameEN string `json:"name_en"`
}

type Feed struct {
	Store      StoreInfo  `json:"store"`
	Categories []Category `json:"categories"`
	Apps       []App      `json:"apps"`
}
