import requests

print("=== CHECKING GITHUB PAGES RESPONSES ===\n")

# Check index.xml
try:
    response = requests.get('https://BuboTheWise.github.io/VoidScanner/fdroid/repo/index.xml', timeout=10)
    print(f"index.xml status: {response.status_code}")
    if response.status_code == 200:
        print("✓ index.xml accessible")
        # Check for app-release.apk reference
        if 'app-release.apk' in response.text:
            print("✓ APK reference found in index.xml")
        else:
            print("✗ APK reference NOT found in index.xml")
        # Print first 500 chars
        print("\nFirst 500 chars of index.xml:")
        print(response.text[:500])
    else:
        print(f"✗ Error: {response.text[:200]}")
except Exception as e:
    print(f"✗ Error fetching index.xml: {e}")

print("\n" + "="*80)

# Check app-release.apk directly
try:
    apk_response = requests.get('https://BuboTheWise.github.io/VoidScanner/fdroid/repo/app-release.apk', timeout=10)
    print(f"\napp-release.apk status: {apk_response.status_code}")
    if apk_response.status_code == 200:
        print(f"✓ APK accessible ({apk_response.headers.get('Content-Length', 'unknown')} bytes)")
        print(f"MIME type: {apk_response.headers.get('Content-Type', 'unknown')}")
    else:
        print(f"✗ Error: {apk_response.text[:200]}")
except Exception as e:
    print(f"✗ Error fetching APK: {e}")

print("\n" + "="*80)

# Check main README for link
try:
    readme_response = requests.get('https://BuboTheWise.github.io/VoidScanner/README.md', timeout=10)
    print(f"\nREADME.md status: {readme_response.status_code}")
    if readme_response.status_code == 200:
        print("✓ README.md accessible")
    else:
        print(f"✗ Error: {readme_response.text[:200]}")
except Exception as e:
    print(f"✗ Error fetching README: {e}")

# Now check the local repository structure
print("\n" + "="*80)
print("=== LOCAL REPOSITORY STRUCTURE ===\n")

import subprocess
import os

repo_path = '/home/bubo/.hermes/workspace/Code/VoidScanner'

print("Checking /fdroid/repo structure:")
result = subprocess.run(
    ['ls', '-la', os.path.join(repo_path, 'fdroid/repo')],
    capture_output=True, text=True,
    cwd=repo_path
)
print(result.stdout)

print("\nChecking /fdroid index and config:")
for f in ['index.xml', 'metadata.txt', 'config.yml']:
    path = os.path.join(repo_path, 'fdroid', f)
    if os.path.exists(path):
        print(f"✓ {f} exists")
        # Check for app-release.apk reference in this file
        if os.path.exists(path):
            with open(path, 'r') as file:
                content = file.read()
                if 'app-release.apk' in content:
                    print(f"  ✓ APK reference found in {f}")
# Check if APK file exists in fdroid/repo
apk_path = os.path.join(repo_path, 'fdroid/repo', 'app-release.apk')
if os.path.exists(apk_path):
    size = os.path.getsize(apk_path)
    print(f"\n✓ APK file exists in fdroid/repo: {apk_path} ({size:,} bytes)")
else:
    print(f"\n✗ APK file NOT found in fdroid/repo: {apk_path}")

# Check F-Droid metadata
metadata_path = os.path.join(repo_path, 'fdroid/metadata', 'voidscanner-metadata.yml')
if os.path.exists(metadata_path):
    print(f"\n✓ Metadata file exists: {metadata_path}")
    with open(metadata_path, 'r') as file:
        content = file.read()
        print("\nMetadata content:")
        print(content[:500])
else:
    print(f"\n✗ Metadata file NOT found: {metadata_path}")

print("\n" + "="*80)