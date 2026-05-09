# VoidScanner v1.1.03

## Fixed Issues

### Version Sync
- Fixed app version display showing "1.1.00" instead of "1.1.02"
- Updated all version references to use gradle.properties configuration
- Now shows correct "VoidScanner 1.1.03"

### Export Function
- Fixed JsonExporter to actually export collected WiFi and sensor data
- Removed placeholder anonymized data; now exports real scan results
- Added WRITE_EXTERNAL_STORAGE permission for broader compatibility
- Improved file write verification in Downloads directory

### Data Collection
- WiFi networks now properly exported with SSID, BSSID, signal level, frequency, and capabilities
- Sensor metadata includes available sensors and device info
- Exported JSON file in VoidScanner folder within Downloads directory

## Installation

Download the APK from the [GitHub releases page](https://github.com/BuboTheWise/VoidScanner/releases/tag/v1.1.03).

**App Version:** 1.1.03  
**Minimum Android Version:** Android 8.0 (API 26)  
**Target Android Version:** Android 14 (API 34)
