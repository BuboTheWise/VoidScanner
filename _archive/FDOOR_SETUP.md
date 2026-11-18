# F-Droid Repository Setup for Void Scanner

This document explains how to set up F-Droid to auto-build and update Void Scanner.

## Repository Structure

```
fdroid/
├── data/
│   └── metadata/
│       └── com.bubo.voidscanner.yml  # Application metadata
└── repo/
    └── metadata/
        └── com.bubo.voidscanner.yml  # Metadata for repo
```

## GitHub Actions

The F-Droid CI workflow in `.github/workflows/fdroid-ci.yml` automatically:
- Builds the release APK on push to main branch
- Copies APK to `fdroid/repo/` directory
- Commits and pushes changes to the repository

## Using the Repository

Once pushed to GitHub, users can add Void Scanner to F-Droid by:
1. Opening F-Droid app
2. Settings → Add new repository
3. URL: `https://github.com/BuboTheWise/VoidScanner`
4. Tap OK

## Manual Testing

To test locally:

```bash
# Build release APK
./gradlew :app:assembleRelease

# Copy to repo
cp ./app/build/outputs/apk/release/app-release-unsigned.apk ./fdroid/repo/

# Verify
ls -lh ./fdroid/repo/app*.apk
```

## Version Management

Version properties in `gradle.properties`:
```properties
APP_VERSION_MAJOR=1
APP_VERSION_MINOR=01
APP_VERSION_PATCH=05  # Currently 05
```

Update metadata.yml manually on each version bump for F-Droid consistency.