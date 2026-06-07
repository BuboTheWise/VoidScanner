# Void Scanner

Privacy-focused Android application for network and device analysis.

## Version 1.028

### New Features

- **Enhanced UI Feedback**: Progress indicators and toast notifications during scanning
- **Better Error Handling**: Clear, actionable error messages
- **Improved Permissions**: Explanations for required permissions

### Bug Fixes

- Fixed export directory issues on Graphene OS
- Improved WiFi scanning reliability
- Enhanced Bluetooth discovery handling

## Installation Options

### From F-Droid App (Manual Repository)

The F-Droid repository is **manual** (hosted on GitHub Pages). To use Void Scanner via F-Droid:

1. **Add Repository** (Method 1 - GitHub Repository URL recommended):
   ```
   https://github.com/BuboTheWise/VoidScanner
   ```

2. **Refresh Repository** (bottom of Settings → Repository)

3. **Trust Repository** (if prompted)

4. **Install Void Scanner v1.028**

**Alternative Method (Index URL)**:
If the automatic detection doesn't work, add the direct index URL instead:
   ```
   https://BuboTheWise.github.io/VoidScanner/fdroid/repo/index.xml
   ```

**Note**: This is a manual GitHub Pages repository, not the official F-Droid catalog. Updates can be pushed immediately without F-Droid review.

### From GitHub Releases

1. Visit [Releases](https://github.com/BuboTheWise/VoidScanner/releases)
2. Download latest APK
3. Enable "Unknown Sources" in Android settings
4. Install the APK

## Features

- **WiFi Scanning**: Network scanning with BSSID, Signal Strength, Channel info
- **Bluetooth Discovery**: Device discovery with RSSI measurements
- **Sensor Data**: Device sensors collection
- **JSON Export**: Export results to `/sdcard/Downloads/VoidScanner/`
- **Real-time Status**: Live feedback during operations

## Screen Recording

![Void Scanner Demo](https://github.com/BuboTheWise/VoidScanner/assets/your-username/your-video-id)

## Permissions Required

| Permission | Purpose |
|------------|---------|
| Location (Fine/Coarse) | WiFi and Bluetooth scanning |
| Storage | JSON file export to Downloads folder |

## Getting Help

- GitHub Issues: https://github.com/BuboTheWise/VoidScanner/issues
- Source Code: https://github.com/BuboTheWise/VoidScanner

## Build & Development

```bash
cd ~/.hermes/workspace/Code/VoidScanner
./gradlew assembleRelease
```

**Target Platform**: Android 8.0+ (API 26+)
**Tested on**: Graphene OS 09291FDD4000, Android 10+

## License

Apache License 2.0

***

**Version**: 1.028
**Release Date**: 2026-06-06