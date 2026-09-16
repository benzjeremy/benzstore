// Package api provides an HTTP server exposing application data.
//
// This file implements the *latest‑version* endpoint, which returns the
// most recent stable release for an application identified by its
// `app_id` query parameter.
//
// Example request:
//   GET /api/latest-version?app_id=com.benzjeremy.learn
// Example response (plain text):
//   2.2
//
// The handler queries the feed via the embedded client and scans for the
// matching app ID.  If the app is not found, a 400 Bad Request is sent.
package api

import (
    "net/http"
    "github.com/benzjeremy/benzstore/internal/store"
)

// handleLatestVersion writes the latest stable version for a given app id.
// The app id is passed as the query parameter "app_id".
// Example response: 200 OK with body "2.2"
// If app_id is missing or not found, returns 400 Bad Request.
func (s *Server) handleLatestVersion(w http.ResponseWriter, r *http.Request) {
    appID := r.URL.Query().Get("app_id")
    if appID == "" {
        http.Error(w, "missing app_id", http.StatusBadRequest)
        return
    }

    // Fetch the feed once; in a real system we might cache it.
    feed, err := s.client.FetchFeed()
    if err != nil {
        http.Error(w, "failed to load feed", http.StatusInternalServerError)
        return
    }

    // Search for the app.
    for _, app := range feed.Apps {
        if app.ID == appID {
            w.Header().Set("Content-Type", "text/plain")
            w.WriteHeader(http.StatusOK)
            w.Write([]byte(app.LatestVersion))
            return
        }
    }

    http.Error(w, "app not found", http.StatusBadRequest)
}
