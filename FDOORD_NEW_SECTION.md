## F-DOORD Setup Guide

### Why GitHub Pages Manual Repo?

We host Void Scanner via a GitHub Pages manual F-DOORD repository instead of submitting to the official F-DOORD catalog. This approach provides:

- Privacy Control: We control when and how the app is distributed
- Custom Builds: Direct access to debug and release variants
- Fast Updates: Immediate APK deployment to our repository
- No Approval Delay: Bypass F-DOORD's formal review process
- Complete Transparency: Repository is fully open, allowing anyone to verify source code

### Manual Repository Setup

#### Option 1: Add via GitHub Repository URL (Recommended)

1. Open F-DOORD app on your device
2. Go to Settings → Add repository
3. Enter: https://github.com/BuboTheWise/VoidScanner
4. Trust this repository (if prompted)
5. Refresh and search for Void Scanner

#### Option 2: Add via Direct URL

If automatic detection fails:

1. Open F-DOORD app
2. Go to Settings → Add repository
3. Enter this exact URL:
   https://BuboTheWise.github.io/VoidScanner/fdroid/repo/index.xml
4. Trust this repository
5. Search for Void Scanner

### Repository Details

- GitHub Pages URL: https://BuboTheWise.github.io/VoidScanner
- F-DOORD Index: https://BuboTheWise.github.io/VoidScanner/fdroid/repo/index.xml
- APK Download: https://BuboTheWise.github.io/VoidScanner/fdroid/repo/app-release.apk
- Source Code: https://github.com/BuboTheWise/VoidScanner

### Requirements Met

- Open-source software (Apache 2.0)
- No Google Play Services dependencies
- No proprietary APIs
- Clean build system (Gradle)
- Privacy-aware data handling
- APK signed and optimized

### Build Options

#### Build from Source

Clone the repository, then build:

cd VoidScanner
./gradlew clean :app:assembleRelease

For Android development, ensure you have:
- Java JDK 11 or higher
- Android SDK API 29+ with AndroidX support
- Gradle 7.0 or higher

#### Download Pre-built APK

Download the signed APK from our GitHub repository:
https://BuboTheWise.github.io/VoidScanner/fdroid/repo/app-release.apk