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

APK_DIR="app/build/outputs/apk/debug/app-debug.apk"
APK_NAME=""
VERSION="$(grep APP_VERSION_NAME gradle.properties | cut -d'=' -f2)"
BUILD_TYPE="debug"
DEST_DIR="fdroid/repo"

while [[ $# -gt 0 ]]; do
  case "$1" in
    --version) VERSION="$2"; shift 2 ;;
    --release) BUILD_TYPE="release"; APK_DIR="app/build/outputs/apk/release/app-release-unsigned.apk"; shift ;;
    --debug)   BUILD_TYPE="debug"; APK_DIR="app/build/outputs/apk/debug/app-debug.apk"; shift ;;
    *) echo "Unknown arg: $1"; exit 1 ;;
  esac
done

APK_NAME="void-scanner-${VERSION}-${BUILD_TYPE}.apk"
APK_PATH="$APK_DIR"

# Validate APK exists
if [[ ! -f "$APK_PATH" ]]; then
  echo "No APK found at $APK_PATH. Run './gradlew assemble${BUILD_TYPE}' first." >&2
  exit 1
fi

cd fdroid/repo

# Copy APK + verify
cp ../../"$APK_PATH" "$APK_NAME"
echo "Added: $APK_NAME ($(du -h "$APK_NAME" | cut -f1))"

# Validate
if python3 ../validate_fdroid_repo.py; then
  echo "Validation passed ✓"
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
