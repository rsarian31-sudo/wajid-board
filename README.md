# WAJID BOARD — private Android keyboard prototype

A personal, offline-first Android IME prototype for the owner's Samsung A55 5G.

## Current prototype scope
- Android system keyboard service (InputMethodService).
- English QWERTY layout.
- Bengali direct-key starter layout.
- English-to-Bengali common-word replacement (small Avro-style starter dictionary; not a full Avro engine yet).
- Number/symbol page, language toggle, backspace, enter, shift and light haptic feedback.
- No INTERNET permission, analytics, telemetry, cloud sync or key logging.

## Important limitations
This is source code v0.1.0, not a tested or signed APK. It has not been built on a real Android SDK or tested on a Samsung A55. Bengali direct-key layout and transliteration dictionary are starter implementations; they need proper Probhat mapping and a full phonetic engine before production use. Clipboard history, gesture typing, candidate prediction, advanced animations and theme settings are not implemented yet.

## Build
Requires JDK 17, Android SDK 35 and Gradle 8.7.
Run: `gradle :app:assembleDebug`
APK: `app/build/outputs/apk/debug/app-debug.apk`

A GitHub Actions workflow builds the debug APK on pushes to main or by manual dispatch. Download it from the workflow run's Artifacts section.

## Privacy
The app requests only VIBRATE; it does not request Internet access. The IME does not intentionally store or transmit typed content. Android displays its standard warning when enabling a third-party keyboard.

## Credits
This prototype is newly written and does not copy Orbit Board's private v1.3.9 code. Orbit Board's public repository states that its public source is available through v1.0.8. If its source is incorporated later, preserve its license notices and comply with applicable terms.