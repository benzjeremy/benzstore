package store

import (
	"strconv"
	"strings"
)

// CompareVersions compares two version strings (e.g. "1.0", "v2.1", "1.2.3").
// Returns -1 if v1 < v2, 0 if v1 == v2, 1 if v1 > v2.
func CompareVersions(v1, v2 string) int {
	clean1 := strings.TrimLeft(strings.TrimSpace(v1), "vV")
	clean2 := strings.TrimLeft(strings.TrimSpace(v2), "vV")

	parts1 := splitVersionParts(clean1)
	parts2 := splitVersionParts(clean2)

	maxLen := len(parts1)
	if len(parts2) > maxLen {
		maxLen = len(parts2)
	}

	for i := 0; i < maxLen; i++ {
		var n1, n2 int64
		if i < len(parts1) {
			n1, _ = strconv.ParseInt(parts1[i], 10, 64)
		}
		if i < len(parts2) {
			n2, _ = strconv.ParseInt(parts2[i], 10, 64)
		}
		if n1 < n2 {
			return -1
		}
		if n1 > n2 {
			return 1
		}
	}
	return 0
}

func splitVersionParts(v string) []string {
	var parts []string
	var curr strings.Builder
	for _, r := range v {
		if r == '.' || r == '-' {
			if curr.Len() > 0 {
				parts = append(parts, curr.String())
				curr.Reset()
			}
		} else if r >= '0' && r <= '9' {
			curr.WriteRune(r)
		} else {
			if curr.Len() > 0 {
				parts = append(parts, curr.String())
				curr.Reset()
			}
		}
	}
	if curr.Len() > 0 {
		parts = append(parts, curr.String())
	}
	return parts
}
