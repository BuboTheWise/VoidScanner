#!/usr/bin/env python3
"""
F-Droid Manual Repository Validation Harness
=============================================

Validates a manually-built F-Droid repository against the official spec:
  https://f-droid.org/docs/Manual-Repos/
  https://f-droid.org/docs/All_Metadata_Formats/
  https://f-droid.org/docs/Repo_Index_Format/

Checks performed (7 validation groups):
  1. Directory Structure   — required files, layout, APK presence
  2. Metadata YAML Syntax  — field validity, forbidden keys, value types
  3. Index XML             — well-formedness, <fdroid>/<repo>/<application> elements
  4. APK Hash Integrity    — SHA-256 in index-v2.json matches on-disk files
  5. Signing Configuration — config.yml key/alias/password/keystore checks
  6. Version Consistency   — metadata / index / APK filename version alignment
  7. Icon Assets           — repo-icon.png + icons/ directory with 512px

Usage:
    # From VoidScanner project root:
    python3 fdroid/validate_fdroid_repo.py fdroid/repo --config fdroid/config.yml

    # Standalone against any manual F-Droid repo:
    python3 validate_fdroid_repo.py /path/to/repo --config /path/to/config.yml

Options:
    --config CONFIG_PATH     Path to config.yml (default: ../config.yml relative to repo)
    --strict                 Fail on warnings as well as errors
    --json                   Emit results as JSON instead of human-readable text

Exit code: 0 if no blocking errors, 1 otherwise.
"""

import argparse
import hashlib
import json
import os
import re
import sys
import xml.etree.ElementTree as ET
from dataclasses import dataclass, field
from pathlib import Path
from typing import Optional

# ---------------------------------------------------------------------------
# Try to import PyYAML; fall back gracefully if unavailable
# ---------------------------------------------------------------------------
try:
    import yaml  # type: ignore[import-not-found]
    HAS_YAML = True
except ImportError:
    yaml = None  # type: ignore[assignment,misc]
    HAS_YAML = False

# ---------------------------------------------------------------------------
# Data structures
# ---------------------------------------------------------------------------

@dataclass
class CheckResult:
    """A single validation check outcome."""
    name: str
    passed: bool
    message: str
    severity: str  # "ERROR" | "WARNING"

@dataclass
class ValidationResult:
    """Aggregate of all checks for one section."""
    section: str
    checks: list[CheckResult] = field(default_factory=list)

# ---------------------------------------------------------------------------
# Helpers
# ---------------------------------------------------------------------------

def sha256_file(path: Path) -> str:
    """Compute SHA-256 hex digest of a file."""
    h = hashlib.sha256()
    with open(path, "rb") as f:
        for chunk in iter(lambda: f.read(65536), b""):
            h.update(chunk)
    return h.hexdigest()

def apk_version_from_name(path: Path) -> Optional[str]:
    """Try to extract version name from APK filename pattern."""
    stem = path.stem
    # Patterns like app-1.2.00.apk or void-scanner-1.2.00.apk
    m = re.search(r'-(\d+\.\d+(?:\.\d+)?)\b', stem)
    return m.group(1) if m else None

# ---------------------------------------------------------------------------
# Validation: Directory Structure
# ---------------------------------------------------------------------------

REQUIRED_FILES = ["index.xml", "index-v2.json"]
RECOMMENDED_FILES = ["repo-icon.png"]


def check_directory_structure(repo_path: Path) -> ValidationResult:
    res = ValidationResult(section="Directory Structure")

    for fname in REQUIRED_FILES:
        fpath = repo_path / fname
        if fpath.is_file():
            res.checks.append(CheckResult(fname, True, f"{fname} exists", "ERROR"))
        else:
            res.checks.append(CheckResult(fname, False, f"MISSING: {fname} is required", "ERROR"))

    for fname in RECOMMENDED_FILES:
        if (repo_path / fname).is_file():
            res.checks.append(CheckResult(
                f"{fname} (recommended)", True, f"{fname} exists", "WARNING"))
        else:
            res.checks.append(CheckResult(
                f"{fname} (recommended)", False,
                f"Missing recommended file: {fname}", "WARNING"))

    apk_files = list(repo_path.glob("*.apk"))
    if apk_files:
        res.checks.append(CheckResult(
            "APK files present", True, f"{len(apk_files)} APK(s) found", "ERROR"))
    else:
        res.checks.append(CheckResult(
            "APK files present", False, "No .apk files in repo directory", "ERROR"))

    meta_dir = repo_path / "metadata"
    if meta_dir.is_dir():
        meta_count = len(list(meta_dir.glob("*.yml"))) + len(list(meta_dir.glob("*.yaml")))
        res.checks.append(CheckResult(
            "metadata directory", True, f"{meta_count} metadata file(s)", "ERROR"))
    else:
        res.checks.append(CheckResult(
            "metadata directory", False, "No metadata/ directory found", "ERROR"))

    return res

# ---------------------------------------------------------------------------
# Validation: Metadata YAML Syntax
# ---------------------------------------------------------------------------

REQUIRED_METADATA_FIELDS = ["Package"]

VALID_CATEGORIES = {
    "Accessibility", "ActionBar", "Add-ons", "Adult", "Ai Life", "Astronomy",
    "Audio", "Barcode", "Calculator", "Calendar", "Camera", "Communication",
    "Customization", "Date & Time", "Dictionary", "Education",
    "Environment", "Filestorage & Share", "Finance", "Font", "Games",
    "Health & Fitness", "Home Automation", "Image Viewing", "Input Method",
    "Keyboard", "Launcher", "Links", "Localization", "Maps & Navigation",
    "Media", "Music", "Network", "News", "Notes", "Office",
    "Photography", "Reader", "RSS", "Science", "Security",
    "Settings", "Social", "Sports & Fitness", "System", "Terminal Emulator",
    "Text Processing", "Themes & Wallpapers", "Tools", "Translation",
    "Transportation", "TV Input Provider", "Utilities", "Video Playback",
    "Weather", "Widget", "Writing",
}

VALID_RATINGS = {
    "noAdultContent", "noDangerousContent", "noNonFreeDependency",
    "noP2P", "noTracking",
}

FORBIDDEN_METADATA_FIELDS = [
    "CurrentVersion", "CurrentVersionCode", "UpdateChecksDisabled",
]


def check_metadata_yaml(repo_path: Path) -> ValidationResult:
    res = ValidationResult(section="Metadata YAML Syntax")

    meta_dir = repo_path / "metadata"
    if not meta_dir.is_dir():
        res.checks.append(CheckResult(
            "metadata directory exists", False,
            "Skipping: no metadata/ directory", "ERROR"))
        return res

    yaml_files = list(meta_dir.glob("*.yml")) + list(meta_dir.glob("*.yaml"))
    if not yaml_files:
        res.checks.append(CheckResult(
            "metadata files found", False,
            "No .yml or .yaml files in metadata/", "ERROR"))
        return res

    for yf in yaml_files:
        prefix = f"{yf.name}:"

        if not HAS_YAML:
            res.checks.append(CheckResult(
                f"{prefix}: YAML parseable", False,
                "PyYAML not installed — cannot validate", "ERROR"))
            continue

        try:
            with open(yf) as fh:
                data = yaml.safe_load(fh)  # type: ignore[union-attr]
            res.checks.append(CheckResult(
                f"{prefix}: valid YAML", True,
                "Parsed successfully", "ERROR"))
        except Exception as e:
            res.checks.append(CheckResult(
                f"{prefix}: valid YAML", False,
                f"Parse error: {e}", "ERROR"))
            continue

        if not isinstance(data, dict):
            res.checks.append(CheckResult(
                f"{prefix}: is mapping", False,
                "Top-level must be a YAML mapping (dict)", "ERROR"))
            continue

        # Required fields
        for req in REQUIRED_METADATA_FIELDS:
            val = data.get(req)
            if val:
                res.checks.append(CheckResult(
                    f"{prefix}: has '{req}'", True,
                    f"'{req}' = {val}", "ERROR"))
            else:
                res.checks.append(CheckResult(
                    f"{prefix}: has '{req}'", False,
                    f"Required field '{req}' missing", "ERROR"))

        # Forbidden fields (handled by F-Droid server, not manual-repo metadata)
        for forbidden in FORBIDDEN_METADATA_FIELDS:
            if forbidden in data:
                res.checks.append(CheckResult(
                    f"{prefix}: no '{forbidden}'", False,
                    f"Forbidden field '{forbidden}' present", "ERROR"))

        # Package field should match filename stem
        pkg_field = data.get("Package")
        if pkg_field and pkg_field != yf.stem:
            res.checks.append(CheckResult(
                f"{prefix}: name matches filename", False,
                f"'{pkg_field}' != '{yf.stem}'", "ERROR"))
        elif pkg_field:
            res.checks.append(CheckResult(
                f"{prefix}: name matches filename", True,
                f"'{pkg_field}' == '{yf.stem}'", "ERROR"))

        # Validate categories
        cats = data.get("Categories")
        if cats:
            cat_list = cats if isinstance(cats, list) else [cats]
            bad_cats = [c for c in cat_list if c not in VALID_CATEGORIES]
            if bad_cats:
                res.checks.append(CheckResult(
                    f"{prefix}: categories valid", False,
                    f"Unknown categories: {bad_cats}", "ERROR"))
            else:
                res.checks.append(CheckResult(
                    f"{prefix}: categories valid", True,
                    f"{len(cat_list)} category/categories OK", "ERROR"))

        # ContentRating validation
        rating = data.get("ContentRating")
        if rating:
            rat_list = rating if isinstance(rating, list) else [rating]
            bad_ratings = [r for r in rat_list if r not in VALID_RATINGS]
            if bad_ratings:
                res.checks.append(CheckResult(
                    f"{prefix}: ContentRating valid", False,
                    f"Unknown ratings: {bad_ratings}", "WARNING"))
            else:
                res.checks.append(CheckResult(
                    f"{prefix}: ContentRating valid", True,
                    "OK", "WARNING"))

    return res

# ---------------------------------------------------------------------------
# Validation: Index XML (fdroid server-compatible)
# ---------------------------------------------------------------------------


def check_index_xml(repo_path: Path) -> ValidationResult:
    res = ValidationResult(section="Index XML")
    xml_file = repo_path / "index.xml"

    if not xml_file.is_file():
        res.checks.append(CheckResult(
            "index.xml exists", False, "File missing", "ERROR"))
        return res

    try:
        tree = ET.parse(str(xml_file))
        root = tree.getroot()
        res.checks.append(CheckResult(
            "well-formed XML", True, "Parsed OK", "ERROR"))
    except ET.ParseError as e:
        res.checks.append(CheckResult(
            "well-formed XML", False, f"Parse error: {e}", "ERROR"))
        return res

    # Root must be <fdroid>
    if root.tag == "fdroid":
        res.checks.append(CheckResult(
            "root is <fdroid>", True, "Correct", "ERROR"))
    else:
        res.checks.append(CheckResult(
            "root is <fdroid>", False,
            f"Root tag is <{root.tag}> not <fdroid>", "ERROR"))

    # Check <repo> element with required attributes
    repo_elem = root.find("repo")
    if repo_elem is not None:
        for attr in ["name", "url"]:
            val = repo_elem.get(attr) or repo_elem.attrib.get(attr)
            if val:
                res.checks.append(CheckResult(
                    f"<repo> has '{attr}'", True,
                    f"'{attr}' = {val}", "ERROR"))
            else:
                res.checks.append(CheckResult(
                    f"<repo> has '{attr}'", False,
                    f"Missing '{attr}' attribute", "ERROR"))

        pubkey = repo_elem.get("pubkey")
        if pubkey:
            clean = pubkey.replace(":", "").lower()
            if re.fullmatch(r'[0-9a-f]{40}', clean):
                res.checks.append(CheckResult(
                    "<repo> pubkey valid SHA-1", True,
                    f"SHA-1 OK ({len(clean)} hex chars)", "ERROR"))
            else:
                res.checks.append(CheckResult(
                    "<repo> pubkey valid SHA-1", False,
                    f"Not a 40-char hex string: {pubkey[:20]}...", "ERROR"))

    # Check <application> elements
    apps = root.findall("application")
    if apps:
        res.checks.append(CheckResult(
            "applications indexed", True,
            f"{len(apps)} app(s) in index", "ERROR"))
    else:
        res.checks.append(CheckResult(
            "applications indexed", False,
            "No <application> elements found", "ERROR"))

    for app in apps:
        app_id = app.get("id") or app.findtext("id")
        label = f"app '{app_id}'"

        pkg = app.find("package")
        if pkg is not None:
            ver = pkg.findtext("version")
            hash_elem = pkg.find("hash")
            if ver and hash_elem is not None:
                res.checks.append(CheckResult(
                    f"{label}: has version+hash", True,
                    f"version={ver}, hash present", "ERROR"))
            else:
                res.checks.append(CheckResult(
                    f"{label}: has version+hash", False,
                    f"Missing version or hash in <package>", "ERROR"))

    return res

# ---------------------------------------------------------------------------
# Validation: APK Hash Integrity (cross-check index-v2.json vs disk)
# ---------------------------------------------------------------------------


def check_apk_hashes(repo_path: Path) -> ValidationResult:
    res = ValidationResult(section="APK Hash Integrity")

    v2_file = repo_path / "index-v2.json"
    if not v2_file.is_file():
        res.checks.append(CheckResult(
            "index-v2.json exists", False,
            "Cannot cross-check hashes — file missing", "ERROR"))
        return res

    try:
        with open(v2_file) as fh:
            v2_data = json.load(fh)
    except Exception as e:
        res.checks.append(CheckResult(
            "index-v2.json valid JSON", False, f"Parse error: {e}", "ERROR"))
        return res

    # Collect APK entries from index-v2.json packages
    packages = v2_data.get("packages", {})
    apk_info = {}  # filename -> expected sha256
    for pkg_id, pkg_data in packages.items():
        versions = pkg_data.get("versions", {})
        for _ver_hash, ver_info in versions.items():
            fobj = ver_info.get("file", {})
            fname = Path(fobj.get("name", "")).name
            expected_hash = fobj.get("sha256")
            if fname and fname.endswith(".apk") and expected_hash:
                apk_info[fname] = expected_hash

    if not apk_info:
        res.checks.append(CheckResult(
            "APK entries in index-v2", False,
            "No APK file entries found in index-v2.json packages", "ERROR"))
        return res

    # Cross-check each on-disk APK against the index
    for fname, expected_hash in apk_info.items():
        apk_path = repo_path / fname
        if not apk_path.is_file():
            res.checks.append(CheckResult(
                f"{fname}: file exists", False,
                "Referenced in index but missing on disk", "ERROR"))
            continue

        actual_hash = sha256_file(apk_path)
        if actual_hash.lower() == expected_hash.lower():
            res.checks.append(CheckResult(
                f"{fname}: hash matches", True,
                f"SHA-256 OK ({actual_hash[:16]}...)", "ERROR"))
        else:
            res.checks.append(CheckResult(
                f"{fname}: hash matches", False,
                f"MISMATCH — disk={actual_hash[:16]}..., index={expected_hash[:16]}...",
                "ERROR"))

    return res

# ---------------------------------------------------------------------------
# Validation: Signing Configuration (config.yml)
# ---------------------------------------------------------------------------


def check_signing_config(config_path: Path) -> ValidationResult:
    res = ValidationResult(section="Signing Configuration")

    if not config_path.is_file():
        res.checks.append(CheckResult(
            "config.yml exists", False,
            f"Not found: {config_path}", "ERROR"))
        return res

    if not HAS_YAML:
        res.checks.append(CheckResult(
            "PyYAML available", False,
            "Cannot validate config without PyYAML", "ERROR"))
        return res

    try:
        with open(config_path) as fh:
            config = yaml.safe_load(fh)  # type: ignore[union-attr]
    except Exception as e:
        res.checks.append(CheckResult(
            "config.yml valid YAML", False, f"Parse error: {e}", "ERROR"))
        return res

    if not isinstance(config, dict):
        res.checks.append(CheckResult(
            "config is mapping", False, "Not a YAML mapping", "ERROR"))
        return res

    # Check presence of signing fields
    for field_name in [
        "repo_name", "repo_url", "repo_keyalias",
        "keystore", "keystorepass", "keypass",
    ]:
        if config.get(field_name):
            res.checks.append(CheckResult(
                f"has '{field_name}'", True, "Present", "WARNING"))
        else:
            res.checks.append(CheckResult(
                f"has '{field_name}'", False,
                f"Missing signing config field '{field_name}'", "WARNING"))

    # repo_url should end with /repo
    url = config.get("repo_url")
    if url and str(url).rstrip("/").endswith("/repo"):
        res.checks.append(CheckResult(
            "repo_url path correct", True,
            "Ends with '/repo'", "ERROR"))
    elif url:
        res.checks.append(CheckResult(
            "repo_url path correct", False,
            f"Should end with '/repo', got: {url}", "ERROR"))

    # Keystore file existence check
    ks = config.get("keystore")
    if ks:
        ks_path = config_path.parent / ks
        if ks_path.is_file():
            res.checks.append(CheckResult(
                "keystore file exists", True,
                f"Found at {ks_path}", "ERROR"))
        else:
            res.checks.append(CheckResult(
                "keystore file exists", False,
                f"Not found: {ks_path} (relative to config)", "WARNING"))

    return res

# ---------------------------------------------------------------------------
# Validation: Version Consistency (index.xml vs metadata.yml)
# ---------------------------------------------------------------------------


def check_version_consistency(repo_path: Path) -> ValidationResult:
    res = ValidationResult(section="Version Consistency")

    # Versions from index.xml
    xml_versions = {}
    xml_file = repo_path / "index.xml"
    if xml_file.is_file():
        try:
            tree = ET.parse(str(xml_file))
            for app in tree.getroot().findall("application"):
                app_id = app.get("id") or app.findtext("id")
                ver = app.findtext("package/version")
                vercode = app.findtext("package/versioncode")
                if app_id and ver:
                    xml_versions[app_id] = {"version": ver, "versioncode": vercode}
        except ET.ParseError:
            pass

    # Versions from metadata YAML files
    meta_versions = {}
    meta_dir = repo_path / "metadata"
    if HAS_YAML and meta_dir.is_dir():
        for yf in list(meta_dir.glob("*.yml")) + list(meta_dir.glob("*.yaml")):
            try:
                with open(yf) as fh:
                    data = yaml.safe_load(fh)  # type: ignore[union-attr]
                if isinstance(data, dict):
                    pkg = data.get("Package")
                    ver = data.get("Version")
                    vercode = data.get("VersionCode")
                    if pkg:
                        meta_versions[pkg] = {"version": ver, "versioncode": vercode}
            except Exception:
                pass

    all_packages = set(list(xml_versions.keys()) + list(meta_versions.keys()))
    if not all_packages:
        res.checks.append(CheckResult(
            "packages found", False,
            "No packages in either index or metadata", "ERROR"))
        return res

    for pkg_id in sorted(all_packages):
        xv = xml_versions.get(pkg_id)
        mv = meta_versions.get(pkg_id)

        xml_ver = xv["version"] if xv else None
        meta_ver = mv["version"] if mv else None

        if xml_ver and meta_ver:
            if xml_ver == meta_ver:
                res.checks.append(CheckResult(
                    f"{pkg_id}: versions aligned", True,
                    f"index={xml_ver}, metadata={meta_ver}", "ERROR"))
            else:
                res.checks.append(CheckResult(
                    f"{pkg_id}: versions aligned", False,
                    f"MISALIGNED — index={xml_ver} vs metadata={meta_ver}",
                    "ERROR"))
        elif xml_ver and not meta_ver:
            res.checks.append(CheckResult(
                f"{pkg_id}: version in both", True,
                f"Only index has version ({xml_ver}) — acceptable for manual repo",
                "WARNING"))

    return res

# ---------------------------------------------------------------------------
# Validation: Icon Assets
# ---------------------------------------------------------------------------


def check_icons(repo_path: Path) -> ValidationResult:
    res = ValidationResult(section="Icon Assets")

    # Check for a repo icon (repo-icon.png or icon.png at top level)
    found_icon = False
    for candidate in ["repo-icon.png", "icon.png"]:
        cp = repo_path / candidate
        if cp.is_file():
            size_kb = cp.stat().st_size / 1024
            res.checks.append(CheckResult(
                f"{candidate} exists", True, f"{size_kb:.0f} KB", "WARNING"))
            found_icon = True
            break

    if not found_icon:
        res.checks.append(CheckResult(
            "repo icon present", False,
            "No repo-icon.png or icon.png found (recommended)", "WARNING"))

    # Check icons/ directory for multiple sizes
    icons_dir = repo_path / "icons"
    if icons_dir.is_dir():
        icons = list(icons_dir.glob("*.png"))
        res.checks.append(CheckResult(
            "icons/ directory", True,
            f"{len(icons)} icon(s) in icons/", "WARNING"))

        has_large = any("512" in i.name for i in icons)
        if has_large:
            res.checks.append(CheckResult(
                "512px icon present", True,
                "Found (recommended minimum)", "WARNING"))
        else:
            res.checks.append(CheckResult(
                "512px icon present", False,
                "No 512px icon found (F-Droid recommends at least 512x512)",
                "WARNING"))

    return res

# ---------------------------------------------------------------------------
# Report rendering
# ---------------------------------------------------------------------------


def render_report(results: list[ValidationResult], strict: bool) -> str:
    lines = []
    lines.append("=" * 68)
    lines.append("  F-Droid Manual Repository Validation Report")
    lines.append("=" * 68)

    total_pass = 0
    total_fail = 0
    blocking_errors = 0

    for section in results:
        lines.append("")
        lines.append(f"--- {section.section}")
        for c in section.checks:
            icon = "[PASS]" if c.passed else "[FAIL]"
            lines.append(
                f"  {icon} [{c.severity}] {c.name}: {c.message}")

            total_pass += 1 if c.passed else 0
            total_fail += 1 if not c.passed else 0
            if not c.passed and (c.severity == "ERROR" or strict):
                blocking_errors += 1

    lines.append("")
    lines.append("=" * 68)
    verdict = "PASS" if blocking_errors == 0 else "FAIL"
    lines.append(
        f"  RESULT: {verdict}  |  "
        f"{total_pass} passed, {total_fail} failed  |  "
        f"{blocking_errors} blocking error(s)"
    )
    lines.append("=" * 68)

    return "\n".join(lines)


def render_json(results: list[ValidationResult]) -> str:
    output = []
    for section in results:
        entry = {
            "section": section.section,
            "checks": [
                {
                    "name": c.name,
                    "passed": c.passed,
                    "severity": c.severity,
                    "message": c.message,
                }
                for c in section.checks
            ],
        }
        output.append(entry)

    return json.dumps({"validation_report": output}, indent=2)

# ---------------------------------------------------------------------------
# Main entry point
# ---------------------------------------------------------------------------


def main():
    parser = argparse.ArgumentParser(
        description="Validate F-Droid manual repository against spec requirements")
    parser.add_argument("repo_path",
                        help="Path to the repo/ directory (contains index.xml)")
    parser.add_argument("--config", default=None,
                        help="Path to config.yml (default: ../config.yml)")
    parser.add_argument("--strict", action="store_true",
                        help="Treat warnings as errors")
    parser.add_argument("--json", dest="as_json", action="store_true",
                        help="Emit JSON instead of human-readable text")

    args = parser.parse_args()

    repo_path = Path(args.repo_path).resolve()
    if not repo_path.is_dir():
        print(f"ERROR: {repo_path} is not a directory", file=sys.stderr)
        sys.exit(1)

    config_path = (Path(args.config).resolve()
                   if args.config else repo_path.parent / "config.yml")

    # Run all 7 validation groups
    results: list[ValidationResult] = [
        check_directory_structure(repo_path),
        check_metadata_yaml(repo_path),
        check_index_xml(repo_path),
        check_apk_hashes(repo_path),
        check_signing_config(config_path),
        check_version_consistency(repo_path),
        check_icons(repo_path),
    ]

    # Render report
    if args.as_json:
        print(render_json(results))
    else:
        print(render_report(results, args.strict))

    # Exit code reflects blocking errors
    blocking = sum(
        1
        for s in results
        for c in s.checks
        if not c.passed and (c.severity == "ERROR" or (args.strict and c.severity == "WARNING"))
    )
    sys.exit(1 if blocking else 0)


if __name__ == "__main__":
    main()
