# 📦 BenzStore

[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)
[![Go Version](https://img.shields.io/badge/Go-1.22+-00ADD8?logo=go)](https://golang.org)
[![Android](https://img.shields.io/badge/Android-APK%20(Native)-3DDC84?logo=android)](https://benzjeremy.github.io/benzstore/)
[![Website](https://img.shields.io/badge/Web-Showcase-brightgreen)](https://benzjeremy.github.io/benzstore/)

> **Unified, privacy-first AppStore for Android and PC (Linux & Windows)** by Jeremy Benz.
> Zero telemetry, no JAR compilation overhead (`jarsigner` eliminated), verified cryptographic SHA-256 integrity, single-click install for Latest or any specific version.

---

## 🌟 Highlights & Philosophy

- **📱 Dual-Platform Support (Android & PC):** Native Android APK client with built-in `PackageInstaller` integration and lightweight Go desktop shell for PC (WebKitGTK 4.1 on Linux, Chrome/Edge app-mode on Windows).
- **🚀 Goodbye JAR Compiling Overhead:** Unlike traditional F-Droid repositories that demand building and signing nested `.jar` files on every release, BenzStore operates directly with a clean, cryptographically hashed JSON manifest and direct binaries on the `content` branch.
- **🛡️ Zero-Dummy-Security (Verified SHA-256):** Every single binary and APK is verified against its cryptographic SHA-256 checksum before execution or installation. Corrupted or tampered downloads are rejected immediately.
- **🎯 Full Version Picker:** Choose between installing **Latest** or any specific past release (`v2.2`, `v2.1`, `v1.0`) with instant changelog inspection.
- **⚡ Single-Binary Linux Deliveries:** Clean standalone executables installed to `~/.local/bin` with automatic `.desktop` file registration.
- **🔒 100% Local-First & Zero Telemetry:** No tracking, no user profiling, no analytics SDKs, no Google Play Services dependency.

---

## 🏗️ Repository Architecture

BenzStore uses a clean multi-branch architecture:

```text
benzstore/
├── main Branch                 <- Core Go desktop/CLI engine & native Android app source
├── content Branch              <- Single-binary repository:
│   ├── android/                <- Native APKs (learn_2.2.apk, wetter_1.2.apk, ...)
│   ├── linux/                  <- Standalone Linux ELF binaries (untis-go_2.3, docklite_1.2.1, ...)
│   ├── windows/                <- Windows PE executables (untis-go_2.3.exe, ...)
│   └── feed.json               <- Manifest with verified SHA-256 digests
└── web Branch                  <- Bilingual showcase landingpage & Web-Store cockpit
```

---

## 🚀 Installation & Usage

### PC (Linux & Windows)

#### Via Go Install:
```bash
go install github.com/benzjeremy/benzstore@latest
benzstore
```

#### CLI Operations:
```bash
# List available applications and local status
benzstore list

# Install latest version of an app
benzstore install untis-go

# Install a specific version
benzstore install untis-go -v 2.2

# Uninstall an application
benzstore uninstall docklite

# Start daemon without GUI window
benzstore serve
```

### Android

Download the native **BenzStore APK** directly:
- Direct Download: [`benzstore-v1.0.apk`](https://raw.githubusercontent.com/benzjeremy/benzstore/content/android/benzstore/benzstore_1.0.apk)
- Web Portal: [`https://benzjeremy.github.io/benzstore/`](https://benzjeremy.github.io/benzstore/)

---

## 👥 Contributors & Credits

- **Lead Engineer & Architect:** Jeremy Benz ([@benzjeremy](https://github.com/benzjeremy) & [@jbenz1706](https://github.com/jbenz1706))
- **AI Pair Programming:** Google Antigravity
- **Copyright:** © 2026 Jeremy Benz

---

## 📄 License

This project is open-source software licensed under the **GNU General Public License, Version 3 (GPL-3.0)**.
See the official [LICENSE](LICENSE) file for terms.
