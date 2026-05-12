# F-Droid Repository Setup Guide — void scanner

## Why Manual Addition Instead of Official Submission?

The void scanner app is currently in development with frequent updates. Direct GitHub hosting is faster and allows users to get the latest version immediately without waiting for F-Droid review processes.

## GitHub-Hosted F-Droid Repository Setup

### Step 1: Create a F-Droid repository on GitHub

1. Navigate to your Void Scanner GitHub repository settings
2. Go to **Settings** → **GitHub Actions** → **Workflows** → **New Workflow**
3. Name it `.github/workflows/fdroid.yml`
4. Create an action workflow for building and publishing APK files

### Step 2: Configure F-Droid Server

1. Create a new repository on your GitHub: `https://github.com/username/void-scanner-fdroid`
2. Add the `fdroid.yml` workflow that builds the APK
3. Push the repository code

### Step 3: Add APK Builds to F-Droid

The F-Droid client will automatically pick up your GitHub repository IF you configure:

1. **Repository URL**: `https://github.com/username/void-scanner-fdroid`
2. **Repository Name**: `void-scanner`
3. **Fingerprint**: (optional, recommended for security)
4. **Build Instructions** (in repo's `f-droid.yml`):
   - Build Android APK using your CI pipeline
   - Upload APK files to the repository's Releases or a dedicated APK folder

### Step 4: User Manual Setup

Users can add your F-Droid-compatible repository manually:

**If using F-Droid Desktop:**
1. Open F-Droid Desktop → Repositories → Add Repository
2. Enter: `https://github.com/username/void-scanner`
3. Click "Add"

**If using F-Droid CLI:**
```bash
fdroid repo add https://github.com/username/void-scanner
fdroid update
fdroid install void
```

**Manually via config:**
*Add to `~/.config/fdroid/repo.cfg`*:
```ini
repo=https://github.com/username/void-scanner
fingerprint=YOUR_FINGERPRINT_HERE
```

## Build Instructions

The GitHub Actions workflow should:
1. Check out the Void Scanner code
2. Android build with Gradle
3. Sign the APK with your keystore
4. Upload signed APK to Releases as `void-v1.1.05.apk`

## Security Considerations

- Use F-Droid's signing mechanisms to protect against repository tampering
- Verify the F-Droid fingerprint before installing
- Keep the GitHub repository private if desired (f-droid-client supports private repos)

## Alternative: fdroidserver

If you want to use fdroidserver directly:

1. Install fdroidserver: `pip install fdroidserver`
2. Initialize: `fdroid init /path/to/repo`
3. Edit `builds.gradle` in the VoidScanner project for F-Droid builds
4. Create a `fdroid.yml` for CI/CD builds
5. Add repository metadata in `repo/meta/`

## Support

For questions or issues, check:
- F-Droid documentation: https://f-droid.org/docs/Hosting_a_repository
- F-Droid Client: https://f-droid.org/en/docs/Client_User_Interface
