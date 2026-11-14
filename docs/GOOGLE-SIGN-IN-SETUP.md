# Tasks Google sign-in and sync

Tasks Android and Windows 0.86.0 support optional Google sync through Drive’s private app-data folder. You can also use either app offline or pair over local Wi-Fi. Google sync shares tasks, completion/progress, projects, notes, checklists, ordering, labels and theme. UI view/filter choices stay local.

## Connect

Install the Android update and Windows installer. Open Settings → Google sync on each device and sign into the same Google account. On Windows, Connect also opens this screen. The system browser handles desktop authorization; Google Play services handles phone authorization. The account’s email and sync status appear in Settings. Use Sync now to request an immediate sync. Sign out leaves local tasks intact.

The configured Google project is Tasks (277712575915). Drive API must be enabled. While Google Auth Platform → Audience is in Testing, add each account under Test users. A testing grant can expire after seven days; sign in again if Google requests consent. No paid GitHub or Google subscription is required for this private app-data sync.

## Registered OAuth clients

Android: `277712575915-pc43c20vte31fk611r6r5ipcqu7uml5f.apps.googleusercontent.com`

- Package: `com.personal.tasklist.preview`
- Signing SHA-1: `2B:E6:14:0F:A0:50:9D:13:6E:18:D5:25:06:2C:85:10:5D:57:9F:9C`

Windows/Desktop: `277712575915-aum3u79p81j3e83676mme4sgcf2l7ffg.apps.googleusercontent.com`

Both clients belong to the same project. IDs and signing fingerprints are public configuration. Do not share the app signing keystore, refresh tokens or Google password.

## Storage and merging

Windows uses the system browser, loopback redirect, state validation and PKCE (S256). A refresh token is encrypted with Windows DPAPI CurrentUser; there is no plaintext fallback. Android keeps short-lived access tokens in memory and asks Google Play services for renewed authorization. Only the chosen account identity and task merge baseline persist in private preferences. Tokens are never sent to the WebView or local HTTP status endpoints.

The requested Drive scope is `https://www.googleapis.com/auth/drive.appdata`, plus verified email identity. The shared document is `tasks-state-v1.json` in `appDataFolder`, with a versioned Tasks envelope. Reads and writes use Drive v2 ETag preconditions; file creation uses Drive v3 private-folder metadata. Version conflicts retry with the existing three-way field/task merge. Local edits made during requests survive. Simultaneous first-time file creation resolves to the oldest file without deleting duplicate files or losing local data.

Google sync runs while the phone app is in the foreground and while the PC background process is running. Widget actions made while the phone app is closed sync when you next open Tasks. No real Google account is signed into automated tests; the actual account/consent flow must be checked after installing.

Official references:

- https://developers.google.com/identity/protocols/oauth2/native-app
- https://developer.android.com/identity/authorization
- https://developers.google.com/workspace/drive/api/guides/appdata
