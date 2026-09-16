#!/usr/bin/env python3
"""add_legal.py – Ergänzt jedem App‑Eintrag im feed.json die Felder legal.info.

Speichere dieses Skript unter
  benzstore/internal/api/add_legal.py
und führe es anschließend aus: ```bash
python3 benzstore/internal/api/add_legal.py
```
"""

import json
import os

ROOT = os.path.abspath("/home/benzj/Projekte/benzjeremy.github.io")
FEED = os.path.join(ROOT, "benzstore", "web", "feed.json")

with open(FEED, "r", encoding="utf-8") as f:
    data = json.load(f)

for app in data.get("apps", []):
    app["legal"] = {
        "license_name": app.get("license", "GPL-3.0"),
        "license_url": "https://opensource.org/licenses/gpl-3.0.html",
        "impressum_url": "https://benzjeremy.github.io/impressum.html",
        "datenschutz_url": "https://benzjeremy.github.io/datenschutz.html",
    }

with open(FEED, "w", encoding="utf-8") as f:
    json.dump(data, f, indent=2)
    f.write("\n")

print("Legal fields added to feed.json")
