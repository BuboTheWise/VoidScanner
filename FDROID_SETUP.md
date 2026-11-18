## F-Droid Compatibility

This app is hosted via GitHub Pages as a **manual F-Droid repository**. This allows users to manually add and trust the repository, but excludes the app from the official F-Droid catalogs.

### Manual F-Droid Repository Setup

If you want to use Void Scanner from an F-Droid repository interface with additional safeguards:

#### Option 1: Add via GitHub Repository URL (Recommended)

1. Open F-Droid app
2. Settings → **Add repository**
3. Enter repository URL:
   ```
   https://github.com/BuboTheWise/VoidScanner
   ```
4. Trust this repository (optional, if prompted)
5. Refresh the repository to see Void Scanner in the list

#### Option 2: Add via Direct URL (Direct Index)

If the automatic detection doesn't work:

1. Open F-Droid app
2. Settings → **Add repository**
3. Enter URL:
   ```
   https://BuboTheWise.github.io/VoidScanner/fdroid/repo/index.xml
   ```
4. Trust this repository
5. Search for "Void Scanner" and install

### Repository Verification

- **GitHub Pages URL**: https://BuboTheWise.github.io/VoidScanner
- **F-Droid Index XML**: https://BuboTheWise.github.io/VoidScanner/fdroid/repo/index.xml
- **GitHub Repository**: https://github.com/BuboTheWise/VoidScanner
- **APK Repository**: https://BuboTheWise.github.io/VoidScanner/fdroid/repo/app-release.apk

### Why Manual Repository?

We chose to host Void Scanner via a manual F-Droid repository instead of submitting to the F-Droid catalog because:

1. **Privacy Focus**: We can control when and how the app is distributed
2. **Custom Builds**: Users can easily access different build variants (debug vs. release)
3. **No Approval Wait**: No need to pass through F-Droid's formal review process
4. **Direct Updates**: We can push updates immediately to our repository
5. **Open Source Commitment**: The repository is fully open, allowing anyone to verify the source code

### Build and Installation

#### Build from Source

```bash
# Clone the repository
git clone https://github.com/BuboTheWise/VoidScanner.git
cd VoidScanner

# Build Release APK
./gradlew clean :app:assembleRelease

# Install to device
adb install app/build/outputs/apk/release/app-release.apk
```

#### Download APK

The packaged APK (signed release version) can be downloaded from:
```
https://BuboTheWise.github.io/VoidScanner/fdroid/repo/app-release.apk
```

### Status: GitHub Pages Manual F-Droid Deployed ✅

**Repository**: Hosted on GitHub Pages, fully accessible, no formal submission to F-Droid required