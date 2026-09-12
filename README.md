# 📦 BenzStore Content Repository

This branch (`content`) hosts all standalone binaries, packages, and APKs for the BenzStore application ecosystem by **Jeremy Benz**.

## 📁 Directory Architecture

```text
.
├── android/                   <- Native Android Applications (APK)
│   ├── benzstore/             <- benzstore_1.0.apk
│   ├── learn/                 <- learn_1.0.apk ... learn_2.2.apk
│   └── wetter/                <- wetter_1.0.apk ... wetter_1.2.apk
│
├── linux/                     <- Linux Single Binaries (no tar.gz bloat!)
│   ├── benzstore/             <- benzstore_1.0
│   ├── docklite/              <- docklite_1.2.1
│   ├── spotify-screensaver/   <- spotify-screensaver_1.4
│   └── untis-go/              <- untis-go_2.2, untis-go_2.3
│
├── windows/                   <- Windows Executables (.exe)
│   ├── benzstore/             <- benzstore_1.0.exe
│   ├── docklite/              <- docklite_1.2.1.exe
│   ├── spotify-screensaver/   <- spotify-screensaver_1.4.exe
│   └── untis-go/              <- untis-go_2.2.exe, untis-go_2.3.exe
│
├── feed.json                  <- Verified JSON Manifest with SHA-256 Hashes
└── apps.json                  <- Mirrored Endpoint
```

## 🛡️ Security
All binaries and packages have cryptographic SHA-256 checksums recorded in `feed.json`. BenzStore clients verify integrity automatically before executing installations.

## 📄 License
All hosted software is released under the **GNU General Public License v3.0**.
