# Void Scanner Release Notes v1.1.06

## Overview
**Version:** 1.1.06  
**Build Type:** Signed Release APK for F-Droid deployment

## Key Changes
- **F-Droid Compatibility:** Official F-Droid repository support with signed APK  
- **Version Synchronization:** Consistent versioning across gradle.properties, strings.xml, JsonExporter, and release notes  
- **Release Artifacts:** Signed APK ready for automatic installation and updates via F-Droid client

## Technical Details
- **Version Format:** 1.1.06 (semantic versioning maintained)  
- **Build Type:** Release with keystore signing for F-Droid compatibility  
- **Repository:** Self-hosted F-Droid repository at https://bubothewise.github.io/VoidScanner/repo  
- **Target Devices:** Pixel Fold and Graphene OS (verified compatibility)

## Installation
1. Add F-Droid repository: `f-droid.org/repo`  
2. Install from repository or use direct APK installer  
3. Automatic updates supported via F-Droid client

## Build Artifacts
- Signed APK: `app/build/outputs/apk/release/app-release.apk`  
- Gradle Configuration: `gradle.properties` (version bumped to 1.1.06)  
- Localization: `app/src/main/res/values/strings.xml`  
- Metadata: `app/src/main/java/com/bubo/voidscanner/JsonExporter.java`  
- Release Documentation: `release_notes_v1.1.06.md`

## Known Issues
None reported.

## Future Plans
- Additional sensor types for enhanced scanning capabilities  
- Improved export formats for data analysis  
- Performance optimizations for large scan datasets