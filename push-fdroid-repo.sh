#!/usr/bin/env bash
# push-fdroid-repo.sh — Update the F-Droid manual repo and deploy to Pages
# Usage: ./push-fdroid-repo.sh [--version X.Y.Z] [--release/--debug]
set -euo pipefail

# Support being run from repo root or fdroid/repo dir
if [[ -f "fdroid/repo/" ]]; then
  REPO_ROOT="$(pwd)"
elif [[ -d "../fdroid/validate_fdroid_repo.py" ]]; then
  REPO_ROOT="$(cd .. && pwd)"
  cd ../
else
  echo "Error: not in repo root or fdroid/repo. Run from VoidScanner/ root." >&2; exit 1
fi

cd "$REPO_ROOT"

# Defaults to release build (appropriate for F-Droid manual repo)
BUILD_TYPE="release"
BUILD_ARGS=""
DEST_DIR="fdroid/repo"

while [[ $# -gt 0 ]]; do
  case "$1" in
    --version) VERSION="$2"; shift 2 ;;
    --release) BUILD_TYPE="release"; shift ;;
    --debug)   BUILD_TYPE="debug"; shift ;;
    --build-args) BUILD_ARGS="$2"; shift 2 ;;
    *) echo "Unknown arg: $1"; exit 1 ;;
  esac
done

# Derive version from gradle.properties if not provided
if [[ -z "$VERSION" ]]; then
  MAJOR="$(grep APP_VERSION_MAJOR gradle.properties | cut -d'=' -f2)"
  MINOR="$(grep APP_VERSION_MINOR gradle.properties | cut -d'=' -f2)"
  PATCH="$(grep APP_VERSION_PATCH gradle.properties | cut -d'=' -f2)"
  VERSION="${MAJOR}.${MINOR}.${PATCH}"
fi

# Determine APK path based on build type
if [[ "$BUILD_TYPE" == "release" ]]; then
  APK_PATH="app/build/outputs/apk/release/app-release-*.apk"
else
  APK_PATH="app/build/outputs/apk/debug/app-debug.apk"
fi
APK_NAME="void-scanner-${VERSION}-${BUILD_TYPE}.apk"

# Find actual APK file (may use glob)
ACTUAL_APK=$(ls "$APK_PATH" 2>/dev/null | head -1)
if [[ ! -f "$ACTUAL_APK" ]]; then
  echo "No APK found at $APK_PATH. Run './gradlew assemble${BUILD_TYPE}' first." >&2
  exit 1
fi

cd fdroid/repo

# Copy APK + verify
cp -- "$ACTUAL_APK" "$APK_NAME"
echo "Added: $APK_NAME ($(du -h "$APK_NAME" | cut -f1))"

# Update repo index (hash, size, version) in index.xml, index-v2.json, metadata YAML
REPO_ROOT="${PWD}"
cd "$REPO_ROOT/../.."  # back to VoidScanner/ root
python3 fdroid/repo/update_repo_index.py --apk "fdroid/repo/$APK_NAME"

# Validate before committing
echo ""
if python3 ../validate_fdroid_repo.py; then
  echo "Repo validation passed ✓"
else
  echo "WARNING: Validation failed — fix before committing! ✗" >&2
fi

git add -f fdroid/repo/

# Idempotency guard
DIFF=$(git diff --cached --stat)
if [[ -z "$DIFF" ]]; then
  echo "No changes to commit."
else
  git commit -m "F-Droid: Update repo to version $VERSION ($BUILD_TYPE)"
  if [[ -n "${CI:-}" || -z "${SKIP_PUSH:-}" ]]; then
    git push origin main
    echo "Pushed ✓"
  else
    echo "(Set SKIP_PUSH=1 to preview without pushing.)"
  fi
fi
