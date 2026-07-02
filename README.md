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

- Android development environment (Android Studio recommended)
- JDK 17 or higher
- Target device: Android API 29+ (Graphene OS verified, Pixel Fold tested)

### Installation from Release

1. Download the latest `.apk` from the [Releases page](https://github.com/BuboTheWise/VoidScanner/releases)
2. Open the file with your Android file manager
3. If your device blocks unknown sources, enable "Install from unknown sources" in Settings

### F-Droid Repository

Add the manual F-Droid repository:
```
https://BuboTheWise.github.io/VoidScanner/fdroid/repo/index.xml
```

For detailed setup instructions, see [FDROID_SETUP.md](FDROID_SETUP.md).

### Build from Source

```bash
git clone https://github.com/BuboTheWise/VoidScanner.git
cd VoidScanner
./gradlew assembleRelease
```

For first-time development setup, refer to [INSTALL.md](INSTALL.md).

## Current Version

**v1.1.08** (2026-05-28) — latest release with enhanced UI feedback, permission error handling, and export fixes for Graphene OS. See [release_notes_v1.1.08.md](release_notes_v1.1.08.md) for full details.

Version history: v1.1.02 → v1.1.04 → v1.1.05 → v1.1.06 → v1.1.07 (fixes) → v1.1.08 | Full versioning policy and tag conventions: [VERSIONING.md](VERSIONING.md)

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
| 1 | Sensor Expansion (IMU, environmental) | 1.1.x | Planned — see [TASKS.md](TASKS.md) |
| 2 | Feature Extraction (seed hashing, entity UI) | 1.2.x | Pending Phase 1 |
| 3 | QA & v0.3 Release | 2.0.x | Blocked on Phases 1-2 |

See [TASKS.md](TASKS.md) for the full task breakdown with dependencies and success criteria.

## License

VoidScanner is released under the MIT License. See [LICENSE](LICENSE) (if present) or refer to the GitHub repository settings.

## Support & Issues

Report bugs and request features through the [GitHub issue tracker](https://github.com/BuboTheWise/VoidScanner/issues). For questions about Nethervoid Network integration points, open issues in the [Nethervoid-Network repository](https://github.com/BuboTheWise/Nethervoid-Network/issues) instead.

## Related Projects

- **[Nethervoid Network](https://github.com/BuboTheWise/Nethervoid-Network)** — Parent project: mesh-native game layer consuming VoidScanner seeds
- **[Nethervoid Requirements v0.2](https://github.com/BuboTheWise/Nethervoid-Network/blob/main/Requirements.md)** — Technical architecture and integration spec
- **[Void Resonator Specification v0.3](https://github.com/BuboTheWise/Nethervoid-Network/blob/main/Void%20Resonator%20(scanner).md)** — Scanner role within the broader system
