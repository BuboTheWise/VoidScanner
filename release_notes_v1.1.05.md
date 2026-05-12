# Void Scanner v1.1.05 - F-Droid Repository Setup

## Release Date
2026-05-12

## Summary
Added GitHub-hosted F-Droid repository setup documentation, enabling users to manually add void scanner without formal F-Droid review queue delays.

## What's New

### F-Droid Integration
- **Added FDROID_SETUP.md**: Complete guide for setting up GitHub Actions workflow
  - Creates automatic Android APK builds via CI/CD
  - Publishes signed APKs to GitHub Releases
  - F-Droid-compatible repository setup instructions
  - Manual and CLI installation methods
  - Security and fingerprint configuration

- **Added INSTALL.md**: User-facing installation guide
  - APK download from GitHub Releases
  - F-Droid repository addition via Desktop/CLI
  - References to comprehensive F-Droid setup documentation

### Version Management
- Enhanced version file handling
- Improved gradle build configuration
  
### Documentation Updates
- Updated README with F-Droid setup links
- Removed obsolete release_notes_v1.1.03.md

## Release APK
- Automatic build via GitHub Actions
- Uploaded as `void-scanner-v1.1.05.apk` to GitHub Releases

## Usage

### For Users - Manual Installation
1. Download `void-scanner-v1.1.05.apk` from GitHub Releases
2. If your device blocks unknown sources, enable in Settings
3. Install the APK

### For Users - F-Droid Installation
1. Add `https://github.com/BuboTheWise/VoidScanner` via F-Droid Desktop or CLI
2. F-Droid will automatically track updates

See [FDROID_SETUP.md](FDROID_SETUP.md) for detailed F-Droid configuration.

## Benefits
- Users get immediate access to latest versions (no queue delays)
- Open source, transparent development
- Direct feedback channel via GitHub issues
- Community-driven quality assurance

## Technical Details
- API Compatibility: Android 14+ (API 34)
- Build System: Gradle with Android SDK 34
- Minimum SDK: API 29 recommended
