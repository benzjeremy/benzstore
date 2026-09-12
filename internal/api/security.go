package api

import (
	"crypto/rand"
	"encoding/hex"
	"net/http"
	"strings"
)

type SecurityManager struct {
	SessionToken string
}

func NewSecurityManager() *SecurityManager {
	tokenBytes := make([]byte, 32)
	_, _ = rand.Read(tokenBytes)
	return &SecurityManager{
		SessionToken: hex.EncodeToString(tokenBytes),
	}
}

// Middleware guards against DNS Rebinding and CSRF attacks (Zero-Dummy-Security)
func (sm *SecurityManager) Middleware(next http.Handler) http.Handler {
	return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		host := r.Host
		// Anti-DNS-Rebinding
		if !strings.HasPrefix(host, "127.0.0.1") && !strings.HasPrefix(host, "localhost") {
			http.Error(w, "Forbidden: Invalid Host header (Anti-DNS-Rebinding Protection)", http.StatusForbidden)
			return
		}

		// Anti-CSRF on mutations
		if r.Method == http.MethodPost || r.Method == http.MethodDelete || r.Method == http.MethodPut {
			origin := r.Header.Get("Origin")
			if origin != "" && !strings.Contains(origin, "127.0.0.1") && !strings.Contains(origin, "localhost") {
				http.Error(w, "Forbidden: Invalid Origin header (Anti-CSRF Protection)", http.StatusForbidden)
				return
			}

			// Validate session token
			token := r.Header.Get("X-BenzStore-Token")
			if token == "" {
				token = r.URL.Query().Get("token")
			}
			if token != sm.SessionToken {
				http.Error(w, "Unauthorized: Invalid or missing BenzStore token", http.StatusUnauthorized)
				return
			}
		}

		// Set strict security headers
		w.Header().Set("X-Content-Type-Options", "nosniff")
		w.Header().Set("X-Frame-Options", "DENY")
		w.Header().Set("Referrer-Policy", "no-referrer")

		next.ServeHTTP(w, r)
	})
}
