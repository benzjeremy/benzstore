#!/usr/bin/env python3
"""Update feed.json for BenzStore.

This script removes the BenzStore entry, updates the Learn and Wetter icon URLs
to point to the local copies in ``/benzstore/icons``, adds a ``release_url``
for each binary version, and re‑computes SHA‑256 hashes (and file sizes)
whenever a matching local file exists.
"""

import json
import os
import hashlib

# ----- Configuration -----
ROOT_DIR  = os.path.abspath("/home/benzj/Projekte/benzjeremy.github.io")
FEED_PATH = os.path.join(ROOT_DIR, "benzstore", "web", "feed.json")
ICON_DIR  = os.path.join(ROOT_DIR, "benzstore", "icons")

# ----- Helpers -----

def sha256_of(path: str) -> str:
    h = hashlib.sha256()
    with open(path, "rb") as f:
        for chunk in iter(lambda: f.read(4096), b""):
            h.update(chunk)
    return h.hexdigest()


def find_local_file(root: str, filename: str):
    for dirpath, dirnames, filenames in os.walk(root):
        # Skip extraneous dirs
        for d in ["node_modules", "__pycache__", "build"]:
            if d in dirnames:
                dirnames.remove(d)
        if filename in filenames:
            return os.path.join(dirpath, filename)
    return None

# ----- Main -----

def main() -> None:
    with open(FEED_PATH, "r", encoding="utf-8") as f:
        data = json.load(f)

    # 1. Remove the BenzStore app entry
    data["apps"] = [app for app in data["apps"] if app["id"] != "com.benzjeremy.benzstore"]

    # 2. Update icons for Learn and Wetter
    for app in data["apps"]:
        if app["id"] == "com.benzjeremy.learn":
            app["icon"] = "https://benzjeremy.github.io/benzstore/icons/learn.png"
        elif app["id"] == "com.benzjeremy.wetter":
            app["icon"] = "https://benzjeremy.github.io/benzstore/icons/wetter.png"

    # 3. Add release_url and (re)validate SHA‑256 per binary
    repo_root = ROOT_DIR
    for app in data["apps"]:
        # Der Repository‑Slug (z. B. "benzjeremy/learn")
        repo_slug = app["source"].rstrip("/").split("github.com/")[-1]
        for ver in app.get("versions", []):
            ver["release_url"] = f"https://github.com/{repo_slug}/releases/tag/{ver['version']}"
            for plat, dl in ver.get("downloads", {}).items():
                local = find_local_file(repo_root, dl["filename"])
                if local and os.path.isfile(local):
                    new_hash = sha256_of(local)
                    if new_hash != dl["sha256"]:
                        dl["sha256"] = new_hash
                    dl["size"] = os.path.getsize(local)

    # 4. Write back the processed JSON
    with open(FEED_PATH, "w", encoding="utf-8") as f:
        json.dump(data, f, indent=2)
        f.write("\n")
    print("feed.json updated")

if __name__ == "__main__":
    main()
