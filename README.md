# NoteMax

An  Material 3 dynamic-color note-taking app for Android, built with Kotlin and Jetpack Compose.

## Features

- Dynamic color (Material You) theming with light/dark support
- Rich note editor with checklists and markdown rendering
- Bi-directional (RTL/LTR) text support and smooth animations
- Biometric protection for private notes
- Local storage with Room

## Build

Requires JDK 17+ and the Android SDK.

```bash
./gradlew assembleDebug
```

The debug APK is output to `app/build/outputs/apk/debug/`.

## Configuration

Copy `.env.example` to `.env` and uncomment `GEMINI_API_KEY` if the app uses the Gemini API. The key is never packaged into release builds unless explicitly set.

## License

See [LICENSE](LICENSE).
