package api

import (
	"encoding/json"
	"fmt"
	"io/fs"
	"net"
	"net/http"
	"os/exec"

	"github.com/benzjeremy/benzstore/internal/store"
)

type Server struct {
	client    *store.Client
	installer *Installer
	security  *SecurityManager
	staticFS  fs.FS
	listener  net.Listener
	port      int
}

func NewServer(client *store.Client, staticFS fs.FS) (*Server, error) {
	ln, err := net.Listen("tcp", "127.0.0.1:0")
	if err != nil {
		return nil, fmt.Errorf("failed to bind local port: %w", err)
	}

	port := ln.Addr().(*net.TCPAddr).Port
	sec := NewSecurityManager()
	inst := NewInstaller(client)

	return &Server{
		client:    client,
		installer: inst,
		security:  sec,
		staticFS:  staticFS,
		listener:  ln,
		port:      port,
	}, nil
}

func (s *Server) Port() int {
	return s.port
}

func (s *Server) URL() string {
	return fmt.Sprintf("http://127.0.0.1:%d/?token=%s", s.port, s.security.SessionToken)
}

func (s *Server) Start() error {
	mux := http.NewServeMux()

	mux.HandleFunc("/api/feed", s.handleGetFeed)
	mux.HandleFunc("/api/install", s.handleInstall)
	mux.HandleFunc("/api/uninstall", s.handleUninstall)
	mux.HandleFunc("/api/launch", s.handleLaunch)

	fileServer := http.FileServer(http.FS(s.staticFS))
	mux.Handle("/", fileServer)

	handler := s.security.Middleware(mux)
	return http.Serve(s.listener, handler)
}

func (s *Server) handleGetFeed(w http.ResponseWriter, r *http.Request) {
	feed, err := s.client.FetchFeed()
	if err != nil {
		http.Error(w, err.Error(), http.StatusInternalServerError)
		return
	}
	w.Header().Set("Content-Type", "application/json")
	_ = json.NewEncoder(w).Encode(feed)
}

type InstallRequest struct {
	AppID   string `json:"app_id"`
	Version string `json:"version"`
	Force   bool   `json:"force,omitempty"`
}

func (s *Server) handleInstall(w http.ResponseWriter, r *http.Request) {
	if r.Method != http.MethodPost {
		http.Error(w, "Method not allowed", http.StatusMethodNotAllowed)
		return
	}

	var req InstallRequest
	if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
		http.Error(w, "Invalid JSON body", http.StatusBadRequest)
		return
	}

	if err := s.installer.InstallApp(req.AppID, req.Version, req.Force); err != nil {
		w.Header().Set("Content-Type", "application/json")
		w.WriteHeader(http.StatusBadRequest)
		_ = json.NewEncoder(w).Encode(map[string]interface{}{
			"success": false,
			"message": err.Error(),
		})
		return
	}

	w.Header().Set("Content-Type", "application/json")
	_ = json.NewEncoder(w).Encode(map[string]interface{}{
		"success": true,
		"message": fmt.Sprintf("App %s erfolgreich eingerichtet", req.AppID),
	})
}

type UninstallRequest struct {
	AppID string `json:"app_id"`
}

func (s *Server) handleUninstall(w http.ResponseWriter, r *http.Request) {
	if r.Method != http.MethodPost {
		http.Error(w, "Method not allowed", http.StatusMethodNotAllowed)
		return
	}

	var req UninstallRequest
	if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
		http.Error(w, "Invalid JSON body", http.StatusBadRequest)
		return
	}

	if err := s.installer.UninstallApp(req.AppID); err != nil {
		http.Error(w, err.Error(), http.StatusInternalServerError)
		return
	}

	w.Header().Set("Content-Type", "application/json")
	_ = json.NewEncoder(w).Encode(map[string]interface{}{
		"success": true,
		"message": fmt.Sprintf("App %s deinstalliert", req.AppID),
	})
}

type LaunchRequest struct {
	AppID string `json:"app_id"`
}

func (s *Server) handleLaunch(w http.ResponseWriter, r *http.Request) {
	if r.Method != http.MethodPost {
		http.Error(w, "Method not allowed", http.StatusMethodNotAllowed)
		return
	}

	var req LaunchRequest
	if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
		http.Error(w, "Invalid JSON body", http.StatusBadRequest)
		return
	}

	feed, _ := s.client.FetchFeed()
	if feed != nil {
		for _, app := range feed.Apps {
			if app.ID == req.AppID && app.BinaryPath != "" {
				cmd := exec.Command(app.BinaryPath)
				if err := cmd.Start(); err != nil {
					http.Error(w, "Failed to launch: "+err.Error(), http.StatusInternalServerError)
					return
				}
				w.Header().Set("Content-Type", "application/json")
				_ = json.NewEncoder(w).Encode(map[string]interface{}{"success": true})
				return
			}
		}
	}

	http.Error(w, "App not installed or binary not found", http.StatusNotFound)
}
