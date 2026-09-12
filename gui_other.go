//go:build !windows && (!linux || !cgo)

package main

import (
	"log"
	"os/exec"
	"runtime"
)

func LaunchGUI(title, url string, width, height int) {
	log.Printf("[GUI] Öffne BenzStore im Standard-Browser: %s\n", url)
	switch runtime.GOOS {
	case "darwin":
		_ = exec.Command("open", url).Start()
	default:
		_ = exec.Command("xdg-open", url).Start()
	}
}
