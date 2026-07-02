# Versioning Policy

## Current Scheme

VoidScanner uses a **dual-track version scheme** to accommodate different release contexts:

| Track | Format | Example | Used For |
|-------|--------|---------|----------|
| App releases | `X.Y.ZZ` (zero-padded patch) | `1.1.08`, `1.028` | Gradle `versionName`, GitHub Releases, F-Droid metadata |
| Component specs | `v0.X` | `v0.2`, `v0.3` | Architectural documents, protocol specifications for Nethervoid Network |

The two tracks serve different purposes and should not be conflated:

- **App releases** track the Android APK lifecycle — every increment reflects a shippable binary in the F-Droid repository or GitHub Releases page.
- **Component specs** track design documents within the broader Nethervoid Network ecosystem (Requirements.md, Void Resonator spec, Task definitions). These move on their own schedule since architecture evolves independently from app binaries.

## Version Number Encoding

Gradle `VERSION_CODE` is derived from the version string:

```
VERSION_CODE = MAJOR * 10000 + MINOR * 100 + PATCH
Version 1.1.08 → 1*10000 + 1*100 + 8 = 10108
```

F-Droid metadata files (`fdroid/data/metadata/com.bubo.voidscanner.yml` and `fdroid/repo/metadata/com.bubo.voidscanner.yml`) use the canonical version format: `versionName` holds the human-readable string while `versionCode` is the integer.

## Release History

| Tag | Date | Type | Notes |
|-----|------|------|-------|
| v1.0.0 | 2026-04-xx | Pre-release | VeilScanner, pre-rename |
| v1.0.2 → v1.028 | 2026-05 | Transitional | X.Y.ZZ convention introduced; `v1.028` tag is the last release using the transitional format before stabilization |
| v1.1.02 → v1.1.04 → v1.1.05 → v1.1.06 → v1.1.07 → v1.1.08 | 2026-05 onwards | Stable | Current release series with zero-padded patch component |

The gap between `v1.028` and the `v1.1.0x` series reflects a minor bump (1.0 → 1.1) alongside adoption of the zero-padded patch convention. Both lines point to real releases; they were just tagged at different times during the transitional period. The latest production release is **v1.1.08** (2026-05-28).

## Branch and Tag Convention

- `main` — stable release branch, always shippable
- Feature branches: `feature/<description>` — development work only; never delete until merged and verified pushed
- Tags: `vX.Y.ZZ` — created after merging a feature branch, pushed alongside the merge commit

## Project Rename

The project was originally called **VeilScanner**. It was renamed to **VoidScanner** during the transition from a standalone scouting tool into the sensor-collecting half of Nethervoid Network. The GitHub pre-release tag `VeilScanner v1.0.0` and all subsequent tags use the VoidScanner naming. Old VeilScanner references resolve to the same commits.
