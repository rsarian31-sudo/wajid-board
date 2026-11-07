# WAJID BOARD — private Android keyboard

An offline-first personal Android keyboard for the owner's Samsung A55 5G.

## Features in 0.2.0
- Android InputMethodService with English QWERTY.
- Avro Phonetic transliteration using the upstream OpenBangla Avro rule data and a Kotlin rule parser.
- Probhat fixed layout using the upstream OpenBangla Probhat keymap data, including the alternate layer.
- Clipboard panel: recent text copied while the keyboard is active, up to 10 entries / 3,000 characters each. History is in memory only and is cleared when the IME process is stopped; use Clear to empty it manually.
- Rounded keycaps, Android ripple feedback, press-scale animation, and light haptics.
- Offline; no INTERNET permission, analytics, telemetry, cloud sync, or key logging.

## Important status
This is source code version 0.2.0. It has not yet been built by GitHub Actions or tested on a physical Samsung device. Avro uses the official upstream rule table and adapted matching algorithm, but it still needs a real-device compatibility test against the Avro reference test corpus. Probhat keymap data is included; vowel composition/automatic kar joining may need further refinement. Clipboard history is intentionally session-only and does not persist after the keyboard process stops.

## Use
1. Install the APK after a successful Actions build.
2. Enable WAJID BOARD in Android keyboard settings.
3. Select it from the keyboard picker.
4. Tap the top language selector to cycle EN → অভ্র → প্রভাত.
5. In Avro mode, type romanized Bengali. Tap space to finish a word.
6. Tap the clipboard icon to view recent copied text.

## Build
JDK 17, Android SDK 35 and Gradle 8.7.
Run: `gradle :app:assembleDebug`
APK: `app/build/outputs/apk/debug/app-debug.apk`

GitHub Actions builds a debug APK on pushes to main or manual dispatch. Download the artifact from the workflow run.

## Privacy
The app requests VIBRATE only. Clipboard content is read by the active keyboard to populate its local in-memory history. It is not written to persistent storage or sent over a network. Android shows its standard third-party keyboard privacy warning.

## Licensing and attribution
See `LICENSE` and `THIRD_PARTY_NOTICES.md`. Avro phonetic and Probhat layout assets are from OpenBangla Keyboard, with their original attribution and GPL-3.0 licensing preserved. The adapted Avro parser is based on the OpenBangla AvroPhonetic implementation (dual-licensed MPL-2.0 / GPL-3.0).
