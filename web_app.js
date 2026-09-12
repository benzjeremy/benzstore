/**
 * BenzStore Web Client & Showcase
 * Author: Jeremy Benz
 * License: GNU GPL-3.0
 */

const I18N = {
  de: {
    nav_features: "Features",
    nav_catalog: "App-Katalog",
    nav_download: "Download",
    nav_install: "Installation",
    nav_about: "Über",
    hero_title: "Der moderne AppStore",
    hero_sub: "für Android & PC.",
    hero_desc: "Schluss mit quälender JAR-Signierung und überladenen Store-Clients. BenzStore liefert freie Open-Source Software direkt als verifizierte Single-Binaries und native APKs. Mit echtem Version-Picker und kryptografischer SHA-256 Prüfung ab Werk.",
    btn_dl_apk: "BenzStore APK herunterladen",
    btn_dl_pc: "Linux Single Binary",
    btn_dl_win: "Windows Executable",
    quick_install: "Schnellinstallation via Go:",
    sec_arch: "Architektur & Philosophie",
    sec_feat_title: "Warum ein eigener Store?",
    f1_title: "Schluss mit JAR-Kompilierung",
    f1_desc: "Kein nerviges jarsigner mehr bei jedem Release. BenzStore nutzt direkt geprüfte Einzeldateien und JSON-Feeds auf dem Content-Branch.",
    f2_title: "Echte SHA-256 Integrität",
    f2_desc: "Zero-Dummy-Security: Jedes Paket wird vor der Installation auf Byte-Ebene mit dem Manifest abgeglichen. Manipulationen werden sofort abgewiesen.",
    f3_title: "Freier Version-Picker",
    f3_desc: "Installiere entweder die neueste Version (Latest) oder wähle gezielt einen beliebigen früheren Release-Stand mit vollständigem Changelog.",
    f4_title: "Linux Single Binaries",
    f4_desc: "Unter Linux landen reine Standalone-Binaries direkt in ~/.local/bin inklusive automatischer .desktop-Starter für dein Anwendungsmenü.",
    sec_catalog_sub: "Live Repository Feed",
    sec_catalog_title: "Alle Anwendungen im BenzStore",
    search_ph: "Apps filtern nach Name, Funktion...",
    filter_all: "Alle",
    filter_pc: "PC & Desktop",
    filter_android: "Android APKs",
    filter_tools: "Entwickler-Tools",
    filter_edu: "Bildung",
    sec_install_sub: "Setup Guides",
    sec_install_title: "Installation auf deinen Geräten",
    about_text: "Software-Entwickler aus Nordrhein-Westfalen (Deutschland). Fokus auf Go, native Desktop-Shells, Systemprogrammierung und kompromisslose Privatsphäre ohne Telemetrie.",
    license_text: "BenzStore und alle bereitgestellten Anwendungen stehen unter der GNU GPL-3.0. Quellcode, Transparenz und Nutzerkontrolle sind unverhandelbar.",
    btn_details: "Details & Versionen",
    btn_dl_version: "Herunterladen",
    select_ver_label: "Version auswählen:",
    size_label: "Größe",
    sha256_label: "Kryptografischer SHA-256 Prüfsummen-Hash:",
    no_apps_found: "Keine Anwendungen für diese Auswahl gefunden.",
    copied: "Kopiert!"
  },
  en: {
    nav_features: "Features",
    nav_catalog: "App Catalog",
    nav_download: "Download",
    nav_install: "Installation",
    nav_about: "About",
    hero_title: "The Modern AppStore",
    hero_sub: "for Android & PC.",
    hero_desc: "No more painful JAR signing cycles and bloated store clients. BenzStore delivers free open-source software directly as verified single-binaries and native APKs. Featuring a true version picker and out-of-the-box SHA-256 integrity verification.",
    btn_dl_apk: "Download BenzStore APK",
    btn_dl_pc: "Linux Single Binary",
    btn_dl_win: "Windows Executable",
    quick_install: "Quick install via Go:",
    sec_arch: "Architecture & Philosophy",
    sec_feat_title: "Why an Independent Store?",
    f1_title: "No More JAR Recompilations",
    f1_desc: "Eliminates jarsigner overhead on every release. BenzStore directly utilizes verified single files and automated JSON feeds on the content branch.",
    f2_title: "True SHA-256 Integrity",
    f2_desc: "Zero-Dummy-Security: Every package is checked byte-for-byte against the cryptographic manifest before installation. Tampering is rejected immediately.",
    f3_title: "True Version Picker",
    f3_desc: "Install either the latest stable build or choose any previous release milestone with complete release notes and changelogs.",
    f4_title: "Linux Single Binaries",
    f4_desc: "On Linux, pure standalone ELF binaries are installed directly into ~/.local/bin alongside automatic .desktop launchers for your app menu.",
    sec_catalog_sub: "Live Repository Feed",
    sec_catalog_title: "All Applications in BenzStore",
    search_ph: "Filter apps by name, purpose...",
    filter_all: "All",
    filter_pc: "PC & Desktop",
    filter_android: "Android APKs",
    filter_tools: "Dev Tools",
    filter_edu: "Education",
    sec_install_sub: "Setup Guides",
    sec_install_title: "Device Installation Guides",
    about_text: "Software engineer from North Rhine-Westphalia (Germany). Focused on Go, native desktop shells, systems programming, and uncompromising telemetry-free privacy.",
    license_text: "BenzStore and all supplied applications are licensed under the GNU GPL-3.0. Source code, auditability, and user sovereignty are non-negotiable.",
    btn_details: "Details & Versions",
    btn_dl_version: "Download",
    select_ver_label: "Select version:",
    size_label: "Size",
    sha256_label: "Cryptographic SHA-256 Checksum:",
    no_apps_found: "No applications found matching your criteria.",
    copied: "Copied!"
  }
};

let currentLang = localStorage.getItem('site_lang') || 'de';
let currentCategory = 'all';
let allApps = [];
let activeApp = null;

// Initialize
document.addEventListener('DOMContentLoaded', () => {
  applyLanguage(currentLang);
  loadCatalogData();
});

// Language Management
function toggleLanguage() {
  currentLang = currentLang === 'de' ? 'en' : 'de';
  localStorage.setItem('site_lang', currentLang);
  applyLanguage(currentLang);
  renderCatalog();
  if (activeApp) {
    openWebModal(activeApp.id);
  }
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

// Data Fetching
async function loadCatalogData() {
  try {
    const res = await fetch('./feed.json');
    if (!res.ok) throw new Error("Local feed fetch failed");
    const data = await res.json();
    allApps = data.apps || [];
  } catch (err) {
    console.warn("Falling back to raw GitHub content feed", err);
    try {
      const resRemote = await fetch('https://raw.githubusercontent.com/benzjeremy/benzstore/content/feed.json');
      const dataRemote = await resRemote.json();
      allApps = dataRemote.apps || [];
    } catch (e) {
      console.error("Could not load feed:", e);
    }
  }
  renderCatalog();
}

// Filtering
function setCatalogFilter(cat) {
  currentCategory = cat;
  document.querySelectorAll('.cat-pill').forEach(btn => {
    btn.classList.toggle('active', btn.getAttribute('data-cat') === cat);
  });
  renderCatalog();
}

function filterCatalog() {
  renderCatalog();
}

// Render Catalog Grid
function renderCatalog() {
  const grid = document.getElementById('catalogGrid');
  if (!grid) return;

  const query = (document.getElementById('catalogSearch')?.value || '').toLowerCase().trim();
  const filtered = allApps.filter(app => {
    // Category check
    if (currentCategory === 'pc' && !app.platforms.some(p => p === 'linux' || p === 'windows')) return false;
    if (currentCategory === 'android' && !app.platforms.includes('android')) return false;
    if (currentCategory !== 'all' && currentCategory !== 'pc' && currentCategory !== 'android') {
      if (!app.categories.includes(currentCategory)) return false;
    }

    // Search query
    if (query) {
      const name = (app.name || '').toLowerCase();
      const descDe = (app.description_de || '').toLowerCase();
      const descEn = (app.description_en || '').toLowerCase();
      const sumDe = (app.summary_de || '').toLowerCase();
      const sumEn = (app.summary_en || '').toLowerCase();
      return name.includes(query) || descDe.includes(query) || descEn.includes(query) || sumDe.includes(query) || sumEn.includes(query);
    }

    return true;
  });

  if (filtered.length === 0) {
    grid.innerHTML = `<div style="grid-column: 1/-1; text-align: center; padding: 40px; color: var(--text-muted);">${I18N[currentLang].no_apps_found}</div>`;
    return;
  }

  grid.innerHTML = filtered.map(app => {
    const summary = currentLang === 'de' ? (app.summary_de || app.description_de) : (app.summary_en || app.description_en);
    const platformBadges = app.platforms.map(p => {
      if (p === 'android') return '<span class="chip" style="background:var(--brand-green-bg);color:var(--brand-green);border-color:var(--brand-green)">Android</span>';
      if (p === 'linux') return '<span class="chip" style="background:var(--brand-blue-bg);color:var(--brand-blue);border-color:var(--brand-blue)">Linux</span>';
      if (p === 'windows') return '<span class="chip" style="background:rgba(168,85,247,0.15);color:#a855f7;border-color:#a855f7">Windows</span>';
      return '';
    }).join(' ');

    return `
      <div class="c-card">
        <div>
          <div class="c-card-top">
            <img src="${app.icon}" alt="${app.name}" class="c-card-icon" onerror="this.src='icon.png'">
            <div class="c-card-info">
              <div class="c-card-title-row">
                <span class="c-card-title">${app.name}</span>
                <span class="c-card-ver">v${app.latest_version}</span>
              </div>
              <div style="margin-top: 4px; display: flex; gap: 4px; flex-wrap: wrap;">
                ${platformBadges}
              </div>
            </div>
          </div>
          <p class="c-card-desc" style="margin-top: 12px;">${summary}</p>
        </div>
        <div class="c-card-actions">
          <button class="btn btn-sm btn-secondary" style="width: 100%;" onclick="openWebModal('${app.id}')">
            ${I18N[currentLang].btn_details} →
          </button>
        </div>
      </div>
    `;
  }).join('');
}

// Modal Detail View & Version Picker
function openWebModal(appId) {
  const app = allApps.find(a => a.id === appId);
  if (!app) return;
  activeApp = app;

  const modal = document.getElementById('webModal');
  const modalBody = document.getElementById('webModalBody');
  if (!modal || !modalBody) return;

  const desc = currentLang === 'de' ? app.description_de : app.description_en;
  const versions = app.versions || [];

  modalBody.innerHTML = `
    <div class="detail-hero">
      <img src="${app.icon}" alt="${app.name}" onerror="this.src='icon.png'">
      <div>
        <h2 style="font-size: 20px; font-weight: 700; margin-bottom: 4px;">${app.name}</h2>
        <div style="font-size: 12px; color: var(--text-muted); display: flex; gap: 10px; align-items: center; flex-wrap: wrap;">
          <span>👤 ${app.author}</span>
          <span>📄 ${app.license}</span>
          ${app.website ? `<a href="${app.website}" target="_blank" rel="noopener" class="btn-link">Website ↗</a>` : ''}
          ${app.source ? `<a href="${app.source}" target="_blank" rel="noopener" class="btn-link">GitHub ↗</a>` : ''}
        </div>
      </div>
    </div>

    <p style="font-size: 14px; color: var(--text-muted); line-height: 1.6; margin-bottom: 20px;">${desc}</p>

    <div class="version-picker-box">
      <label style="font-size: 13px; font-weight: 600; color: var(--text-main); display: block;">
        ${I18N[currentLang].select_ver_label}
      </label>
      <select id="modalVersionSelect" class="version-select" onchange="onModalVersionChange('${app.id}')">
        ${versions.map(v => `<option value="${v.version}">Version ${v.version} (${v.release_date})${v.version === app.latest_version ? ' – Latest' : ''}</option>`).join('')}
      </select>

      <div id="modalVersionDetails"></div>
    </div>
  `;

  modal.classList.add('open');
  onModalVersionChange(app.id);
}

function onModalVersionChange(appId) {
  const app = allApps.find(a => a.id === appId);
  if (!app) return;

  const sel = document.getElementById('modalVersionSelect');
  const targetVer = sel ? sel.value : app.latest_version;
  const verObj = (app.versions || []).find(v => v.version === targetVer) || (app.versions || [])[0];
  const container = document.getElementById('modalVersionDetails');
  if (!container || !verObj) return;

  const changelog = currentLang === 'de' ? (verObj.changelog_de || 'Keine Notizen verfügbar.') : (verObj.changelog_en || 'No release notes available.');
  const downloads = verObj.downloads || {};

  let dlHtml = '<div style="display: flex; flex-direction: column; gap: 10px; margin-top: 14px;">';

  if (downloads.android) {
    const d = downloads.android;
    const mb = (d.size / (1024 * 1024)).toFixed(2);
    const kb = (d.size / 1024).toFixed(0);
    const sizeStr = d.size > 1048576 ? `${mb} MB` : `${kb} KB`;
    dlHtml += `
      <div style="background: var(--bg-surface); padding: 12px; border-radius: 8px; border: 1px solid var(--border-color);">
        <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 6px;">
          <span style="font-weight: 600; font-size: 13px;">📱 Android APK (${sizeStr})</span>
          <a href="${d.url}" class="btn btn-sm btn-primary" download>${I18N[currentLang].btn_dl_version}</a>
        </div>
        <div class="sha256-box">SHA-256: ${d.sha256}</div>
      </div>
    `;
  }

  if (downloads.linux) {
    const d = downloads.linux;
    const mb = (d.size / (1024 * 1024)).toFixed(2);
    dlHtml += `
      <div style="background: var(--bg-surface); padding: 12px; border-radius: 8px; border: 1px solid var(--border-color);">
        <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 6px;">
          <span style="font-weight: 600; font-size: 13px;">💻 Linux Single Binary (${mb} MB)</span>
          <a href="${d.url}" class="btn btn-sm btn-secondary" download>${I18N[currentLang].btn_dl_version}</a>
        </div>
        <div class="sha256-box">SHA-256: ${d.sha256}</div>
      </div>
    `;
  }

  if (downloads.windows) {
    const d = downloads.windows;
    const mb = (d.size / (1024 * 1024)).toFixed(2);
    dlHtml += `
      <div style="background: var(--bg-surface); padding: 12px; border-radius: 8px; border: 1px solid var(--border-color);">
        <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 6px;">
          <span style="font-weight: 600; font-size: 13px;">🪟 Windows Executable (${mb} MB)</span>
          <a href="${d.url}" class="btn btn-sm btn-secondary" download>${I18N[currentLang].btn_dl_version}</a>
        </div>
        <div class="sha256-box">SHA-256: ${d.sha256}</div>
      </div>
    `;
  }

  dlHtml += '</div>';

  container.innerHTML = `
    <div class="version-changelog">
      <strong>Changelog (v${verObj.version}):</strong><br>
      ${changelog}
    </div>
    ${dlHtml}
  `;
}

function closeWebModal() {
  const modal = document.getElementById('webModal');
  if (modal) modal.classList.remove('open');
  activeApp = null;
}

function closeWebModalOnBackdrop(e) {
  if (e.target.id === 'webModal') {
    closeWebModal();
  }
}

// Global escape key listener
document.addEventListener('keydown', (e) => {
  if (e.key === 'Escape') {
    closeWebModal();
  }
});
