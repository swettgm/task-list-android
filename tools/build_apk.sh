#!/usr/bin/env bash
# Environment bootstrap and APK validation. Run with network access enabled.
# Uses inherited proxy/CA configuration; never disables certificate validation.
set -Eeuo pipefail

TASK_PROJECT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)
TASK_SDK_DIR=/workspace/android-sdk
TASK_DOWNLOAD_DIR=/workspace/downloads
export GRADLE_USER_HOME=/workspace/.gradle
export ANDROID_HOME="$TASK_SDK_DIR"
export ANDROID_SDK_ROOT="$TASK_SDK_DIR"
TASK_LOG=/workspace/task-list/build.log
mkdir -p "$TASK_DOWNLOAD_DIR" "$TASK_SDK_DIR" "$GRADLE_USER_HOME"
exec > >(tee -a "$TASK_LOG") 2>&1
trap 'printf "Build failed at line %s. Full log: %s\n" "$LINENO" "$TASK_LOG"' ERR
printf '\nAndroid build started: %s\n' "$(date -u +%FT%TZ)"

# Android's Java-image transform requires a complete JDK, including jlink,
# javac, ct.sym and jmods; the environment's initial Java runtime is stripped.
TASK_JDK_DIR=/workspace/jdk-21
if [[ ! -x "$TASK_JDK_DIR/bin/javac" || ! -x "$TASK_JDK_DIR/bin/jlink" || ! -f "$TASK_JDK_DIR/jmods/java.base.jmod" ]]; then
    printf 'Downloading a complete Eclipse Temurin 21 JDK.\n'
    TASK_JDK_METADATA="$TASK_DOWNLOAD_DIR/temurin-21-metadata.json"
    case "$(uname -m)" in
        x86_64) TASK_JDK_ARCH=x64 ;;
        aarch64) TASK_JDK_ARCH=aarch64 ;;
        *) printf 'Unsupported JDK architecture.\n' >&2; exit 1 ;;
    esac
    curl --fail --location --retry 2 --connect-timeout 20 --max-time 120 \
        --output "$TASK_JDK_METADATA" \
        "https://api.adoptium.net/v3/assets/latest/21/hotspot?architecture=$TASK_JDK_ARCH&image_type=jdk&os=linux&vendor=eclipse"
    mapfile -t TASK_JDK_PACKAGE < <(python3 - "$TASK_JDK_METADATA" <<'PY'
import json,re,sys
with open(sys.argv[1]) as f:
    package=json.load(f)[0]['binary']['package']
if not package['link'].startswith('https://') or not re.fullmatch('[0-9a-fA-F]{64}',package['checksum']):
    raise SystemExit('Invalid vendor JDK package metadata')
print(package['link'])
print(package['checksum'].lower())
PY
    )
    [[ ${#TASK_JDK_PACKAGE[@]} -eq 2 ]] || { printf 'Missing vendor JDK package metadata.\n' >&2; exit 1; }
    TASK_JDK_ARCHIVE="$TASK_DOWNLOAD_DIR/temurin-21-jdk.tar.gz"
    if [[ ! -f "$TASK_JDK_ARCHIVE" ]] || [[ "$(sha256sum "$TASK_JDK_ARCHIVE" | cut -d ' ' -f 1)" != "${TASK_JDK_PACKAGE[1]}" ]]; then
        curl --fail --location --retry 2 --connect-timeout 20 --max-time 120 \
            --output "$TASK_JDK_ARCHIVE.part" "${TASK_JDK_PACKAGE[0]}"
        printf '%s  %s\n' "${TASK_JDK_PACKAGE[1]}" "$TASK_JDK_ARCHIVE.part" | sha256sum --check --status
        mv "$TASK_JDK_ARCHIVE.part" "$TASK_JDK_ARCHIVE"
    fi
    mkdir -p "$TASK_JDK_DIR"
    tar -xzf "$TASK_JDK_ARCHIVE" --strip-components=1 -C "$TASK_JDK_DIR"
fi
export JAVA_HOME="$TASK_JDK_DIR"
export PATH="$JAVA_HOME/bin:$PATH"
test -x "$JAVA_HOME/bin/javac"
test -x "$JAVA_HOME/bin/jlink"
test -f "$JAVA_HOME/lib/ct.sym"

# Proxy hosts and ports only: credentials, if present, are never printed or
# copied into Java properties. Authentication remains on the inherited path.
mapfile -t TASK_PROXY_FLAGS < <(python3 - <<'PY'
import os
from urllib.parse import urlsplit
for protocol in ('http','https'):
    raw=os.environ.get(protocol.upper()+'_PROXY') or os.environ.get(protocol+'_proxy')
    if not raw: continue
    proxy=urlsplit(raw if '://' in raw else 'http://'+raw)
    if proxy.hostname:
        print('-D'+protocol+'.proxyHost='+proxy.hostname)
        print('-D'+protocol+'.proxyPort='+str(proxy.port or (443 if proxy.scheme=='https' else 80)))
PY
)
TASK_PROXY_FLAGS+=('-Dsun.net.client.defaultConnectTimeout=20000' '-Dsun.net.client.defaultReadTimeout=120000')

# The managed proxy CA is installed in system PEM trust after the JDK image
# was created. Copy Java's normal trust store, then add the configured local
# CA certificates without changing global/system files.
TASK_TRUSTSTORE="$GRADLE_USER_HOME/task-list-cacerts"
if [[ -f /etc/ssl/certs/java/cacerts ]]; then
    cp /etc/ssl/certs/java/cacerts "$TASK_TRUSTSTORE"
    for TASK_CA_FILE in /usr/local/share/ca-certificates/*.crt; do
        [[ -f "$TASK_CA_FILE" ]] || continue
        TASK_CA_ALIAS="task-list-$(basename "$TASK_CA_FILE" .crt)"
        keytool -importcert -noprompt -trustcacerts -storepass changeit \
            -keystore "$TASK_TRUSTSTORE" -alias "$TASK_CA_ALIAS" \
            -file "$TASK_CA_FILE" >/dev/null 2>&1
    done
    TASK_PROXY_FLAGS+=("-Djavax.net.ssl.trustStore=$TASK_TRUSTSTORE" '-Djavax.net.ssl.trustStorePassword=changeit')
fi

TASK_SDKMANAGER="$TASK_SDK_DIR/cmdline-tools/latest/bin/sdkmanager"
if [[ ! -x "$TASK_SDKMANAGER" ]]; then
    TASK_TOOLS_ZIP="$TASK_DOWNLOAD_DIR/commandlinetools-linux-13114758_latest.zip"
    if [[ ! -f "$TASK_TOOLS_ZIP" ]] || ! unzip -tq "$TASK_TOOLS_ZIP" >/dev/null 2>&1; then
        printf 'Downloading official Android command-line tools.\n'
        curl --fail --location --retry 2 --connect-timeout 20 --max-time 120 \
            --output "$TASK_TOOLS_ZIP.part" \
            https://dl.google.com/android/repository/commandlinetools-linux-13114758_latest.zip
        mv "$TASK_TOOLS_ZIP.part" "$TASK_TOOLS_ZIP"
    fi
    TASK_UNPACK=$(mktemp -d "$TASK_DOWNLOAD_DIR/android-tools.XXXXXX")
    unzip -q "$TASK_TOOLS_ZIP" -d "$TASK_UNPACK"
    mkdir -p "$TASK_SDK_DIR/cmdline-tools"
    mv "$TASK_UNPACK/cmdline-tools" "$TASK_SDK_DIR/cmdline-tools/latest"
    rmdir "$TASK_UNPACK"
fi

TASK_SDKMANAGER_FLAGS=$(printf '%s ' "${TASK_PROXY_FLAGS[@]}")
export SDKMANAGER_OPTS="${SDKMANAGER_OPTS:-} $TASK_SDKMANAGER_FLAGS"
printf 'Accepting Android SDK licenses and checking SDK packages.\n'
# yes exits with SIGPIPE when sdkmanager finishes; only sdkmanager's exit code
# determines whether license acceptance succeeded.
set +o pipefail
yes | "$TASK_SDKMANAGER" --sdk_root="$TASK_SDK_DIR" --licenses
TASK_LICENSE_STATUS=${PIPESTATUS[1]}
set -o pipefail
if [[ "$TASK_LICENSE_STATUS" -ne 0 ]]; then exit "$TASK_LICENSE_STATUS"; fi
"$TASK_SDKMANAGER" --sdk_root="$TASK_SDK_DIR" \
    'platforms;android-35' 'build-tools;35.0.0' 'platform-tools'
printf 'sdk.dir=%s\n' "$TASK_SDK_DIR" > "$TASK_PROJECT_DIR/local.properties"

# Forward the non-secret networking/trust properties to Robolectric test
# workers, which resolve their instrumented Android runtime independently.
TASK_INIT="$GRADLE_USER_HOME/task-list-network.init.gradle"
cat > "$TASK_INIT" <<'GRADLE'
allprojects {
    tasks.withType(org.gradle.api.tasks.testing.Test).configureEach {
        ['http.proxyHost', 'http.proxyPort', 'https.proxyHost', 'https.proxyPort',
         'javax.net.ssl.trustStore', 'javax.net.ssl.trustStorePassword'].each { key ->
            def value = System.getProperty(key)
            if (value != null) systemProperty key, value
        }
    }
}
GRADLE

printf 'Building debug APK; running unit tests and Android lint.\n'
cd "$TASK_PROJECT_DIR"
./gradlew "${TASK_PROXY_FLAGS[@]}" --init-script "$TASK_INIT" \
    --console=plain --stacktrace :app:assembleDebug :app:testDebugUnitTest :app:lintDebug
TASK_APK="$TASK_PROJECT_DIR/app/build/outputs/apk/debug/app-debug.apk"
test -s "$TASK_APK"
"$TASK_SDK_DIR/build-tools/35.0.0/apksigner" verify --verbose "$TASK_APK"
printf '\nAPK ready: %s\nBuild log: %s\n' "$TASK_APK" "$TASK_LOG"
