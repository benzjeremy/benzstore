/**
 * BenzStore Web Client & Showcase
 * Author: Jeremy Benz
 * License: GNU GPL-3.0
 */

const I18N = {
  de: {
    nav_features: "Features",
    nav_install: "Installation",
    nav_download: "Downloads",
    nav_webapp: "🚀 Web-App",
    nav_wiki: "Wiki & Docs",
    nav_about: "Über",
    hero_title: "Der moderne AppStore",
    hero_sub: "für freie Android- & PC-Software.",
    hero_desc: "Keine quälende JAR-Signierung und keine überladenen Store-Clients. BenzStore liefert freie Open-Source Software direkt als verifizierte Single-Binaries und native APKs. Mit echtem Version-Picker, automatisierter Linux/Windows-Paketverwaltung und kryptografischer SHA-256 Integritätsprüfung ab Werk.",
    btn_launch_webapp: "Web-App starten (Live AppStore)",
    btn_dl_apk: "BenzStore APK herunterladen",
    btn_install_pc: "Desktop & CLI Installation",
    quick_install: "Schnellinstallation via Go:",
    sec_feat_badge: "Architektur & Philosophie",
    sec_feat_title: "Warum ein eigener AppStore?",
    sec_feat_sub: "Maximale Privatsphäre, strikte kryptografische Absicherung und echte Entwickler-Freiheit.",
    f1_title: "Zero-Dummy Security",
    f1_desc: "Militärisch gehärtete Produktionssicherheit: AES-256-GCM Verschlüsselung, PBKDF2 mit 100.000 Iterationen, CSRF- und DNS-Rebinding-Schutz auf allen lokalen APIs.",
    f2_title: "Echte SHA-256 Integrität",
    f2_desc: "Jedes APK- und Desktop-Paket wird vor der Installation auf Byte-Ebene mit dem Manifest abgeglichen. Manipulationen werden sofort erkannt und abgewiesen.",
    f3_title: "Multi-Plattform Native",
    f3_desc: "Ein einheitliches Ökosystem für Android (APK), Linux (WebKitGTK CGO / Standalone) und Windows (App-Mode) ohne Electron-Overhead.",
    f4_title: "Freier Version-Picker",
    f4_desc: "Installiere entweder die neueste Version oder wähle gezielt einen beliebigen früheren Release-Stand mit vollständigem Changelog und Downgrade-Schutz.",
    f5_title: "F-Droid Repo Integration",
    f5_desc: "Nahtlose Synchronisation mit dem Jeremy Benz F-Droid Repository (myfdroid). Signierte JAR-Indexe und direkter APK-Download ohne Drittanbieter-Tracking.",
    f6_title: "100% Local-First & Zero Telemetrie",
    f6_desc: "Keine Benutzerkonten, keine Cloud-Zwänge, keine Werbetracker. Volle Funktionalität offline und absolute Datenhoheit auf deinen Geräten.",
    sec_install_badge: "Setup Guides",
    sec_install_title: "Installation auf deinen Geräten",
    sec_install_sub: "Wähle deine Zielplattform für die direkte Installation.",
    inst_android_desc: "Installiere den nativen Store-Client direkt auf dein Smartphone oder Tablet (Android 8.0+).",
    inst_linux_desc: "Nativer Desktop-Client via WebKitGTK oder reines CLI-Tool für dein Terminal.",
    inst_windows_desc: "Schlanker App-Mode via Edge/Chrome. Keine DLL-Hölle, pure Go-Executable.",
    sec_dl_badge: "Binaries & Hashes",
    sec_dl_title: "Offizielle Veröffentlichungen",
    sec_dl_sub: "Kryptografisch verifizierte Prüfsummen für alle Binärdateien.",
    about_title: "Mitwirkende & Autoren",
    author_role: "Projektgründer & Lead Developer",
    about_text: "Software-Entwickler aus Nordrhein-Westfalen (Deutschland). Fokus auf Go, native Desktop-Shells, Systemprogrammierung und kompromisslose Privatsphäre ohne Telemetrie.",
    license_title: "Freie & Open Source Software",
    license_text: "BenzStore und alle bereitgestellten Anwendungen stehen unter der GNU General Public License, Version 3 (GPL-3.0). Quellcode, Transparenz und uneingeschränkte Nutzerkontrolle sind unverhandelbare Grundsätze.",
    foot_webapp: "🚀 Web-App",
    foot_wiki: "Web-Wiki",
    foot_issues: "Fehler melden",
    foot_license: "Lizenz",
    copied: "Kopiert!"
  },
  en: {
    nav_features: "Features",
    nav_install: "Installation",
    nav_download: "Downloads",
    nav_webapp: "🚀 Web App",
    nav_wiki: "Wiki & Docs",
    nav_about: "About",
    hero_title: "The Modern AppStore",
    hero_sub: "for free Android & PC software.",
    hero_desc: "No more painful JAR signing cycles and bloated store clients. BenzStore delivers free open-source software directly as verified single-binaries and native APKs. Featuring a true version picker, automated Linux/Windows package management, and out-of-the-box SHA-256 integrity verification.",
    btn_launch_webapp: "Launch Web App (Live AppStore)",
    btn_dl_apk: "Download BenzStore APK",
    btn_install_pc: "Desktop & CLI Installation",
    quick_install: "Quick install via Go:",
    sec_feat_badge: "Architecture & Philosophy",
    sec_feat_title: "Why an Independent AppStore?",
    sec_feat_sub: "Maximum privacy, rigorous cryptographic security, and true developer sovereignty.",
    f1_title: "Zero-Dummy Security",
    f1_desc: "Hardened production security: AES-256-GCM encryption, PBKDF2 with 100,000 iterations, CSRF and DNS-rebinding protection across all local APIs.",
    f2_title: "True SHA-256 Integrity",
    f2_desc: "Every package is verified byte-by-byte against the cryptographic manifest before installation. Any tampering is immediately rejected.",
    f3_title: "Native Multi-Platform",
    f3_desc: "A unified ecosystem for Android (APK), Linux (WebKitGTK CGO / standalone) and Windows (app mode) without Electron overhead.",
    f4_title: "True Version Picker",
    f4_desc: "Install either the latest stable build or choose any previous release milestone with full release notes and downgrade protection.",
    f5_title: "F-Droid Repo Integration",
    f5_desc: "Seamless synchronization with the Jeremy Benz F-Droid repository (myfdroid). Signed JAR indexes and direct APK downloads without third-party tracking.",
    f6_title: "100% Local-First & Zero Telemetry",
    f6_desc: "No user accounts, no cloud lock-in, no advertising trackers. Full offline functionality and absolute data sovereignty on your devices.",
    sec_install_badge: "Setup Guides",
    sec_install_title: "Device Installation Guides",
    sec_install_sub: "Choose your target platform for direct installation.",
    inst_android_desc: "Install the native store client directly onto your smartphone or tablet (Android 8.0+).",
    inst_linux_desc: "Native desktop client via WebKitGTK or pure CLI tool for your terminal.",
    inst_windows_desc: "Lightweight app mode via Edge/Chrome. No DLL dependency hell, pure Go executable.",
    sec_dl_badge: "Binaries & Hashes",
    sec_dl_title: "Official Releases",
    sec_dl_sub: "Cryptographically verified checksums for all binary packages.",
    about_title: "Contributors & Authors",
    author_role: "Project Founder & Lead Developer",
    about_text: "Software engineer from North Rhine-Westphalia (Germany). Focused on Go, native desktop shells, systems programming, and uncompromising telemetry-free privacy.",
    license_title: "Free & Open Source Software",
    license_text: "BenzStore and all hosted applications are licensed under the GNU General Public License, Version 3 (GPL-3.0). Source code, auditability, and user sovereignty are non-negotiable.",
    foot_webapp: "🚀 Web App",
    foot_wiki: "Web Wiki",
    foot_issues: "Report Issue",
    foot_license: "License",
    copied: "Copied!"
  }
};

let currentLang = localStorage.getItem('site_lang') || 'de';

// Initialize
document.addEventListener('DOMContentLoaded', () => {
  applyLanguage(currentLang);
});

// Language Management
function toggleLanguage() {
  currentLang = currentLang === 'de' ? 'en' : 'de';
  localStorage.setItem('site_lang', currentLang);
  applyLanguage(currentLang);
}

function applyLanguage(lang) {
  const dict = I18N[lang] || I18N.de;
  const langToggle = document.getElementById('langToggle');
  if (langToggle) {
    langToggle.textContent = lang === 'de' ? 'EN' : 'DE';
  }

  document.querySelectorAll('[data-i18n]').forEach(el => {
    const key = el.getAttribute('data-i18n');
    if (dict[key]) {
      el.textContent = dict[key];
    }
  });

  document.querySelectorAll('[data-i18n-placeholder]').forEach(el => {
    const key = el.getAttribute('data-i18n-placeholder');
    if (dict[key]) {
      el.setAttribute('placeholder', dict[key]);
    }
  });

  document.documentElement.lang = lang;
}

// Theme Management
function toggleTheme() {
  const current = document.documentElement.getAttribute('data-theme') || 'dark';
  const next = current === 'dark' ? 'light' : 'dark';
  document.documentElement.setAttribute('data-theme', next);
  localStorage.setItem('site_theme', next);
  const themeToggle = document.getElementById('themeToggle');
  if (themeToggle) {
    themeToggle.textContent = next === 'dark' ? '🌙' : '☀️';
  }
}

// Copy Helper
function copyCmd(btn, text) {
  navigator.clipboard.writeText(text).then(() => {
    const orig = btn.textContent;
    btn.textContent = I18N[currentLang].copied || "Kopiert!";
    setTimeout(() => { btn.textContent = orig; }, 2000);
  });
}
