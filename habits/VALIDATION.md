# Habits 1.0.7 verification

Source: `a1e4d6cce7222a14b911920ae64037ac17b107b8`. [Build and full checks](https://github.com/swettgm/task-list-android/actions/runs/44074121766). **37 native Android tests and zero lint errors passed.**

Widget double taps cycle empty → X (state 2) → check (state 1) → empty. The in-app single-tap sequence is unchanged. No persisted state meanings are remapped, so existing saved checks and X marks retain their meanings. The two-tap gate, per-cell/date pairing, future-day locks and haptics remain. Accessible widget descriptions announce the actual next state. Tests cover the reversed sequence, single/slow taps, saved-history meaning and stale/future taps.

APK: `Habits-v1.0.7.apk`, 37694 bytes. SHA-256 `a836f7ce24505fcb5fb6e7fccef3eb9edd0e6ef55243b6811517d644d0b69dee`. Package `com.personal.habits`. Artifact hashes, ZIP integrity, package/version, alignment, retained signer and exact non-signature content equality to CI passed. Updates retain existing app data. Native checks use Robolectric; browser checks use Chromium. No physical phone was tested.

