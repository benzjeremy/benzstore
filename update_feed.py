import json
import os
import hashlib

def sha256_file(filepath):
    """Return SHA256 hex digest of file."""
    h = hashlib.sha256()
    with open(filepath, 'rb') as f:
        for chunk in iter(lambda: f.read(4096), b''):
            h.update(chunk)
    return h.hexdigest()

def find_file_by_name(root_dir, filename):
    """Search for a file with the given name under root_dir.
    Returns the first match found."""
    for root, dirs, files in os.walk(root_dir):
        # Skip some directories to speed up and avoid irrelevant files
        # Skip .git, node_modules, __pycache__, etc.
        dirs[:] = [d for d in dirs if d not in ['.git', 'node_modules', '__pycache__', 'build', '.obsidian', '.vscode']]
        if filename in files:
            return os.path.join(root, filename)
    return None

def main():
    feed_path = 'web/feed.json'
    with open(feed_path, 'r', encoding='utf-8') as f:
        data = json.load(f)

    # 1. Update icons for Learn and Wetter
    for app in data['apps']:
        if app['id'] == 'com.benzjeremy.learn':
            app['icon'] = 'https://benzjeremy.github.io/benzstore/icons/learn.png'
        elif app['id'] == 'com.benzjeremy.wetter':
            app['icon'] = 'https://benzjeremy.github.io/benzstore/icons/wetter.png'

    # 2. Remove BenzStore from apps list
    data['apps'] = [app for app in data['apps'] if app['id'] != 'com.benzjeremy.benzstore']

    # 3. Update SHA256 hashes by searching for the file locally
    repo_root = "/home/benzj/Projekte/benzjeremy.github.io"
    updated_count = 0
    not_found_count = 0

    for app in data['apps']:
        for version in app['versions']:
            for platform_key, dl in version['downloads'].items():
                url = dl['url']
                filename = dl['filename']
                # Try to find the file by name in the repo
                local_path = find_file_by_name(repo_root, filename)
                if local_path and os.path.isfile(local_path):
                    computed_hash = sha256_file(local_path)
                    if computed_hash != dl['sha256']:
                        print(f"Updating SHA256 for {app['id']} {version['version']} {platform_key}: {dl['sha256']} -> {computed_hash}")
                        dl['sha256'] = computed_hash
                        updated_count += 1
                    else:
                        # Hash matches, no update needed
                        pass
                else:
                    print(f"Warning: Could not find local file for {filename} (from {app['id']} {version['version']} {platform_key})")
                    not_found_count += 1

    # Write the updated feed.json
    with open(feed_path, 'w', encoding='utf-8') as f:
        json.dump(data, f, indent=2)

    print(f'Feed.json updated: icons, BenzStore removal, {updated_count} SHA256 hashes updated, {not_found_count} files not found locally.')

if __name__ == '__main__':
    main()