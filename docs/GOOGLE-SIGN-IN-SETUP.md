# Tasks cloud setup and restore

## First list: your current phone

Install Tasks Android 0.87.0 over your current app; do not uninstall or clear data. Open that phone first. An account already connected in 0.86.0 uses the phone’s current list to initialize the corrected cloud format automatically. Wait for Settings → Google sync to say Synced with Google. If setting up a brand-new account, choose Use this phone’s tasks when prompted. The failed legacy cloud file is left untouched and is not imported into the new list.

After initialization, any other Tasks 0.87.0 client restores the cloud list on first sign-in. It does not merge an old installation’s task list into the initial cloud load. A private local backup is saved before replacement. Subsequent edits sync both ways, including deletions. Once connected, the account row shows the email and an × to disconnect; Sync now remains available. Setup and sign-in buttons hide while connected.

## Windows Google client configuration

The 0.86.0 Windows build omitted the client secret from token exchange. A live diagnostic against Google returned `invalid_request: client_secret is missing`. This was incorrectly displayed as expired authorization.

1. Open https://console.cloud.google.com/auth/clients and select the Tasks project.
2. Open the Windows client whose application type is Desktop app and download its JSON. If it is a Web application instead, create a Desktop app client in the same Tasks project and download that JSON.
3. Install Tasks Windows 0.87.0. Open Settings → Google sync → Set up Google and choose that JSON file in the native file picker.
4. Sign into the same Google account used on the current phone. Windows will restore its cloud tasks automatically.

The imported JSON must contain `installed`, `client_id` and `client_secret`, from project number 277712575915. Its client secret is used for both authorization-code exchange and token refresh, alongside PKCE and state. Configuration and refresh credentials are protected with Windows DPAPI CurrentUser. They never appear in the WebView, sync status or source repository. Import the JSON locally; do not paste it into chat.

Android registration remains package `com.personal.tasklist.preview`, signing SHA-1 `2B:E6:14:0F:A0:50:9D:13:6E:18:D5:25:06:2C:85:10:5D:57:9F:9C`, client `277712575915-pc43c20vte31fk611r6r5ipcqu7uml5f.apps.googleusercontent.com`. The original Windows client ID is `277712575915-aum3u79p81j3e83676mme4sgcf2l7ffg.apps.googleusercontent.com`; another Desktop app client in that same project can be imported if needed. These IDs and signing fingerprints are public configuration.

Drive API must be enabled. While the OAuth project is in Testing, add your Google account under Audience → Test users. Testing grants can expire after seven days and require another sign-in. Android uses Google Play services for short-lived access tokens and does not store a refresh token.

## Cloud storage and deleted tasks

Only Google Drive’s private app-data folder is used. An immutable phone seed (`tasks-cloud-v2-seed.json`) starts the list. Each installation writes a separate compact change document. Changes carry logical clocks; deletions remain as tombstones. Each client reconstructs the same list from the seed and changes, so a newly connected client cannot re-upload an old template or undo deletions merely by signing in. Media reads/updates do not use the metadata ETag that caused the repeated 412 loop.

Task state, projects, notes, checklists, priorities, completion/progress, ordering and theme are shared. UI filters, timestamps, account identity, configuration and tokens are excluded from task payloads. Different-account restores are backed up and isolated. Pending changes for a previously connected account retain their own private journal.

Phone sync runs while Tasks is open; widget edits sync on the next app open. PC sync continues while its background process runs. Local QR pairing remains available. Android reminders and the widget are phone features. Automated tests use mocked Google storage/identity, shared Android/Python protocol fixtures and real native Windows installation tests; live consent still requires the user’s Google account and imported Desktop client configuration.
