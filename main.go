package main

import (
	"embed"
	"fmt"
	"io/fs"
	"log"
	"os"
	"strings"

	"github.com/benzjeremy/benzstore/internal/api"
	"github.com/benzjeremy/benzstore/internal/store"
)

//go:embed web/*
var webFS embed.FS

func main() {
	if len(os.Args) > 1 {
		switch os.Args[1] {
		case "list":
			runList()
			return
		case "install":
			runInstall(os.Args[2:])
			return
		case "uninstall":
			runUninstall(os.Args[2:])
			return
		case "serve":
			runServe(os.Args[2:])
			return
		case "version", "--version", "-v":
			fmt.Println("BenzStore v1.0 (Lead Engineer: Jeremy Benz)")
			return
		case "help", "--help", "-h":
			printHelp()
			return
		}
	}

	runGUI()
}

func printHelp() {
	fmt.Println(`BenzStore – Unified Privacy-First AppStore for Android & PC
Author: Jeremy Benz (@benzjeremy) • GNU GPL-3.0

Verwendung:
  benzstore                        Startet die native Desktop-Oberfläche (WebKitGTK)
  benzstore list                   Listet alle verfügbaren Anwendungen und deren Status auf
  benzstore install <app_id>       Installiert die neueste Version einer App (z. B. untis-go)
  benzstore install <app_id> -v X  Installiert eine gezielte Version (z. B. -v 2.2)
  benzstore uninstall <app_id>     Deinstalliert eine installierte App
  benzstore serve                  Startet den lokalen Daemon ohne Desktop-Fenster
  benzstore --version              Zeigt die aktuelle Version an`)
}

func runList() {
	client := store.NewClient("")
	feed, err := client.FetchFeed()
	if err != nil {
		log.Fatalf("Fehler beim Laden des Katalogs: %v\n", err)
	}

	fmt.Println("\n📦 VERFÜGBARE ANWENDUNGEN IM BENZSTORE:")
	fmt.Println(strings.Repeat("─", 75))
	for _, app := range feed.Apps {
		status := "Nicht installiert"
		if app.IsInstalled {
			status = fmt.Sprintf("✅ Installiert (v%s in %s)", app.InstalledVersion, app.BinaryPath)
		}
		plat := strings.Join(app.Platforms, ", ")
		fmt.Printf("• %-26s [v%-5s] [%-16s] %s\n", app.Name, app.LatestVersion, plat, status)
		fmt.Printf("  ID: %-24s -> %s\n\n", app.ID, app.SummaryDE)
	}
	fmt.Println(strings.Repeat("─", 75))
}

func runInstall(args []string) {
	if len(args) == 0 {
		fmt.Println("Fehler: Bitte gib eine App-ID oder einen Namen an (z. B. benzstore install untis-go)")
		os.Exit(1)
	}

	appID := args[0]
	verFlag := ""
	forceFlag := false
	for i := 1; i < len(args); i++ {
		if (args[i] == "-v" || args[i] == "--version") && i+1 < len(args) {
			verFlag = args[i+1]
			i++
		} else if args[i] == "-f" || args[i] == "--force" {
			forceFlag = true
		}
	}

	if !strings.Contains(appID, ".") {
		appID = "com.benzjeremy." + appID
	}

	client := store.NewClient("")
	installer := api.NewInstaller(client)

	fmt.Printf("==> Starte Installation von %s (Version: %s)...\n", appID, verFlag)
	if err := installer.InstallApp(appID, verFlag, forceFlag); err != nil {
		log.Fatalf("❌ Installationsfehler: %v\n", err)
	}
	fmt.Println("✅ Erfolg: Anwendung wurde installiert und einsatzbereit eingerichtet!")
}

func runUninstall(args []string) {
	if len(args) == 0 {
		fmt.Println("Fehler: Bitte gib eine App-ID oder einen Namen an (z. B. benzstore uninstall untis-go)")
		os.Exit(1)
	}

	appID := args[0]
	if !strings.Contains(appID, ".") {
		appID = "com.benzjeremy." + appID
	}

	client := store.NewClient("")
	installer := api.NewInstaller(client)

	if err := installer.UninstallApp(appID); err != nil {
		log.Fatalf("Fehler beim Deinstallieren: %v\n", err)
	}
}

func runServe(args []string) {
	subFS, err := fs.Sub(webFS, "web")
	if err != nil {
		log.Fatalf("Fehler beim Laden des Web-Assets: %v\n", err)
	}

	client := store.NewClient("")
	srv, err := api.NewServer(client, subFS)
	if err != nil {
		log.Fatalf("Fehler beim Starten des Servers: %v\n", err)
	}

	fmt.Printf("🚀 BenzStore Daemon läuft auf: %s\n", srv.URL())
	if err := srv.Start(); err != nil {
		log.Fatal(err)
	}
}

func runGUI() {
	subFS, err := fs.Sub(webFS, "web")
	if err != nil {
		log.Fatalf("Fehler beim Laden des Web-Assets: %v\n", err)
	}

	client := store.NewClient("")
	srv, err := api.NewServer(client, subFS)
	if err != nil {
		log.Fatalf("Fehler beim Starten des Servers: %v\n", err)
	}

	go func() {
		if err := srv.Start(); err != nil {
			log.Printf("[Server] Fehler: %v\n", err)
		}
	}()

	log.Printf("[BenzStore] Server gestartet auf %s\n", srv.URL())
	LaunchGUI("BenzStore – Unified AppStore", srv.URL(), 1100, 750)
}
