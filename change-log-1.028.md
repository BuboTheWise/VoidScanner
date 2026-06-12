# Void Scanner v1.028

## Versioning Convention Update

This update implements the X.Y.ZZ semantic versioning convention correctly:

- Format: X.Y.ZZ (major.minor.zero-padded-patch)
- Example: 1.0.28
- PATCH component zero-padded to two digits (00-99)

### Version Code Calculation
VERSION_CODE = MAJOR * 10000 + MINOR * 100 + PATCH
1 * 10000 + 0 * 100 + 28 = 1028

## F-Droid Configuration Files Updated

1. **`fdroid/data/metadata/com.bubo.voidscanner.yml`**
   - Updated `Version` from "1.028" to "1.0.28" 
   - Updated `VersionCode` from "10028" to "1028"
   - Format: X = 1, Y = 0, Z = 28 (properly zero-padded)

2. **`fdroid/repo/metadata/com.bubo.voidscanner.yml`**
   - Updated `Version` from "1.028" to "1.0.28"
   - Updated `VersionCode` from "1028" to "1028"
   - Format: X = 1, Y = 0, Z = 28 (properly zero-padded)

## Changes
- F-Droid metadata files updated with correct X.Y.ZZ versioning
- Both files now consistently follow the convention 
- VERSION_CODE calculation properly implemented for 1.0.28

## Notes
This version maintains backward compatibility while correctly implementing the workflow standard's X.Y.ZZ versioning scheme.