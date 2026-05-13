# Void Scanner

The core sensor data collection engine for the **Nethervoid Network** — a parallel dimension game layer exploring the void between networked devices.

> *"Peer into the nethervoid all around us. Decode the signatures left by passing devices. Reveal the echoes of existence that flow through the air and network."*

## Overview

Void Scanner is an isolated Android application that exhaustively collects WiFi and Bluetooth sensor data offline-first — the foundation for procedural generation and world-building in the Nethervoid experience. Unlike traditional scanning tools, Void Scanner treats every signal as a signature in the void, creating a deterministic fingerprint of the environment.

### Integration Context

Void Scanner serves as the **Void Resonator** component in the Nethervoid Network. The scanner:
1. Emits **deterministic seeds** from sensor data to generate game entities (planned)
2. Provides real-time feedback and post-scan summarization for the Nethervoid game layer
3. Operates independently as a test platform, then integrates into the full Nethervoid application

## Features

### Sensor Collection
- **WiFi Scouting**: Exhaustive network scan with SSID, BSSID, RSSI, frequency, security type
- **Bluetooth Resonance**: BLE beacon detection with device addresses and signal strength
- **Timestamped Exports**: Every scan produces a traceable JSON record
- **Privacy-Aware**: Real builds anonymize PII; debug builds include raw data

### Export Capability
- **Downloads Folder Export**: JSON files saved to `Downloads/VoidScanner/`
- **Metadata Tracking**: Timestamp, device model, Android version, app version
- **Anonymization**: SSID/BSSID hashed; location coordinates obfuscated in release builds

### Design Philosophy
- **Offline-First**: Works without network, entirely local to device
- **Deterministic Generation**: Sensor fingerprints drive procedural content (planned)
- **Sensor-Mesh Architecture**: Every signal is a possible entity or anchor in the void
- **Test-Isolated**: Separated package structure enables independent development and iteration

## Technical Details

### Core Components
```
com.bubo.voidscanner/
├── MainActivity.java          # UI control and orchestrator
├── WifiScout.java            # WiFi network enumeration
├── BluetoothScanner.java      # BLE device detection
├── JsonExporter.java         # JSON persistence to Downloads folder
└── GoogleSheetsExporter.java  # Export placeholder (OAuth not yet implemented)
```

### Versioning

Void Scanner uses **semantic versioning (SemVer)** with leading zero padding for minor revisions (00-99):

| **Current Version**: 1.1.04 |
| **Semantic Version Format**: `X.Y.Z` |
  - **X (MAJOR)**: Breaking changes, major features
  - **Y (MINOR)**: New features, compatible additions (padded to 2 digits: 01-99)
  - **Z (PATCH)**: Bug fixes, minor improvements (padded to 2 digits: 00-99) |
- **Version Code**: Auto-incremented based on semantic ordering
- **Target API**: Android 10+ (API 29+)
- **Package Name**: `com.bubo.voidscanner`

#### Version Examples

| Version | Meaning | Change Type |
|---------|---------|-------------|
| 1.1.04 → 1.02.00 | New feature release, breaking API additions | Minor increment, PATCH reset |
| 1.1.04 → 1.01.01 | Bug fix only, no API changes | PATCH increment |

See [Semantic Versioning](https://semver.org/) for full specification.

#### Version Update Workflow

1. **New Release** (increment MINOR, reset PATCH):
   - Bump `APP_VERSION_MINOR` in `gradle.properties` and reset `APP_VERSION_PATCH=00`
   - Task naming: Create new task with `t_<major>_<minor>_<patch>` pattern
   - Version code increments based on semantic ordering

2. **Patch Release** (increment PATCH):
   - Increment `APP_VERSION_PATCH` by 1 in `gradle.properties`
   - Use leading zero padding: 0 → 01, 99 → 99
   - Task naming: Continue using same minor version, increment patch

3. **Create GitHub Release**:
   ```bash
   gh release create void-scanner-1.1.04
   ```

#### Version Configuration

Version properties are centralized in `gradle.properties`:

```properties
APP_VERSION_MAJOR=1
APP_VERSION_MINOR=01
APP_VERSION_PATCH=00
```

### Dependencies
- **Gson**: JSON serialization (offline, local)
- **Android X AppCompat**: Backward compatibility layer
- **ConstraintLayout**: Responsive UI
- **OkHttp**: Placeholder for future APIs (not yet integrated)
- **Google Play Services**: Planned for Sheets integration (future)

## Developer Contact

For questions, issues, or collaboration opportunities:

### Communication Channels
- **GitHub Repository**: [https://github.com/BuboTheWise/VoidScanner](https://github.com/BuboTheWise/VoidScanner)
- **Issue Tracker**: [https://github.com/BuboTheWise/VoidScanner/issues](https://github.com/BuboTheWise/VoidScanner/issues)
- **Author**: @BuboTheWise (BuboTheWise)

### Reporting Issues
When reporting issues, please include:
- Device model and Android version
- Void Scanner version (from About screen)
- Reproduction steps
- Any error messages or crash logs
- Android logcat output (if applicable)

### Collaboration
For general discussions, pull requests, or collaboration inquiries:
- Open an issue on GitHub
- Fork the repository and submit a pull request
- Review the [F-Droid Compatibility](#fdroid-compatibility) section for additional guidelines

### F-Droid Availability
Void Scanner is published on F-Droid for privacy-focused users. See [F-Droid Compatibility](#fdroid-compatibility) for download links and metadata.

## Funding
### Requirements
- **Java JDK**: 11 or higher
- **Android SDK**: API 29+ with AndroidX libraries
- **Gradle**: 7.0 or higher
- **Platform**: Linux, macOS, or Windows with WSL2

### Build Steps
```bash
# Clone the repository
git clone https://github.com/BuboTheWise/VoidScanner.git
cd VoidScanner

# Build Debug APK
./gradlew :app:build

# Build Release APK
./gradlew :app:assembleRelease

# Install to device
./gradlew :app:installDebug
```

### Output
- **Debug APK**: `app/build/outputs/apk/debug/app-debug.apk` (~6MB)
- **Release APK**: `app/build/outputs/apk/release/app-release-unsigned.apk`

## Developer Contact

For questions, issues, or collaboration opportunities:
- **Repository**: https://github.com/BuboTheWise/VoidScanner
- **Author**: @BuboTheWise
- **Issue Tracker**: https://github.com/BuboTheWise/VoidScanner/issues
- **F-Droid**: https://gitlab.com/fdroid.fdroid/blob/master/metadata/com.bubo.voidscanner.yml

## F-Droid Compatibility

### F-Droid Repository Support

**Status**: Ready for F-Droid submission ✅

### Requirements Met
- ✓ Open-source software (Apache 2.0)
- ✓ No Google Play Services dependencies
- ✓ No proprietary APIs
- ✓ Clean build system (Gradle)
- ✓ No aggressive runtime tracking
- ✓ Permission rationale documented

### Build for F-Droid

```bash
# Clean and rebuild for F-Droid
./gradlew clean :app:assembleRelease

# Verify APK structure
# - No Google Play services code
# - No obfuscation (ProGuard disabled by default)
# - APK includes source R8/D8 optimization only

# Check for required metadata
lint ./app/

# Create signing key for F-Droid (if not already set up)
keytool -genkeypair -v -keystore voidscanner.keystore -alias voidscanner -keyalg RSA -keysize 4096 -validity 10000 -storepass <password> -keypass <password>
```

### F-Droid Metadata

**Name**: Void Scanner
**Summary**: Sensor data collection for Nethervoid Network void resonance analysis
**Description**: |
>
> The core scanner for the Nethervoid Network — explore the void between networked devices through comprehensive sensor analysis. Collects WiFi and Bluetooth signatures offline-first, generating deterministic seeds for game entity generation.
>
> Features:
> - WiFi and BLE signal detection
> - JSON export to Downloads folder
> - Real-time scan feedback
> - Privacy-aware data handling
>
> **Integration**: Void Resonator component of Nethervoid Network, intended for standalone testing and later integration into the full Nethervoid application.
>
> **Requirements**: Android 10+ (API 29+), no internet required
>
> **Package**: com.bubo.voidscanner
>
> **Source**: https://github.com/BuboTheWise/VoidScanner
>
> **Issue Tracker**: https://github.com/BuboTheWise/VoidScanner/issues

**Categories**: Tools, System, Security
**License**: Apache-2.0
**Scan Info**: Requested scan for: https://gitlab.com/fdroid.fdroid/blob/master/metadata/com.bubo.voidscanner.yml
**AutoUpdateMode: None**
**UpdateCheckMode: None**
**UpdateCheckData**: VERSION_NAME|versionName|app/build.gradle|versionName\s*[=&lt;]+\s*"([\d\.]+)"|VERSION_CODE|versionCode|app/build.gradle|versionCode\s*[=&lt;]+\s*(\d+)

---

## Funding

If you find Void Scanner valuable and want to support continued development, consider donating via Solana:

**SOL Address**: `6bV1GVVcM6dDazpgD6ZJkoQztn7vyKayFoDoRAhHssou`

> *"Every signal in the void is a potential anchor for future exploration. Your support helps us decode more of the network."*

**How to donate**:
```bash
# Verify the address
solana address verify 6bV1GVVcM6dDazpgD6ZJkoQztn7vyKayFoDoRAhHssou

# Or simply send SOL from your wallet
# Wallet: Phantom, Solflare, or any Solana-compatible wallet
# Amount: Your choice
# Destination: Enter the address above
```

```bash
# Scan and Send SOL
solana airdrop 1 <YOUR_SOLANA_ADDRESS>

# Manual transfer
# Wallet: Phantom, Solflare, etc.
# Address: [YOUR_SOLANA_ADDRESS]
```

> *"Every signal in the void is a potential anchor for future exploration. Your support helps us decode more of the network."*

---

## Development Notes

### Testing the Scanner

1. **Launch Void Scanner**
2. **Grant Permissions** (wifi, bluetooth)
3. **Tap "Start Scan"**
4. **Wait for WiFi/Bluetooth cooldown**
5. **Check Downloads folder**: `com.bubo.voidscanner/scan_*-*.json`

### Debug vs Release Builds

| Feature | Debug | Release |
|---------|-------|---------|
| Raw Data | ✅ Included | ❌ Excluded |
| Anonymization | ❌ Disabled | ✅ Enabled |
| Wi-Fi Names | ✅ Exact SSID | ❌ "Network_Hash" |
| BSSIDs | ✅ Exact MAC | ❌ Hashed |
| Coordinates | ✅ Exact Lat/Long | ❌ "Medium" confidence only |

### Future Enhancements
- Deterministic seed hashing for Nethervoid entity generation
- Signed scan records with Ed25519 cryptography
- Anchor world state integration
- Offline mesh synchronization (preliminary)

---

## Project Context

**Based On**: Veil Scanner / Sideband exploration
**Project**: Nethervoid Network (version 0.2)
**Author**: BuboTheWise
**License**: Apache 2.0

### Related Projects

- **Nethervoid Network**: Full game experience (fork of Sideband)
- **Reticulum**: Mesh networking protocol
- **Sideband**: Communication tool by Reticulum Network

### Integration Timeline

Void Scanner is being developed as an **isolated test platform** before merging into the Nethervoid Network application. The scanner codebase will:
1. Develop and test sensor algorithms independently
2. Establish deterministic generation logic
3. Build scan record cryptographic signatures
4. Integrate into Nethervoid game layer as Void Resonator

## License

Apache License 2.0
See [LICENSE](LICENSE) file for details.