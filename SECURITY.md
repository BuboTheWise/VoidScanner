# Security and Privacy — VoidScanner

## Data Flow and Privacy Guarantees

VoidScanner follows a **hash-only data egress** model:

```
Sensor hardware → Android API → in-app collectors (WifiScout, BluetoothScanner)
  → EntityGenerator (deterministic hash on device)
    → DiscoveredEntity objects stored locally
      → JSON export (user-initiated, local storage only)
        → [optionally] cryptographic seed hash shared with Nethervoid Network
```

### What stays on your device:
- Raw WiFi SSIDs, BSSIDs, signal strengths
- Bluetooth LE advertising data and MAC addresses
- Location coordinates and metadata
- System hardware identifiers (device model, OS version)
- All intermediate sensor aggregation results

### What leaves your device:
- **Nothing by default** — VoidScanner has no outbound network calls. It is not a background service. No telemetry, no analytics SDK, no crash reporter.
- Only if you manually export to JSON does data appear in local file storage (`/storage/<volume_id>/Download/VoidScanner/`).
- If you later participate in Nethervoid Network, only the **SHA-256 seed hash** of a scan travels to any mesh network — never the raw sensor readings that produced it. The seed is non-invertible: knowing the hash reveals nothing about which networks or devices were scanned.

## Permissions Required

| Permission | Purpose | Scope |
|------------|---------|-------|
| `ACCESS_FINE_LOCATION` / `ACCESS_COARSE_LOCATION` | Android requires this to scan WiFi and BLE (location privacy regulation) | Foreground scans only; no background tracking |
| `BLUETOOTH_SCAN` | Discover nearby BLE devices | Only during active scan; scanner stops on scan completion |
| `CHANGE_WIFI_STATE` | Trigger WiFi network discovery | Scan duration only (~5-10 seconds) |
| `INTERNET` | Required by Android manifest spec for scanning (not used for data transmission) | Not actively exercised by the app |
| `WRITE_EXTERNAL_STORAGE` (pre-API 30) | JSON export to Downloads folder | Write-only; read-back is user-initiated via file manager |

The app does **not** request camera, microphone, contacts, phone state, SMS, clipboard, or foreground service permissions. Any permission beyond the table above should be treated as a bug and reported through [GitHub Issues](https://github.com/BuboTheWise/VoidScanner/issues).

## F-Droid Build Transparency

VoidScanner ships via a manual F-Droid repository hosted on GitHub Pages. The build configuration, Gradle files, and source are all publicly inspectable in the repository. You can verify the APK by:

1. Checking out the tagged commit matching your release version
2. Building locally with `./gradlew assembleRelease`
3. Comparing the SHA-256 of the resulting APK against the release checksums on GitHub

## Nethervoid Network Security Posture (future)

When the Nethervoid Network game module is implemented, it will extend the security model:

| Layer | Control | Status |
|-------|---------|--------|
| Identity | Ed25519 per-device keys for signing all game objects | Specified in Requirements v0.2; awaiting implementation |
| Storage | SQLCipher encrypted database with identity-derived key + biometric/PIN gate | Specified; not yet built |
| Mesh propagation | Gossip protocol with TTL decay, rate limiting, entity caps | Specified; not yet built |
| Provenance | Merkle-rooted essence history for tamper evidence | Specified; not yet built |

These controls are documented in the [Nethervoid Network Requirements](https://github.com/BuboTheWise/Nethervoid-Network/blob/main/Requirements.md) and are aspirational until implemented. At present, VoidScanner itself does not exercise any of these controls — the security guarantees listed above apply only to the current release.

## Responsible Disclosure

If you discover a vulnerability in VoidScanner:

1. Open a **private issue** on [GitHub Issues](https://github.com/BuboTheWise/VoidScanner/issues) with the label `security` (request this label from maintainers if needed)
2. Describe the impact and reproduction steps
3. Allow 7 days for acknowledgment, 30 days for a patch before public disclosure

Do not post exploit code or sensitive details publicly before a fix is available.

## Threat Model Summary

| Threat | Mitigation | Risk Level |
|--------|-----------|------------|
| Sensor data exposure via app crash dumps | No network endpoint; crash data stays on device | Low |
| Man-in-the-middle during F-Droid download | Hosted on GitHub Pages (HTTPS); verify APK hashes | Low |
| Permission escalation via Android vulnerability | Use graphically signed Graphene OS build; keep OS updated | Medium |
| Reverse engineering of APK | Code is open source by design; trust through transparency, not opacity | N/A |

**Last reviewed:** 2026-07-02 (post-documentation audit)
