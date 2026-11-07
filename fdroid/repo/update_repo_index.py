#!/usr/bin/env python3
"""Update F-Droid manual repo index XML/v2 from a signed APK file.

After copying a new APK into this directory, run:
    python3 update_repo_index.py --apk ../app/build/outputs/apk/release/app-release-*.apk

This updates index.xml (signing hash, size), index-v2.json, and the metadata YAML version.
Preserves all other repo config (pubkey, timestamp, description).
"""

import argparse
import hashlib
import json
import os
import re
import subprocess
import sys


REPO_DIR = os.path.dirname(os.path.abspath(__file__))
PACKAGE_ID = "com.bubo.voidscanner"
DEFAULT_SIG = "df6c014897b3af7bf4873003bea63060"  # current project keystore


def sha256_file(path):
    h = hashlib.sha256()
    with open(path, "rb") as f:
        for chunk in iter(lambda: f.read(1 << 20), b""):
            h.update(chunk)
    return h.hexdigest()


def extract_apk_signature(keystore_path=None):
    """Get the signing certificate SHA-256 hex from our project keystore.
    
    Returns lowercase hex digest, or None if keystore unavailable.
    """
    if keystore_path is None:
        # Default: ../app/voidscanner.keystore
        keystore_path = os.path.join(os.path.dirname(REPO_DIR), "app", "voidscanner.keystore")

    if not os.path.exists(keystore_path):
        return None

    result = subprocess.run(
        ["keytool", "-list", "-v", "-keystore", keystore_path,
         "-storepass", "void123", "-alias", "voidscanner"],
        capture_output=True, text=True
    )

    if result.returncode != 0:
        return None

    for line in result.stdout.splitlines():
        m = re.match(r"SHA256:\s*(.+)", line.strip())
        if m:
            raw = m.group(1).replace(" ", "")
            return raw.lower()

    return None


def update_index_xml(apk_path):
    """Update index.xml with new APK hash, size, and signature."""
    xml_path = os.path.join(REPO_DIR, "index.xml")
    apk_size = os.path.getsize(apk_path)
    apk_hash = sha256_file(apk_path)
    
    # Get current signature from repo or compute from keystore
    with open(xml_path, "r") as f:
        xml_content = f.read()

    sig_match = re.search(r'<sig>([0-9a-f]+)</sig>', xml_content)
    sig_value = DEFAULT_SIG  # fallback
    if not sig_match and not extract_apk_signature():
        print(f"WARNING: No keystore; preserving existing signature {DEFAULT_SIG}", file=sys.stderr)

    # Update SHA-256 hash — find and replace the exact text between tags
    with open(xml_path, "r") as f:
        xml_content = f.read()

    # Find the current 64-char hex hash right after <hash type="sha256">
    old_hash_match = re.search(r'<hash type="sha256">([0-9a-f]{64})', xml_content)
    if old_hash_match:
        old_hash = old_hash_match.group(1)
        # Replace exactly the original hash (avoids greedy concat issues)
        xml_content = xml_content.replace(f'<hash type="sha256">{old_hash}</hash>', 
                                          f'<hash type="sha256">{apk_hash}</hash>')

    # Update size for the APK entry (size followed by <sdkver>)
    xml_content = re.sub(
        r'(<size>)(\d+)(</size>\s*<sdkver>)',
        lambda m: f'{m.group(1)}{apk_size}{m.group(3)}',
        xml_content
    )

    with open(xml_path, "w") as f:
        f.write(xml_content)

    print(f"Updated index.xml: hash={apk_hash[:16]}... size={apk_size}", file=sys.stderr)
    return apk_hash


def update_index_v2(apk_hash):
    """Update index-v2.json hashes."""
    v2_path = os.path.join(REPO_DIR, "index-v2.json")

    with open(v2_path, "r") as f:
        data = json.load(f)

    # Update the file.sha256 under packages -> pkg_id -> versions
    if "packages" in data and isinstance(data["packages"], dict):
        for pkg_name in [PACKAGE_ID]:
            if pkg_name in data["packages"]:
                ver_data = data["packages"][pkg_name].get("versions", {})
                for vkey, ver_map in ver_data.items():
                    if isinstance(ver_map, dict) and "file" in ver_map:
                        ver_map["file"]["sha256"] = apk_hash

    with open(v2_path, "w") as f:
        json.dump(data, f, indent=2)


def update_metadata_yaml():
    """Update the metadata YAML's version field to match gradle.properties."""
    yml_path = os.path.join(REPO_DIR, "metadata", PACKAGE_ID + ".yml")

    with open(yml_path, "r") as f:
        lines = f.readlines()

    # Read current version from gradle.properties (project root: two levels up from fdroid/repo/)
    gppath = os.path.join(REPO_DIR, '..', '..', 'gradle.properties')
    major_str = minor_str = patch_str = "0"
    if os.path.exists(gppath):
        with open(gppath) as gf:
            for pline in gf:
                if pline.startswith("APP_VERSION_MAJOR="):
                    major_str = pline.split("=", 1)[1].strip()
                elif pline.startswith("APP_VERSION_MINOR="):
                    minor_str = pline.split("=", 1)[1].strip()
                elif pline.startswith("APP_VERSION_PATCH="):
                    patch_str = pline.split("=", 1)[1].strip()

    ver_name = f"{major_str}.{minor_str}.{patch_str}"
    code = int(major_str) * 10000 + int(minor_str) * 100 + int(patch_str)

    new_lines = []
    for line in lines:
        if line.startswith("Version: "):
            new_lines.append(f"Version: {ver_name}\n")
        elif line.startswith("VersionCode: "):
            new_lines.append(f"VersionCode: {code}\n")
        else:
            new_lines.append(line)

    with open(yml_path, "w") as f:
        f.writelines(new_lines)


def main():
    parser = argparse.ArgumentParser(
        description="Update F-Droid manual repo index for a new APK release."
    )
    parser.add_argument("--apk", required=True, help="Path to the new APK file")
    parser.add_argument("--quiet", action="store_true",
                        help="Suppress progress output")
    args = parser.parse_args()

    apk_path = os.path.abspath(args.apk)
    if not os.path.exists(apk_path):
        print(f"ERROR: APK file does not exist: {apk_path}", file=sys.stderr)
        sys.exit(1)

    # Update repo metadata  
    apk_hash = update_index_xml(apk_path)
    update_index_v2(apk_hash)
    update_metadata_yaml()

    if not args.quiet:
        print("\nTo verify deployment runs without error:", file=sys.stderr)
        print("python3 validate_fdroid_repo.py . --strict", file=sys.stderr)


if __name__ == "__main__":
    main()
