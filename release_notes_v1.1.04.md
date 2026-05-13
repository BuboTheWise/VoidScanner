# Void Scanner v1.1.04 - Android Compatibility Release

## Release Date
2026-05-09

## Summary
Enhanced Android SDK 34 compatibility with comprehensive sensor collection improvements and UI refinements.

## What's New

### Android 14 (API 34) Compatibility
- Updated Compile SDK to 34
- Updated Target SDK to 34
- Optimized for Android 14 restrictions
- Improved runtime compatibility

### Sensor Improvements
- WiFi scanning optimizations with Android 10+ NEARBY_WIFI_DEVICES permission
- Enhanced Bluetooth RSSI aggregation
- Location services with coarse location fallback
- Environmental sensors (Light, Pressure, Temperature, Humidity)
- IMU sensors (Accelerometer, Gyroscope)
- System metadata collection

### Storage Updates
- Storage permissions improved with runtime request
- Export functionality enhanced for external data analysis
- JSON export with comprehensive scan data

### UI/UX
- Dark theme maintained for scanner interface
- Material Design 3 components
- Better permission request UX
- Scan completion feedback

## Technical Details

### Build Info
- **Version**: 1.1.03
- **Build Type**: Release (Signed)
- **APK Size**: 5.0 MB
- **Keystore**: voidscanner.keystore (RSA 4096-bit)
- **Java**: OpenJDK 17.0.19

### Minimum Requirements
- **Android**: 8.0+ (API 26)
- **Recommended**: Android 10+ (API 29)

### Permissions
- `ACCESS_FINE_LOCATION` - GPS and environment mapping
- `NEARBY_WIFI_DEVICES` - WiFi scanning (Android 10+)
- `BLUETOOTH` - Bluetooth device detection

## Installation

### From Play Store
Coming soon...

### F-Droid
See: https://f-droid.org/packages/com.bubo.voidscanner/

### Manual Installation
1. Download `app-release.apk`
2. Enable "Unknown Sources" if needed
3. Install APK
4. Grant required permissions

## Known Issues
- Some older Android devices may have limited sensor availability
- Location permissions require user interaction
- Background scans require special permissions on Android 12+

## Changelog

### v1.1.04
- Updated SDK to API 34
- Fixed API deprecation warnings
- Storage permission improvements
- Improved error handling

### v1.1.02
- Initial Android 10+ support
- WiFi + Bluetooth sensors
- Basic entity generation

### v1.0.x
- Core Android compatibility
- Basic UI framework
- Sensor collection framework

---

**Developer**: BuboTheWise (@BuboTheWise)
**Repository**: https://github.com/BuboTheWise/void-scanner
**License**: TBD (F-Droid compatible)