# Windows 0.91.0 installer verification

[Successful build and checks](https://github.com/swettgm/task-list-android/actions/runs/44293762640). Source `474c34208af8c135b084f2c3f388d3057eb90e16`. The shared Tasks UI now shows an active-project count badge beside the date. Existing dragging and swipe behavior are retained. All 41 unit, 35 browser/HTTP, nine native-window and eleven installer checks passed, including live upgrade and saved-data retention. Taskbar identity, window reuse and per-user installation paths are unchanged.

`Tasks-Setup-v0.91.0.exe`, 17338239 bytes; SHA-256 `54de9ce2d4c53b79c6a02da2c34b88529fff6cfc84f96ab0a6feb1b279b5ddc8`.

# Windows 0.90.0 installer verification

[Successful build and checks](https://github.com/swettgm/task-list-android/actions/runs/44224919888). Source `26dbcd8cbc699bb861887f3ce9095fc5f7b5edbf`. Shared task dragging now matches Relationship Manager pickup and placement. All 41 unit, 35 browser/HTTP, nine native-window and eleven installer checks passed, including live upgrade and saved-data retention. Taskbar identity, window reuse and per-user installation paths are unchanged.

`Tasks-Setup-v0.90.0.exe`, 17337013 bytes; SHA-256 `74aeccac42f8bb4725681b9c0768fa729742cc974399b4f5f6b78a7c5c1bb199`.

# Windows 0.89.0 installer verification

[Successful build and checks](https://github.com/swettgm/task-list-android/actions/runs/44223397443). Source `2cb4e7d9023c0b08d3cbfe2a920296df90c729fa`. Shared task dragging now matches Relationship Manager pickup and placement. All 41 unit, 35 browser/HTTP, nine native-window and eleven installer checks passed, including live upgrade and saved-data retention. Taskbar identity, window reuse and per-user installation paths are unchanged.

`Tasks-Setup-v0.89.0.exe`, 17336068 bytes; SHA-256 `7098a3535b2335d89206db70e4af9a6c0c6c4bdd4a9d9e293aa5094a2d14f4b4`.

# Windows 0.88.0 installer verification

[Successful Windows build and checks](https://github.com/swettgm/task-list-android/actions/runs/43723414190). Source `568cdf2f77b4ac1014732eb977b7e35d0c6d9cb6`. All existing native-window, installation, live-upgrade and data-preservation checks passed. Home Refresh now requests Google sync after pending window edits reach the native host; its new HTTP/browser regression test passed. Stable taskbar identity and per-user installation paths are retained.

`Tasks-Setup-v0.88.0.exe`, 17338345 bytes; SHA-256 `7568f8fc09e27ca470b5ce8819b93ebc4239649948555f96d19cda3dcec0560d`.

# Windows installer 0.87.0 verification

[Successful Windows build](https://github.com/swettgm/task-list-android/actions/runs/43666000901). Source `6a864b5d98a5d083fc1f98d270a56710be9e67f1`. The actual installer passed per-user install, both shortcuts, Apps registration, installed startup, running upgrade, shutdown/removal and retention of personal task/theme/pairing data. Google Desktop configuration is imported locally and encrypted with DPAPI; cloud restore saves an old-list backup before replacement.

`Tasks-Setup-v0.87.0.exe`, 17335515 bytes. SHA-256 `4766522c99ebfeeb3796a4e53a43094e74f81c139772ffada83255f9502e9c3c`.

# Windows installer 0.86.0 verification

[Successful Windows build and checks](https://github.com/swettgm/task-list-android/actions/runs/43653938352). Source: `aec67775bde7312414225f07e4ea2da778472def`.

The actual installer passed fresh per-user installation, Start menu and desktop shortcuts, Apps uninstall registration, installed startup, upgrade while running, shutdown during upgrade/uninstall, program/shortcut removal and byte-for-byte retention of personal tasks, theme and pairing. Program files stay separate from `%LOCALAPPDATA%\Tasks`. The full Windows task UI and Google connection controls are included. DPAPI credential encryption is tested with the current Windows user.

Installer: `Tasks-Setup-v0.86.0.exe`, 17323002 bytes, SHA-256 `0220e99ef9c357b03d2cb4ec4e02011108e349c8119f8294a7b64b5996f76925`.

# Windows installer 0.65.1 verification

[Successful Windows build and checks](https://github.com/swettgm/task-list-android/actions/runs/42115166068). Source: `ee5b932ea58b284a3b17f30642680777c40bc1ad`.

The Windows installer adds Tasks to the current user's installed apps, creates a Start menu shortcut and an optional desktop shortcut, and installs a standard Windows uninstaller. It uses a stable application ID for future upgrades and requires no administrator rights. Program files live under `%LOCALAPPDATA%\Programs\Tasks`; task state, theme and pairing remain separately under `%LOCALAPPDATA%\Tasks`.

The actual installer was executed on a Windows runner. Checks verify a fresh per-user installation, both shortcuts, the Windows Apps uninstall registration, startup of the installed executable, upgrade while the companion is running, shutdown during upgrade and uninstall, and removal of program files and shortcuts. Saved tasks/theme and the pairing token remain byte-for-byte intact through upgrade and uninstall. All tests use isolated temporary data.

20 Python protocol/persistence tests, 19 browser task/sync/theme checks and the existing packaged-EXE smoke test also passed. Android 0.65.0 remains compatible and was not changed or rebuilt for this packaging update. Google sign-in is still pending its separate OAuth setup.

Installer: `Tasks-Setup-v0.65.1.exe`, 11544015 bytes. SHA-256: `9b04e1154f0f0f1c81576cd399ffa4484d4220deed9ca5a899b13402bf80ef1f`.

The artifact SHA-256 digest and ZIP CRCs, installer checksum, Windows GUI PE headers and public download bytes were verified. The installer has no commercial Authenticode publisher signature. This release is tested on a Windows CI runner, not the user's own PC.
