# Windows installer 0.65.1 verification

[Successful Windows build and checks](https://github.com/swettgm/task-list-android/actions/runs/42115166068). Source: `ee5b932ea58b284a3b17f30642680777c40bc1ad`.

The Windows installer adds Tasks to the current user's installed apps, creates a Start menu shortcut and an optional desktop shortcut, and installs a standard Windows uninstaller. It uses a stable application ID for future upgrades and requires no administrator rights. Program files live under `%LOCALAPPDATA%\Programs\Tasks`; task state, theme and pairing remain separately under `%LOCALAPPDATA%\Tasks`.

The actual installer was executed on a Windows runner. Checks verify a fresh per-user installation, both shortcuts, the Windows Apps uninstall registration, startup of the installed executable, upgrade while the companion is running, shutdown during upgrade and uninstall, and removal of program files and shortcuts. Saved tasks/theme and the pairing token remain byte-for-byte intact through upgrade and uninstall. All tests use isolated temporary data.

20 Python protocol/persistence tests, 19 browser task/sync/theme checks and the existing packaged-EXE smoke test also passed. Android 0.65.0 remains compatible and was not changed or rebuilt for this packaging update. Google sign-in is still pending its separate OAuth setup.

Installer: `Tasks-Setup-v0.65.1.exe`, 11544015 bytes. SHA-256: `9b04e1154f0f0f1c81576cd399ffa4484d4220deed9ca5a899b13402bf80ef1f`.

The artifact SHA-256 digest and ZIP CRCs, installer checksum, Windows GUI PE headers and public download bytes were verified. The installer has no commercial Authenticode publisher signature. This release is tested on a Windows CI runner, not the user's own PC.
