## HIGH: Feature Extraction (Phase 2)

### Task ID: task-build-1.2.1-feature-extraction

**Description:**
Create SensorFeatures.java class to extract metrics from raw sensor data: humanDensity, iotPresence, signalChaos, techLevel, proximity.

**Executor:**
cthugha

**Dependencies:**
task-build-1.1.1-location, task-build-1.1.2-imu-sensors, task-build-1.1.3-cellular-bt, task-build-1.1.4-environmental

**Status:**
pending

**Priority:**
2

**Blockers:**
None

**Success Criteria:**
- [ ] SensorFeatures.java with methods for each metric
- [ ] Metrics extracted from collectedData Map<String, Object>
- [ ] Floating point values rounded to 3 decimal places
- [ ] All sensor data accessible from features object

**Notes:**
See void-scanner-development SKILL.md Phase 2 Build 1.2.1 for metric formulas.

---

### Task ID: task-build-1.2.2-deterministic-seed

**Description:**
Implement SeedGenerator.java using SHA-256 hashing of SensorFeatures input plus timestamp bucket. Ensure same scan data = identical seed.

**Executor:**
cthugha

**Dependencies:**
task-build-1.2.1-feature-extraction

**Status:**
pending

**Priority:**
2

**Blockers:**
None

**Success Criteria:**
- [ ] SeedGenerator.java with SHA-256 implementation
- [ ] Input includes: timestamp, WiFi SSIDs, BT MACs, IMU readings
- [ ] Output: 64-character hex string
- [ ] Deterministic: Same scan data produces same seed

**Notes:**
Use java.security.MessageDigest.

---

### Task ID: task-build-1.2.3-entity-hash

**Description:**
Implement EntityHashGenerator.java per Generation Rules v0.3. Create meaningful entities from sensor signatures.

**Executor:**
cthugha

**Dependencies:**
task-build-1.2.2-deterministic-seed

**Status:**
pending

**Priority:**
2

**Blockers:**
None

**Success Criteria:**
- [ ] EntityHashGenerator.java with entity creation logic
- [ ] Entity JSON with: name, rarity, flavor text, hash
- [ ] Hash = SHA-256(base_scan_seed + entity_baseType + OUIs + location + time + features)
- [ ] Output exportable with scan results

**Notes:**
See void-scanner-development SKILL.md Section 5 for Generation Rules.

---

### Task ID: task-build-1.2.4-post-scan-summary

**Description:**
Add screen/UI to show discovered entities with metadata (name, rarity, flavor text) after scan completes, including entity hashes.

**Executor:**
cthugha

**Dependencies:**
task-build-1.2.3-entity-hash

**Status:**
pending

**Priority:**
2

**Blockers:**
None

**Success Criteria:**
- [ ] New UI component showing discovered entities
- [ ] Display: entity name, rarity, flavor text, hash
- [ ] Summary statistics: entities found, sensor breakdown
- [ ] Buttons: "Attune Essence", "Ignore", "Share Scan"

**Notes:**
Extend activity_main.xml layout for new UI elements.

---

## MEDIUM: QA & Release (Phase 3)

### Task ID: task-build-2.0.2-code-review

**Description:**
Code review for performance bottlenecks. Optimize sensor scanning loop. Add null safety checks.

**Executor:**
cthugha

**Dependencies:**
All Phase 1-2 tasks

**Status:**
pending

**Priority:**
3

**Blockers:**
None

**Success Criteria:**
- [ ] Entity generation completes in < 100ms per scan
- [ ] Sensor scanning loop optimized
- [ ] Null safety checks added (optional sensors)
- [ ] Memory leak detection (no listener accumulation)

**Notes:**
Use Android Profiler or log memory usage.

---

### Task ID: task-build-2.0.3-documentation

**Description:**
Update README.md with v0.3 features, API usage examples, migration guide from v1.0.2.

**Executor:**
cthugha

**Dependencies:**
task-build-2.0.2-code-review

**Status:**
pending

**Priority:**
3

**Blockers:**
None

**Success Criteria:**
- [ ] README.md updated with all new sensor features
- [ ] API usage examples for each sensor type
- [ ] Migration guide from v1.x to v0.3
- [ ] Build/Installation instructions for v0.3

**Notes:**
Keep documentation inline with code; use clear examples.

---

### Task ID: task-build-2.0.4-quality-assurance

**Description:**
Comprehensive sensor testing on real devices, memory leak detection, edge case handling.

**Executor:**
cthugha

**Dependencies:**
task-build-2.0.2-code-review, task-build-2.0.3-documentation

**Status:**
pending

**Priority:**
3

**Blockers:**
None

**Success Criteria:**
- [ ] All sensors tested on real device
- [ ] Edge cases: permissions denied, sensors unavailable, weak signal
- [ ] Memory leak: repeat scans with no memory growth
- [ ] No crashes on UI interruption (app minimized/backgrounded)

**Notes:**
Test on Pixel Fold + Graphene OS plus another Android device (AOSP).

---

### Task ID: task-build-2.0.5-v03-release

**Description:**
Create final v0.3.0 release with all features, proper release notes, signed APK, push to GitHub.

**Executor:**
cthugha

**Dependencies:**
task-build-2.0.3-documentation, task-build-2.0.4-quality-assurance

**Status:**
pending

**Priority:**
3

**Blockers:**
None

**Success Criteria:**
- [ ] Version bumped to 0.3.0 in gradle.properties
- [ ] strings.xml updated to "0.3.0"
- [ ] JsonExporter.java updated
- [ ] Release notes generated: release_notes_v0.3.0.md
- [ ] Released on GitHub with signed APK
- [ ] Tag: v0.3.0

**Notes:**
This is the final release. Follow versioning rule: X.Y.ZZ format → 0.3.00 (but since semantic conventions now, use 0.3.0). Update based on user preference.

**Last Updated:** 2026-05-09