# Fix: Versioned APK filenames for proper F-Droid sync

## Problem
APK files use generic names (`app-release.apk` or `void-scanner.apk`) without version information, making it impossible for users and F-Droid to know what version is being downloaded.

## Solution
Generate versioned APK filenames matching format: `void-scanner-{VERSION_NAME}.apk` or `com.bubo.voidscanner-{VERSION_NAME}.apk`

## Changes Required

### 1. app/build.gradle - VersionName output
Update versionName() to properly reference APP_VERSION_NAME from gradle.properties

### 2. .github/workflows/fdroid.yml - APK filename
- Use versioned APK: `void-scanner-{VERSION_NAME}.apk`
- Reference in fdroid/repo directory
- Update index generation

### 3. fdroid/repo/index.xml generation
- Reference versioned APK in index
- Ensure UpdateMode matches AutoUpdateMode

### 4. fdroid/config.yml - repo URL ending
Keep `repo_url: https://BuboTheWise.github.io/VoidScanner/fdroid/repo/`

## Implementation Plan

1. Read gradle.properties version info
2. Build with versioned APK naming
3. Copy to fdroid/repo/{versioned-name}.apk
4. Run fdroid update to regenerate index
5. Commit and push with version info in message