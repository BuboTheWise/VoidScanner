#!/bin/bash
# Version Management Script
# Semantic versioning helper: ensures X.Y.Z format with leading zero padding and validates version properties

set -e

cd "$(dirname "$0")"

# Validate version format in gradle.properties
validate_version() {
    echo "Validating version formatting..."
    local patch_pattern='^APP_VERSION_PATCH=[0-9][0-9]$'
    local minor_pattern='^APP_VERSION_MINOR=[0-9][0-9]$'

    if ! grep -E "^APP_VERSION_PATCH=" gradle.properties | grep -qE '[0-9][0-9]$'; then
        echo "ERROR: APP_VERSION_PATCH must be two digits (e.g., 00, 01, 99)"
        exit 1
    fi

    if ! grep -E "^APP_VERSION_MINOR=" gradle.properties | grep -qE '[0-9][0-9]$'; then
        echo "ERROR: APP_VERSION_MINOR must be two digits (e.g., 00, 01, 99)"
        exit 1
    fi

    echo "✓ Version format validation passed"
}

# Create version tag for release
create_tag() {
    local version=$1
    echo "Creating git tag for version $version..."

    git tag -a "v$version" -m "Release version $version - $(date +%Y-%m-%d)"

    if [ $? -eq 0 ]; then
        echo "✓ Git tag v$version created successfully"
    else
        echo "ERROR: Failed to create git tag"
        exit 1
    fi
}

# Print current version
print_version() {
    echo "Current version:"
    echo "  Gradle properties: $(grep '^APP_VERSION_' gradle.properties)"
    echo "  App build: $(grep 'versionName' app/build.gradle | head -1)"
}

# Show usage
usage() {
    echo "Usage: $0 <command> [version]"
    echo ""
    echo "Commands:"
    echo "  validate    - Validate version format in gradle.properties"
    echo "  info        - Print current version information"
    echo "  tag <ver>   - Create git tag for semantic version"
    echo ""
    exit 1
}

case "$1" in
    validate)
        validate_version
        ;;
    info)
        print_version
        ;;
    tag)
        if [ -z "$2" ]; then
            echo "Error: version required for tag command"
            usage
        fi
        create_tag "$2"
        ;;
    *)
        usage
        ;;
esac