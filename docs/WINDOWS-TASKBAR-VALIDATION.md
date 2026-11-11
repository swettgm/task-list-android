# Windows 0.65.2 taskbar verification

[Successful Windows build and checks](https://github.com/swettgm/task-list-android/actions/runs/42117912556). Source: `7a864511a648467ab764d99763e64d31ed7e4809`.

Tasks now hosts its interface in a native WinForms/WebView2 window owned by Tasks-PC.exe. It no longer starts Edge app mode for the main window. The process, visible window and installed Start menu/desktop shortcuts use `Personal.Tasks.PC` as their AppUserModelID. Window relaunch properties point at the real executable and its embedded icon, with PreventPinning set to false.

The actual frozen EXE is tested for native window ownership, the window property-store AppUserModelID and relaunch command, restoring a minimized window, hiding on close while sync continues, reopening the same hidden window, and shutting down both window and local host. The same native-window test runs against the installed copy, and the actual Start menu shortcut's AppUserModelID is checked.

Setup/upgrade/uninstall checks, 21 Python protocol/persistence tests, 19 browser task/sync/theme checks and the existing packaged-executable protocol smoke test passed. Installation, upgrading a running copy and uninstall preserve tasks, theme and pairing. Android 0.65.0 and the task interface are unchanged.

Setup includes the official Microsoft WebView2 bootstrapper, whose Microsoft Authenticode signature is validated during the build. It runs only if the runtime is missing; this prerequisite installation needs internet access. The setup itself has no commercial publisher signature.

An old pin copied from the Edge-hosted version may retain its old identity. Users should close that old window, install this update, unpin the old taskbar icon once, then pin Tasks from Start again. The new identity stays stable for future updates.

Installer: `Tasks-Setup-v0.65.2.exe`, 17282437 bytes. SHA-256: `1f724c9eac6d103b33c32ef91d5bd902da4e08c598f1c6650f50711e21654ca9`.

Artifact digest, ZIP CRCs, installer checksum, Windows GUI PE headers and public download bytes were verified. Tests run on a Windows CI runner. Physical interaction with the user's taskbar pin menu was not tested.
