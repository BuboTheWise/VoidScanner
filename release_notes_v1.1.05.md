# Void Scanner v1.1.05 - Release Notes

## Release Date
2026-05-12

## Summary
Fixed critical issues in version 1.1.04 and improved F-Droid repository setup.

## What's Fixed

### Export Filename Correction (CRITICAL)
- **Before**: Exported files incorrectly named `veilscan-export-*.json`
- **After**: Correctly named `voidscanner-export-*.json`
- Fixed in:

### Graphene OS Storage Issue (CRITICAL)
- **Issue**: Directory exists at `/Downloads/VoidScanner/` but save operation failed silently
- **Fix**: Improved file write validation and error handling
- Verified: File now correctly saved to Downloads folder on all tested devices

### F-Droid Repository Setup
- Enhanced GitHub Actions workflow for automatic APK deployments
- Added proper APK naming convention: `void-scanner-1.1.05.apk`
- Verified F-Droid repo accessibility via GitHub Pages

## Technical Details

### Changed Files
- `JsonExporter.java` - Export filename logic
- `app/build.gradle` - F-Droid deployment configuration
- `.github/workflows/fdroid.yml` - CI/CD pipeline

### Test Results
- ✅ File naming: `voidscanner-export-<timestamp>.json`
- ✅ Downloads folder creation: Automatic
- ✅ Write permissions: Handled for Android 10+
- ✅ Empty directory fallback: Graceful handling when directory missing

### APK Build
- **Build Type**: Signed Release APK
- **Size**: 5.0 MB
- **Platform**: Android 10+ (API 29+)
- **Signature**: Keystore-verified

## Installation

### Manual Installation
1. Download `void-scanner-1.1.05.apk` from GitHub Releases
2. Enable "Unknown Sources" in Android settings
3. Install APK
4. Grant required permissions (Location, Nearby Devices, Bluetooth)

### F-Droid Installation
1. Add repository: `https://BuboTheWise.github.io/VoidScanner/fdroid/repo/index.xml`
2. Search for "VoidScanner"
3. Install
4. Automatic updates enabled

## Known Issues
None

## Changelog

### v1.1.05
- ✅ Export filename corrected to `voidscanner-export-*.json`
- ✅ Fixed Graphene OS file save issue (directory + write validation)
- ✅ F-Droid repository workflow improvements
- ✅ APK deployment automation

### v1.1.04
- Enhanced Android SDK 34 compatibility
- Sensor collection improvements
- Storage permission enhancements

---

**Developer**: BuboTheWise (@BuboTheWise)
**Repository**: https://github.com/BuboTheWise/VoidScanner
**License**: GPL-3.0
