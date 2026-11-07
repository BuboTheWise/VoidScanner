# VoidScanner — Privacy-Focused Sensor Scouting Application

VoidScanner is an Android application that collects environment sensor data and deterministically generates game entities from those readings. It serves as the data collection foundation for [Nethervoid Network](https://github.com/BuboTheWise/Nethervoid-Network) — a mesh-native parallel-dimension game layer built on top of Reticulum's offline messaging protocol. All processing happens locally on device. No raw sensor data leaves your phone.

## Project Origins

VoidScanner was originally released as **VeilScanner** (v1.0.0). The project was renamed to VoidScanner during the transition from a standalone scouting tool into the sensor-collecting half of the Nethervoid Network ecosystem. Both names refer to the same codebase — VeilScanner tags on GitHub point to the same commits.

## About

VoidScanner performs exhaustive, user-initiated scans using available hardware sensors. The collected environmental fingerprint is hashed deterministically into a seed value that feeds an entity generator producing creature-like game objects with names, rarity tiers, flavor text, and reproducible hash signatures. Only cryptographic hashes travel to any remote system — never raw signal data.

## Architecture Overview

```
┌─────────────────────────────────────────────┐
│              Sensor Collectors               │
│  WiFi (WifiScout) │ BLE (BluetoothScanner)   │
│  Location │ System metadata                  │
└──────────────────┬──────────────────────────┘
                   │  raw sensor data
                   ▼
┌─────────────────────────────────────────────┐
│           Entity Generator                   │
│  OUI-based vendor mapping → seed hash        │
│  deterministic entity synthesis              │
│  (DiscoveredEntity with rarity + attributes) │
└──────────────────┬──────────────────────────┘
                   │  entity hashes only
                   ▼
┌─────────────────────────────────────────────┐
│           Export / Display                   │
│  JSON export │ Post-scan summary UI          │
│  (F-Droid release │ debug export)            │
└─────────────────────────────────────────────┘
```

For the full Nethervoid Network architecture — including how scan seeds feed into the Entity & Essence Engine, Anchors, and mesh propagation — see the [Nethervoid Network Requirements](https://github.com/BuboTheWise/Nethervoid-Network/blob/main/Requirements.md).

## Features

- **WiFi scanning** — discovers accessible networks via `WifiScout`
- **Bluetooth LE scanning** — detects nearby BLE devices via `BluetoothScanner`
- **Deterministic entity generation** — same sensor fingerprint always produces the same set of entities (`EntityGenerator`)
- **OUI-based vendor profiling** — maps MAC address prefixes to device vendors for contextual entity attributes (Philips Hue, Arlo, Nest, Ecobee, TP-Link, and more)
- **Entity metadata** — each discovered entity carries a name, rarity tier, flavor text, hash signature, power level, and traceable OUI influence list (`DiscoveredEntity`)
- **JSON export** — structured scan results exported to device storage via `JsonExporter`
- **Privacy by design** — all data processing is local; only hashed seeds leave the scanner for game-world consumption
- **Progress indicators** — real-time ProgressBar during active scans with Toast notifications for state changes (v1.1.08)
- **F-Droid deployment** — signed release APKs available through a manual F-Droid repository

## Getting Started

### Prerequisites

| **JDK 17** — install `jdk17-openjdk`. Set `JAVA_HOME` when the system defaults to JDK 21+ (which Gradle does NOT support):

```bash
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk
```

This is currently **required** on this machine because JDK 27 causes the Android SDK's `core-for-system-modules.jar` to fail during Gradle transformation.
- **Android SDK Command-Line Tools** — install `android-tools` (Arch), or [download](https://developer.android.com/studio#command-tools) for macOS/Windows
- **`local.properties`** in the repo root — created on first build with your SDK path:
  ```bash
  echo 'sdk.dir=/absolute/path/to/Android/Sdk' > local.properties
  # Example (Arch Linux default):
  echo 'sdk.dir=/usr/lib/jvm/java-17-openjdk' >> local.properties   # wrong — this is JAVA_HOME
  # Correct (where android-sdk/cmdline-tools lives):
  echo 'sdk.dir=HOME/Android/Sdk' > local.properties                  # WRONG: Gradle does NOT expand shell variables
  echo 'sdk.dir=/path/to/Android/Sdk' >> local.properties         # use absolute path
  ```
- **Target device**: Android API 29+ (Graphene OS verified, Pixel Fold tested)

### Install APK Directly (Fastest for Development)

```bash
./gradlew assembleDebug      # builds to app/build/outputs/apk/debug/app-debug.apk
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

Or clone and build locally: `git clone https://github.com/BuboTheWise/VoidScanner.git && cd VoidScanner`. See [docs/DEVSETUP.md](docs/DEVSETUP.md) for full first-time setup on Linux/macOS/Windows.

### F-Droid Manual Repository (Recommended for Installation)

VoidScanner is available as a pre-built signed APK from a **manual** F-Droid repository served via GitHub Pages. This is the standard, supported method — no manual APK download needed.

1. Make sure [F-Droid](https://f-droid.org) is installed on your Android device
2. Open F-Droid → **Settings → Repositories** (or three-dot menu → Repositories / Manage Repos)
3. Tap the **+** button to add a new repository
4. Paste this URL:
   ```
   https://BuboTheWise.github.io/VoidScanner/fdroid/repo/
   ```
5. Confirm — once synchronized, VoidScanner will appear in your F-Droid client as **Void Scanner**. Updates from GitHub Actions are deployed automatically (see [CI docs](docs/DEVSETUP.md)).

> **Note:** The repo contains pre-built signed APKs with SHA-1 fingerprint `94:11:82:0D:AE:10:C3:D4:CF:13:24:A1:C3:88:08:04:6A:E3:95:17`. The metadata in index.xml and repo is auto-generated by `fdidx update-index`.

### Validate the Repository

A Python validation harness is provided in `fdroid/validate_fdroid_repo.py`. It checks metadata YAML syntax, index XML structure, APK hash integrity, signing configuration, version consistency, and icon assets against F-Droid spec requirements:

```bash
python3 fdroid/validate_fdroid_repo.py fdroid/repo --config fdroid/config.yml
```

Passes all checks with zero blocking errors. Use `--help` for options including `--strict` (treat warnings as errors) and `--json`.

### Build from Source

#### Debug build (for local testing via `adb`)

```bash
git clone https://github.com/BuboTheWise/VoidScanner.git
cd VoidScanner
./gradlew assembleDebug        # outputs app-debug.apk
adb install -r app/build/outputs/apk/debug/app-debug.apk  # push to device
```

#### Release build (for F-Droid / distribution)

```bash
./gradlew assembleRelease
# APK at app/build/outputs/apk/release/app-release-*.apk
# Signed with app/voidscanner.keystore — see [DEVSETUP.md](docs/DEVSETUP.md) for key management.
```

### Quick push of local changes to GitHub (for repo maintainers)

```bash
git add -A && git commit -m "fix: describe change" && git push origin main
```

GitHub Pages will update within minutes via the [CI workflow](https://github.com/BuboTheWise/VoidScanner/actions/workflows/fdroid.yml). If `git push` fails with auth errors, run `gh auth login` to connect your GitHub credentials.

## Current Version

**v1.2.00** — latest release, installed on device via ADB ✓. Built with JDK 17 (set `JAVA_HOME=/usr/lib/jvm/java-17-openjdk` when system defaults to JDK 21+).

Versioning scheme: major/minor/patch (e.g. `1.2.00`) → versionCode `(major×10000)+(minor×100)+patch`. Full policy: [VERSIONING.md](VERSIONING.md).

## Project Structure

```
VoidScanner/
├── app/                          # Android application module
│   └── src/main/java/com/bubo/voidscanner/
│       ├── MainActivity.java           # Main UI controller
│       ├── WifiScout.java              # WiFi sensor collector
│       ├── BluetoothScanner.java        # BLE sensor collector
│       ├── EntityGenerator.java         # Deterministic entity synthesis
│       ├── DiscoveredEntity.java        # Entity data model (hash, rarity, attributes)
│       ├── JsonExporter.java           # Structured scan result export
│       └── entities/                   # Entity type definitions
│           ├── Entity.java
│           └── Rarity.java
├── fdroid/                       # F-Droid repository metadata + signing
├── release_notes_*.md            # Per-release changelogs
├── TASKS.md                      # Development task backlog (Phases 1-3)
├── INSTALL.md                    # Build and deployment instructions
├── FDROID_SETUP.md              # F-Droid configuration reference
├── VERSIONING.md                # Version policy, tag conventions, release history
├── SECURITY.md                  # Data flow, permissions, threat model, disclosure
└── _archive/                    # Archived session artifacts (non-canonical)
```

## Roadmap

| Phase | Title | Builds | Status |
|-------|-------|--------|--------|
| 1 | Sensor Expansion (IMU, environmental) | 1.1.x | ✓ Complete |
| 2 | Feature Extraction (seed hashing, entity UI) | 1.2.x | ✓ Complete; deployed v1.2.00 |
| 3 | QA & v0.3 Release | 2.0.x | Planning |
See [TASKS.md](TASKS.md) for the full task breakdown with dependencies and success criteria.

## License

VoidScanner is released under the **Apache License 2.0**. See [LICENSE](LICENSE) file.

## Support & Issues

Report bugs and request features through the [GitHub issue tracker](https://github.com/BuboTheWise/VoidScanner/issues). For questions about Nethervoid Network integration points, open issues in the [Nethervoid-Network repository](https://github.com/BuboTheWise/Nethervoid-Network/issues) instead.

## Related Projects

- **[Nethervoid Network](https://github.com/BuboTheWise/Nethervoid-Network)** — Parent project: mesh-native game layer consuming VoidScanner seeds
- **[Nethervoid Requirements v0.2](https://github.com/BuboTheWise/Nethervoid-Network/blob/main/Requirements.md)** — Technical architecture and integration spec
- **[Void Resonator Specification v0.3](https://github.com/BuboTheWise/Nethervoid-Network/blob/main/Void%20Resonator%20(scanner).md)** — Scanner role within the broader system
