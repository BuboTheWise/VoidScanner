# VoidScanner — Development Setup Guide

## Prerequisites
- **Linux**: `android-tools`, `jdk17-openjdk`, `fdroidserver` (from AUR: `pacaur -S fdroidserver`)
- **macOS**: [Android SDK Command Line Tools](https://developer.android.com/studio#command-tools), JDK 17
- **Windows**: Android Studio + CLI tools, JDK 17

## Quick Setup (First-time)

```bash
cd VoidScanner

# 1. Install Android SDK command-line tools via system package manager
sudo pacman -S android-tools jdk17-openjdk    # Arch Linux
brew install --cask android-sdk-command-line-tools   # macOS

# 2. Create local.properties with your SDK path
echo 'sdk.dir=/path/to/your/Android/Sdk' > local.properties
# Verify: sdkmanager should work
$HOME/Android/Sdk/cmdline-tools/latest/bin/sdkmanager --list_installed

# 3. Accept Android licenses
yes | $ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager --licenses

# 4. Install required SDK components (just once)
${ANDROID_HOME}/cmdline-tools/latest/bin/sdkmanager \
  --install "platforms;android-34" "build-tools;34.0.0" "platform-tools"
```

## Building the App

### Debug build (development, pushes to phone instantly)
```bash
./gradlew assembleDebug          # Builds APK at app/build/outputs/apk/debug/app-debug.apk
adb install -r app/build/outputs/apk/debug/app-debug.apk  # Push to device
adb shell "dumpsys package com.bubo.voidscanner | grep version"  # Verify installed version
```

### Release build (for F-Droid repo / distribution)
The release APK uses `app/voidscanner.keystore` (already committed to the repo).  
**Never share this keystore publicly.** For production releases:
- Revoke/rotate the public key fingerprint before anyone gets access
- See section below for adding the keystore to the repo

### Verify the installed build
```bash
adb shell pm list packages com.bubo.voidscanner
# Should show: package:com.bubo.voidscanner
```

## F-Droid Repository Management

### Manual repo setup (current)
The repo lives in `fdroid/repo/` and is served via [GitHub Pages](https://BuboTheWise.github.io/VoidScanner/fdroid/repo/) at the URL:
```
https://BuboTheWise.github.io/VoidScanner/fdroid/repo/
```

To add a new APK to the repo:

```bash
# 1. Build the release APK from this tree branch
cd fdroid/repo

# 2. Add the new APK (e.g., void-scanner-2.0.0.apk) here

# 3. Update index.xml with the new APK hash + signature
fdidx --update

# 4. Verify everything is self-consistent
python3 ../validate_fdroid_repo.py
```

This **pushes** to GitHub (triggers Pages deployment automatically via CI). If you want to test locally first:
```bash
cd fdroid/repo
curl -sk https://raw.githubusercontent.com/BuboTheWise/VoidScanner/main/fdroid/repo/index.xml | python3 -c "
import xml.etree.ElementTree as ET, sys
ET.parse(sys.stdin)
print('XML is valid ✓')
"
```

### Adding the repo on client phones
1. Install [F-Droid](https://f-droid.org) from Google Play / F-Droid website  
2. Open F-Droid → Settings → Manage repositories or Trusted repos
3. Tap '+' and paste: `https://BuboTheWise.github.io/VoidScanner/fdroid/repo/`

### Verifying repository structure (before publish)
```bash
cd fdroid/repo
python3 ../validate_fdroid_repo.py   # checks index.xml + APK signatures
# All must show 'passed' before committing to main
```

## CI / GitHub Pages Deployment

The repo uses [GitHub Actions](https://github.com/BuboTheWise/VoidScanner/actions) for automated F-Droid updates triggered by pushes to:
- `app/build.gradle` (version bump)
- `fdroid/**` (repo changes, metadata updates)

**Prerequisites**:
1. Enable Pages in Settings → Pages → Source = Actions
2. Add secret `GITHUB_PAGES_TOKEN` with `write` permission  
3. Configure signing keys as secrets (`KEYSTORE_PASS`, `KEY_ALIAS`)

## Project Structure

```
VoidScanner/
├── app/                          # Android application source
│   ├── build.gradle              # Build config (version codes)
│   ├── voidscanner.keystore      # Signing keystore PKCS12 → MUST NOT be shared publicly.
│   ├── proguard-rules.pro        # Release shrink rules
│   └── src/                      # Source files (Kotlin / Java)
├── fdroid/                       # F-Droid manual repo
│   ├── config.yml                # Repository metadata (pubkey, alias)
│   ├── repo/                     # ** Published artifacts **  
│   │   ├── index.xml             # Auto-generated — always update with `fdidx --update` after adding APKs.
│   │   ├── void-scanner-1.2.00.apk  # Current release APK (signed)
│   │   └── metadata/            # Per-app metadata YAML
│   ├── validate_fdroid_repo.py  # Pre-commit validator script
│   └── tmp/                     # Local tool artifacts (gitignored)
├── gradle.properties             # Build-level properties (version codes, keys)
├── local.properties              # LOCAL ONLY — sdk.dir = absolute path to Android SDK
└── .github/workflows/fdroid.yml  # CI: builds APK + updates repo index
```

## Common Issues & Fixes

### "SDK manager not found" / "sdk.dir not set"
→ Add `local.properties` with the **absolute** path to your SDK directory. Do NOT use `$HOME` or relative paths — Gradle does not expand environment variables in this file.

### "Java version mismatch"
→ This project requires Java 17 (JDK 17). Set `export JAVA_HOME=/path/to/jdk-17` before building. Verify with: `java -version`

### APK signing errors on release builds
→ The keystore in the repo (`app/voidscanner.keystore`) uses password `void123`. If you need to regenerate it:  
```bash
keytool -genkeypair \
  -alias voidscanner -keypass void123 \
  -keystore app/voidscanner.keystore -storepass void123 \
  -keyalg RSA -keysize 2048 -validity 10000 \
  -dname "CN=VoidScanner, OU=Nethervoid, O=VoidScanner Team" \
  -ext "BC=ca:true"
```

### F-Droid can't find or verify the APK
→ Ensure all required files exist in `fdroid/repo/` (index.xml + APK + .idsig + metadata). Always run `valid fdroid_repo.py` before pushing. Check that Content-Type for index.xml is **application/xml** and for APKs is **application/vnd.android.package-archive**.

### GitHub Pages not updating
→ Push to `main` after changing `fdroid/repo/` files directly or running the CI workflow manually at https://github.com/BuboTheWise/VoidScanner/actions/workflows/fdroid.yml
