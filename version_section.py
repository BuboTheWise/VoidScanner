### Versioning

Void Scanner uses **semantic versioning (SemVer)** with leading zero padding for patch revisions (00-99):

| **Current Version**: ${current_version} |
| **Semantic Version Format**: `X.Y.Z` |
  - **X (MAJOR)**: Breaking changes, major features
  - **Y (MINOR)**: New features, compatible additions (padded to 1 digit: 1-9)
  - **Z (PATCH)**: Bug fixes, minor improvements (padded to 2 digits: 00-99)
- **Version Code**: Auto-incremented based on semantic ordering
- **Target API**: Android 10+ (API 29+)
- **Package Name**: `com.bubo.voidscanner`

#### Version Examples

| Version | Meaning | Change Type |
|---------|---------|-------------|
| ${current_version} → 1.2.00 | New feature release, MINOR increment | Bump 1 → 2, reset PATCH to 00 |
| 1.1.05 → 1.1.01 | Bug fix only, PATCH increment | Increment patch 05 → 06 |
| 1.2.00 → 2.0.00 | Major breaking change, new features | Bump MAJOR to 2, reset MINOR and PATCH |

See [Semantic Versioning](https://semver.org/) for full specification.

#### Version Update Workflow

1. **New Release** (increment MINOR, reset PATCH):
   - Bump `APP_VERSION_MINOR` in `gradle.properties` and reset `APP_VERSION_PATCH=00`
   - Bump `APP_VERSION_MAJOR` only for breaking changes
   - Version string: `APP_VERSION_NAME=X.Y.Z`
   - Task naming: Create new task with `t_<major>_<minor>_<patch>` pattern

2. **Patch Release** (increment PATCH):
   - Increment `APP_VERSION_PATCH` by 1 in `gradle.properties`
   - Use leading zero padding: 0 → 01, 06 → 07, ..., 99 → 00
   - Task naming: Continue using same version, increment PATCH

3. **Create Versioned APK**:
   ```bash
   VERSION=1.1.07
   APK_NAME="void-scanner-${VERSION}.apk"
   mkdir -p fdroid/repo
   cp app/build/outputs/apk/release/app-release.apk fdroid/repo/${APK_NAME}
   ```

4. **F-Droid Workflow**:
   ```bash
   git add fdroid/repo/
   git commit -m "F-Droid: Add void-scanner-${VERSION}.apk to repo"
   git push origin main
   ```

#### Version Configuration

Version properties are centralized in `gradle.properties`:

```properties
APP_VERSION_MAJOR=1
APP_VERSION_MINOR=1
APP_VERSION_PATCH=06
APP_VERSION_NAME=1.1.06
```