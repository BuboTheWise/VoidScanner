# Glossary — VoidScanner + Nethervoid Network

Shared definitions for recurring terms across both projects. Use this file to resolve ambiguity without searching through every document.

| Term | Definition | Used In |
|------|-----------|---------|
| **Void Resonator** | The scanner subsystem — collects environmental sensor data and produces deterministic seeds for entity generation | VoidScanner, Nethervoid Network |
| **Entity** | A game creature or object generated from a scan seed; carries a name, rarity tier, flavor text, attributes, and hash signature | Both projects |
| **Essence** | Signed data structure encoding entity state, growth vector, and provenance history (Merkle-rooted interaction chain) | Primarily Nethervoid Network |
| **Seed** | SHA-256 hash of collected sensor fingerprint; deterministic starting point for entity generation. Non-invertible: the seed reveals nothing about which networks or devices were scanned | Both projects |
| **Anchor** | A location marker in the Nethervoid world state, propagated via mesh gossip with TTL and decay | Primarily Nethervoid Network |
| **Codex** | The Void Codex provenance system — tracks lineage and ownership chains for entities | Nethervoid Network |
| **Piercer** | Mesh interface component for real-time group convergence using custom Reticulum protocols | Nethervoid Network |
| **OUI** | Organizationally Unique Identifier — the vendor portion of a MAC address prefix, used for contextual entity attributes (Philips Hue, Arlo, Nest, etc.) | VoidScanner |
| **ScanRecord** | Protobuf model representing a completed sensor scan with seed hash and metadata | Both projects |
| **Sensor Features** | Derived metrics extracted from raw sensor data: humanDensity, iotPresence, signalChaos, techLevel, proximity | VoidScanner (Phase 2) |

## Relationship to Nethervoid Network Glossary

This glossary mirrors the definitions in the [Nethervoid Network README](https://github.com/BuboTheWise/Nethervoid-Network/blob/main/README.md#domain-glossary). If a definition changes, update both files. This file exists so VoidScanner can stand alone without requiring the reader to jump between repositories for basic terminology.
