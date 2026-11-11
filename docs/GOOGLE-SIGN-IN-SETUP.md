# Google sign-in for Tasks: setup needed

The shipped Windows and Android 0.65.0 builds use local QR pairing. Google sign-in is a proposed additional connection method; it is not enabled in these downloads.

Google sign-in identifies the account. Cross-device task sync also needs storage. The intended option is Google Drive’s private app-data folder: the phone and PC sign into the same Google account and share Tasks data and theme through this folder. This removes the same-Wi-Fi requirement while retaining offline saves and the existing merge rules. QR pairing remains available.

## Google Cloud setup

1. Create or choose one Google Cloud project for Tasks.
2. Enable the Google Drive API in that project.
3. Configure the Google Auth consent screen. For personal testing, add your own Google account as a test user.
4. Register OAuth clients for the Android app and Windows desktop app in that same project.
5. Request the app-data-only Drive scope, `https://www.googleapis.com/auth/drive.appdata`, rather than access to all Drive files.

Current Android preview registration:

- Package: `com.personal.tasklist.preview`
- Signing certificate SHA-1: `2B:E6:14:0F:A0:50:9D:13:6E:18:D5:25:06:2C:85:10:5D:57:9F:9C`

The signing certificate fingerprint is public configuration. Keep the signing keystore, Google password, refresh tokens and any service-account keys private. A future Google Play signing certificate must be registered separately.

The installed apps need their own OAuth configuration and interactive user consent. ChatGPT’s Google account connection cannot be embedded into the apps. No account password or service-account key is needed to implement this.

## Implementation after configuration

- Add Sign in with Google alongside QR on the connection screen.
- Use a system-browser OAuth flow with PKCE and state on Windows, and Google’s native authorization flow on Android.
- Store credentials with Windows user encryption and Android Keystore-backed storage.
- Share task state and theme in private app-data storage, retaining local changes during outages and using version-aware writes and the existing three-way merge.
- Test account changes, revoked consent, expired credentials, offline edits and simultaneous task actions before publishing.

Official references:

- https://developers.google.com/identity/protocols/oauth2/native-app
- https://developers.google.com/identity/authorization/android/overview
- https://developers.google.com/workspace/drive/api/guides/appdata
