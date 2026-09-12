let catalog = { apps: [], store: {} };
let currentLang = localStorage.getItem('benzstore_lang') || 'de';
let currentFilter = 'all';
let currentSearch = '';
let activeApp = null;
let authToken = new URLSearchParams(window.location.search).get('token') || '';

const i18n = {
  de: {
    tagline: "Einheitlicher Privacy-First AppStore für Android & PC",
    sync: "Sync",
    search_ph: "Anwendungen durchsuchen (z. B. Untis, Docker, Lernen)...",
    filter_all: "Alle",
    filter_pc: "PC & Desktop",
    filter_android: "Android APKs",
    filter_tools: "Entwickler-Tools",
    filter_edu: "Bildung",
    filter_util: "Nützliches",
    loading: "Lade Katalog...",
    install: "Installieren",
    installed: "Installiert",
    update: "Aktualisieren",
    launch: "Starten",
    details: "Details & Versionen",
    version_select: "Version auswählen:",
    download_size: "Download-Größe:",
    sha256_label: "Kryptografische Signatur (SHA-256):",
    install_version: "Diese Version installieren",
    direct_download: "Direkt herunterladen",
    no_results: "Keine Anwendungen für diesen Filter gefunden.",
    already_installed_banner: "✓ Version v{ver} ist bereits installiert.",
    already_installed_btn: "✓ Bereits installiert",
    reinstall_btn: "↺ Erneut installieren",
    update_available: "⬆ Update verfügbar (v{ver})",
    update_banner: "⬆ Update verfügbar: v{current} ➔ v{target}",
    update_btn: "⬆ Auf v{ver} aktualisieren",
    downgrade_banner: "⚠️ Downgrade-Warnung: Installiert ist Version v{current}. Die gewählte Version v{target} ist älter! Mögliche Dateninkompatibilität.",
    downgrade_btn: "⚠️ Downgrade auf v{ver}",
    downgrade_confirm: "Warnung: Möchtest du wirklich von der neueren Version v{current} auf die ältere Version v{target} downgraden?"
  },
  en: {
    tagline: "Unified Privacy-First AppStore for Android & PC",
    sync: "Sync",
    search_ph: "Search applications (e.g. Untis, Docker, Learn)...",
    filter_all: "All",
    filter_pc: "PC & Desktop",
    filter_android: "Android APKs",
    filter_tools: "Dev Tools",
    filter_edu: "Education",
    filter_util: "Utilities",
    loading: "Loading catalog...",
    install: "Install",
    installed: "Installed",
    update: "Update",
    launch: "Launch",
    details: "Details & Versions",
    version_select: "Select Version:",
    download_size: "Download Size:",
    sha256_label: "Cryptographic Fingerprint (SHA-256):",
    install_version: "Install this version",
    direct_download: "Direct Download",
    no_results: "No applications found for this filter.",
    already_installed_banner: "✓ Version v{ver} is already installed.",
    already_installed_btn: "✓ Already Installed",
    reinstall_btn: "↺ Reinstall",
    update_available: "⬆ Update available (v{ver})",
    update_banner: "⬆ Update available: v{current} ➔ v{target}",
    update_btn: "⬆ Update to v{ver}",
    downgrade_banner: "⚠️ Downgrade Warning: Currently installed is version v{current}. Selected version v{target} is older! Potential incompatibility.",
    downgrade_btn: "⚠️ Downgrade to v{ver}",
    downgrade_confirm: "Warning: Are you sure you want to downgrade from newer version v{current} to older version v{target}?"
  }
};

function compareVersions(v1, v2) {
  if (!v1 && !v2) return 0;
  if (!v1) return -1;
  if (!v2) return 1;

  const clean1 = v1.toString().trim().replace(/^[vV]/, '');
  const clean2 = v2.toString().trim().replace(/^[vV]/, '');

  const parts1 = clean1.split(/[.-]/).map(p => parseInt(p, 10) || 0);
  const parts2 = clean2.split(/[.-]/).map(p => parseInt(p, 10) || 0);

  const len = Math.max(parts1.length, parts2.length);
  for (let i = 0; i < len; i++) {
    const p1 = parts1[i] || 0;
    const p2 = parts2[i] || 0;
    if (p1 < p2) return -1;
    if (p1 > p2) return 1;
  }
  return 0;
}

function toggleLanguage() {
  currentLang = currentLang === 'de' ? 'en' : 'de';
  localStorage.setItem('benzstore_lang', currentLang);
  document.getElementById('langToggle').textContent = currentLang === 'de' ? 'EN' : 'DE';
  updateTexts();
  renderApps();
}

function updateTexts() {
  const dict = i18n[currentLang];
  document.querySelectorAll('[data-i18n]').forEach(el => {
    const key = el.getAttribute('data-i18n');
    if (dict[key]) el.textContent = dict[key];
  });
  document.querySelectorAll('[data-i18n-placeholder]').forEach(el => {
    const key = el.getAttribute('data-i18n-placeholder');
    if (dict[key]) el.placeholder = dict[key];
  });
}

async function loadCatalog() {
  const statusEl = document.getElementById('catalogStatus');
  statusEl.textContent = i18n[currentLang].loading;

  try {
    let res;
    if (window.location.origin.includes('127.0.0.1') || window.location.origin.includes('localhost')) {
      res = await fetch('/api/feed?token=' + encodeURIComponent(authToken));
    } else {
      res = await fetch('https://raw.githubusercontent.com/benzjeremy/benzstore/content/feed.json');
    }

    if (!res.ok) throw new Error('HTTP ' + res.status);
    catalog = await res.json();
    statusEl.textContent = `${catalog.apps.length} Anwendungen bereitgestellt • BenzStore v1.0`;
    renderApps();
  } catch (err) {
    statusEl.textContent = 'Fehler beim Laden: ' + err.message;
  }
}

function setFilter(filter) {
  currentFilter = filter;
  document.querySelectorAll('.filter-pills .pill').forEach(btn => {
    btn.classList.toggle('active', btn.getAttribute('data-filter') === filter);
  });
  renderApps();
}

function filterApps() {
  currentSearch = document.getElementById('searchInput').value.trim().toLowerCase();
  renderApps();
}

function renderApps() {
  const grid = document.getElementById('appsGrid');
  grid.innerHTML = '';

  const filtered = catalog.apps.filter(app => {
    if (currentFilter === 'pc' && !app.platforms.some(p => ['linux', 'windows', 'pc'].includes(p.toLowerCase()))) return false;
    if (currentFilter === 'android' && !app.platforms.includes('android')) return false;
    if (['tools', 'education', 'utility'].includes(currentFilter) && !app.categories.includes(currentFilter)) return false;

    if (currentSearch) {
      const summary = currentLang === 'de' ? (app.summary_de || app.summary_en) : (app.summary_en || app.summary_de);
      const match = app.name.toLowerCase().includes(currentSearch) || (summary && summary.toLowerCase().includes(currentSearch));
      if (!match) return false;
    }
    return true;
  });

  if (filtered.length === 0) {
    grid.innerHTML = `<div style="grid-column: 1/-1; text-align: center; color: var(--text-muted); padding: 40px 0;">${i18n[currentLang].no_results}</div>`;
    return;
  }

  filtered.forEach(app => {
    const card = document.createElement('div');
    card.className = 'app-card';

    const summary = currentLang === 'de' ? (app.summary_de || app.summary_en) : (app.summary_en || app.summary_de);
    const platformsHtml = app.platforms.map(p => {
      const pClass = p.toLowerCase() === 'android' ? 'android' : 'pc';
      return `<span class="platform-pill ${pClass}">${p.toUpperCase()}</span>`;
    }).join(' ');

    let actionBtnHtml = '';
    let badgeHtml = '';
    if (app.is_installed) {
      const installedVer = app.installed_version || app.latest_version;
      const cmp = compareVersions(app.latest_version, installedVer);
      if (cmp > 0) {
        badgeHtml = `<span class="install-badge badge-update">v${installedVer} • ⬆ Update</span>`;
        actionBtnHtml = `<button class="btn-action btn-update" onclick="installApp('${app.id}', '${app.latest_version}')">⬆ ${i18n[currentLang].update} (v${app.latest_version})</button>`;
      } else {
        badgeHtml = `<span class="install-badge badge-installed">✓ ${i18n[currentLang].installed} (v${installedVer})</span>`;
        actionBtnHtml = `<button class="btn-action btn-launch" onclick="launchApp('${app.id}')">▶ ${i18n[currentLang].launch}</button>`;
      }
    } else if (app.platforms.some(p => ['linux', 'windows', 'pc'].includes(p.toLowerCase()))) {
      actionBtnHtml = `<button class="btn-action btn-primary" onclick="installApp('${app.id}', '${app.latest_version}')">⬇ ${i18n[currentLang].install}</button>`;
    } else {
      actionBtnHtml = `<button class="btn-action btn-secondary" onclick="openAppModal('${app.id}')">📱 Android APK</button>`;
    }

    card.innerHTML = `
      <div class="app-card-top">
        <img src="${app.icon}" alt="${app.name}" class="app-icon" onerror="this.src='icon.png'">
        <div class="app-info">
          <div class="app-header-row">
            <span class="app-name">${app.name}</span>
            <div style="display: flex; align-items: center; gap: 6px;">
              ${badgeHtml}
              <span class="app-ver-tag">v${app.latest_version}</span>
            </div>
          </div>
          <p class="app-summary">${summary}</p>
          <div class="app-meta-row">
            ${platformsHtml}
          </div>
        </div>
      </div>
      <div class="app-card-actions">
        ${actionBtnHtml}
        <button class="btn-action btn-secondary" onclick="openAppModal('${app.id}')">🔍 ${i18n[currentLang].details}</button>
      </div>
    `;

    grid.appendChild(card);
  });
}

function openAppModal(appId) {
  const app = catalog.apps.find(a => a.id === appId);
  if (!app) return;
  activeApp = app;

  const desc = currentLang === 'de' ? (app.description_de || app.description_en) : (app.description_en || app.description_de);
  const versionOptions = app.versions.map(v => {
    const isLatest = v.version === app.latest_version ? ' (Latest)' : '';
    return `<option value="${v.version}">v${v.version}${isLatest} – ${v.release_date}</option>`;
  }).join('');

  const modalBody = document.getElementById('modalBody');
  modalBody.innerHTML = `
    <div class="detail-hero">
      <img src="${app.icon}" alt="${app.name}" onerror="this.src='icon.png'">
      <div>
        <h2>${app.name}</h2>
        <span style="font-size: 13px; color: var(--text-muted);">${app.author} • Lizenz: ${app.license}</span>
      </div>
    </div>
    <div class="detail-desc">${desc}</div>

    <div class="version-picker-box">
      <label style="font-weight: 600; font-size: 14px;">${i18n[currentLang].version_select}</label>
      <select id="modalVersionSelect" class="version-select" onchange="onModalVersionChange()">
        ${versionOptions}
      </select>
      <div id="modalVersionStatus"></div>
      <div id="modalVersionChangelog" class="version-changelog"></div>
      <div id="modalVersionSha" class="sha256-box"></div>
    </div>

    <div id="modalActionArea" style="display: flex; gap: 10px; margin-top: 16px;"></div>
  `;

  onModalVersionChange();
  document.getElementById('appModal').classList.add('open');
}

function onModalVersionChange() {
  if (!activeApp) return;
  const select = document.getElementById('modalVersionSelect');
  const chosenVer = select.value;
  const verObj = activeApp.versions.find(v => v.version === chosenVer) || activeApp.versions[0];

  const changelog = currentLang === 'de' ? (verObj.changelog_de || verObj.changelog_en) : (verObj.changelog_en || verObj.changelog_de);
  document.getElementById('modalVersionChangelog').textContent = changelog ? `Changelog: ${changelog}` : 'Kein Changelog verfügbar.';

  let dlInfo = verObj.downloads.linux || verObj.downloads.android || verObj.downloads.windows;
  if (dlInfo) {
    document.getElementById('modalVersionSha').textContent = `SHA-256: ${dlInfo.sha256 || 'Wird verifiziert'}\nURL: ${dlInfo.url}`;
  } else {
    document.getElementById('modalVersionSha').textContent = 'Keine Download-Informationen für diese Version.';
  }

  const statusContainer = document.getElementById('modalVersionStatus');
  const actionArea = document.getElementById('modalActionArea');
  const isLocal = window.location.origin.includes('127.0.0.1') || window.location.origin.includes('localhost');
  const installedVer = activeApp.installed_version;

  if (activeApp.is_installed && installedVer) {
    const cmp = compareVersions(chosenVer, installedVer);
    if (cmp === 0) {
      // Exactly same version already installed
      const bannerText = i18n[currentLang].already_installed_banner.replace('{ver}', chosenVer);
      statusContainer.innerHTML = `<div class="modal-status-banner banner-installed">${bannerText}</div>`;
      if (isLocal) {
        actionArea.innerHTML = `
          <button class="btn-action btn-launch" style="padding: 12px;" onclick="launchApp('${activeApp.id}')">
            ▶ ${i18n[currentLang].launch}
          </button>
          <button class="btn-action btn-secondary" style="padding: 12px;" onclick="installApp('${activeApp.id}', '${chosenVer}', true)" title="Erneut installieren">
            ${i18n[currentLang].reinstall_btn}
          </button>
        `;
      } else if (dlInfo && dlInfo.url) {
        actionArea.innerHTML = `
          <a href="${dlInfo.url}" class="btn-action btn-secondary" style="text-decoration: none; padding: 12px; text-align: center;" download>
            ⬇ ${i18n[currentLang].direct_download} (v${chosenVer})
          </a>
        `;
      }
    } else if (cmp < 0) {
      // Downgrade warning!
      const bannerText = i18n[currentLang].downgrade_banner.replace('{current}', installedVer).replace('{target}', chosenVer);
      statusContainer.innerHTML = `<div class="modal-status-banner banner-downgrade">${bannerText}</div>`;
      if (isLocal) {
        actionArea.innerHTML = `
          <button class="btn-action btn-danger" style="padding: 12px;" onclick="promptDowngrade('${activeApp.id}', '${chosenVer}', '${installedVer}')">
            ${i18n[currentLang].downgrade_btn.replace('{ver}', chosenVer)}
          </button>
        `;
      } else if (dlInfo && dlInfo.url) {
        actionArea.innerHTML = `
          <a href="${dlInfo.url}" class="btn-action btn-danger" style="text-decoration: none; padding: 12px; text-align: center;" download onclick="return confirm(i18n[currentLang].downgrade_confirm.replace('{current}', '${installedVer}').replace('{target}', '${chosenVer}'))">
            ⚠️ ${i18n[currentLang].direct_download} (v${chosenVer})
          </a>
        `;
      }
    } else {
      // Update!
      const bannerText = i18n[currentLang].update_banner.replace('{current}', installedVer).replace('{target}', chosenVer);
      statusContainer.innerHTML = `<div class="modal-status-banner banner-update">${bannerText}</div>`;
      if (isLocal) {
        actionArea.innerHTML = `
          <button class="btn-action btn-update" style="padding: 12px;" onclick="installApp('${activeApp.id}', '${chosenVer}')">
            ${i18n[currentLang].update_btn.replace('{ver}', chosenVer)}
          </button>
        `;
      } else if (dlInfo && dlInfo.url) {
        actionArea.innerHTML = `
          <a href="${dlInfo.url}" class="btn-action btn-update" style="text-decoration: none; padding: 12px; text-align: center;" download>
            ⬆ ${i18n[currentLang].direct_download} (v${chosenVer})
          </a>
        `;
      }
    }
  } else {
    // Not installed
    statusContainer.innerHTML = '';
    if (isLocal) {
      actionArea.innerHTML = `
        <button class="btn-action btn-primary" style="padding: 12px;" onclick="installApp('${activeApp.id}', '${chosenVer}')">
          ⬇ ${i18n[currentLang].install_version} (v${chosenVer})
        </button>
      `;
    } else if (dlInfo && dlInfo.url) {
      actionArea.innerHTML = `
        <a href="${dlInfo.url}" class="btn-action btn-primary" style="text-decoration: none; padding: 12px; text-align: center;" download>
          ⬇ ${i18n[currentLang].direct_download} (v${chosenVer})
        </a>
      `;
    }
  }
}

function promptDowngrade(appId, targetVer, currentVer) {
  const msg = i18n[currentLang].downgrade_confirm.replace('{current}', currentVer).replace('{target}', targetVer);
  if (confirm(msg)) {
    installApp(appId, targetVer, true);
  }
}

function closeModal() {
  document.getElementById('appModal').classList.remove('open');
  activeApp = null;
}

function closeModalOnBackdrop(e) {
  if (e.target.id === 'appModal') closeModal();
}

async function installApp(appId, version, force = false) {
  showToast(`Installiere ${appId} (v${version})... SHA-256 wird geprüft.`);
  try {
    const res = await fetch('/api/install?token=' + encodeURIComponent(authToken), {
      method: 'POST',
      headers: { 'Content-Type': 'application/json', 'X-BenzStore-Token': authToken },
      body: JSON.stringify({ app_id: appId, version: version, force: force })
    });
    const data = await res.json();
    if (res.ok && data.success) {
      showToast(`✅ ${data.message}!`);
      await loadCatalog();
      if (activeApp && activeApp.id === appId) {
        activeApp = catalog.apps.find(a => a.id === appId) || activeApp;
        onModalVersionChange();
      }
    } else {
      showToast(`❌ ${data.message || 'Installation fehlgeschlagen'}`);
    }
  } catch (err) {
    showToast(`❌ Netzwerkfehler: ${err.message}`);
  }
}

async function launchApp(appId) {
  try {
    const res = await fetch('/api/launch?token=' + encodeURIComponent(authToken), {
      method: 'POST',
      headers: { 'Content-Type': 'application/json', 'X-BenzStore-Token': authToken },
      body: JSON.stringify({ app_id: appId })
    });
    if (res.ok) {
      showToast(`🚀 ${appId} gestartet.`);
    } else {
      showToast(`❌ Startfehler`);
    }
  } catch (err) {
    showToast(`❌ Fehler: ${err.message}`);
  }
}

function showToast(msg) {
  const toast = document.getElementById('toastBar');
  toast.textContent = msg;
  toast.classList.add('show');
  setTimeout(() => toast.classList.remove('show'), 4500);
}

document.addEventListener('DOMContentLoaded', () => {
  document.getElementById('langToggle').textContent = currentLang === 'de' ? 'EN' : 'DE';
  updateTexts();
  loadCatalog();
});
