#!/bin/sh
# Bootstrap launcher, not the official Gradle Wrapper JAR launcher.
# This project was prepared offline. No gradle-wrapper.jar is bundled.
# The first local run downloads Gradle 8.11.1 over HTTPS and verifies its
# archive against the SHA-256 published by Gradle over HTTPS before running.
set -eu

GRADLE_VERSION=8.11.1
TASK_PROJECT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
TASK_GRADLE_CACHE=${GRADLE_USER_HOME:-"$HOME/.gradle"}
TASK_GRADLE_BASE="$TASK_GRADLE_CACHE/task-list-bootstrap/$GRADLE_VERSION"
TASK_GRADLE_BIN="$TASK_GRADLE_BASE/gradle-$GRADLE_VERSION/bin/gradle"

if [ ! -x "$TASK_GRADLE_BIN" ]; then
    command -v curl >/dev/null 2>&1 || { printf '%s\n' 'Install curl or open this project in Android Studio.' >&2; exit 1; }
    command -v unzip >/dev/null 2>&1 || { printf '%s\n' 'Install unzip or open this project in Android Studio.' >&2; exit 1; }
    mkdir -p "$TASK_GRADLE_BASE"
    TASK_ARCHIVE="$TASK_GRADLE_BASE/gradle-$GRADLE_VERSION-bin.zip"
    TASK_CHECKSUM="$TASK_ARCHIVE.sha256"
    TASK_DOWNLOAD_URL="https://services.gradle.org/distributions/gradle-$GRADLE_VERSION-bin.zip"
    printf '%s\n' "Downloading Gradle $GRADLE_VERSION; this first run requires internet access."
    curl --fail --location --retry 2 --connect-timeout 30 --max-time 120 --output "$TASK_CHECKSUM.part" "$TASK_DOWNLOAD_URL.sha256"
    curl --fail --location --retry 2 --connect-timeout 30 --max-time 120 --output "$TASK_ARCHIVE.part" "$TASK_DOWNLOAD_URL"
    TASK_EXPECTED_SHA=$(tr -d '[:space:]' < "$TASK_CHECKSUM.part")
    if [ ${#TASK_EXPECTED_SHA} -ne 64 ] || printf '%s' "$TASK_EXPECTED_SHA" | LC_ALL=C grep -q '[^0123456789abcdefABCDEF]'; then
        printf '%s\n' 'The official Gradle checksum response is invalid; refusing to execute the download.' >&2
        exit 1
    fi
    if command -v sha256sum >/dev/null 2>&1; then
        TASK_ACTUAL_SHA=$(sha256sum "$TASK_ARCHIVE.part" | cut -d ' ' -f 1)
    elif command -v shasum >/dev/null 2>&1; then
        TASK_ACTUAL_SHA=$(shasum -a 256 "$TASK_ARCHIVE.part" | cut -d ' ' -f 1)
    else
        printf '%s\n' 'Install sha256sum or shasum to verify Gradle; refusing to run an unverified download.' >&2
        exit 1
    fi
    if [ "$TASK_ACTUAL_SHA" != "$TASK_EXPECTED_SHA" ]; then
        printf '%s\n' 'Gradle checksum mismatch; refusing to execute the download.' >&2
        exit 1
    fi
    mv "$TASK_ARCHIVE.part" "$TASK_ARCHIVE"
    mv "$TASK_CHECKSUM.part" "$TASK_CHECKSUM"
    unzip -q -o "$TASK_ARCHIVE" -d "$TASK_GRADLE_BASE"
    chmod +x "$TASK_GRADLE_BIN"
fi

cd "$TASK_PROJECT_DIR"
exec "$TASK_GRADLE_BIN" "$@"
