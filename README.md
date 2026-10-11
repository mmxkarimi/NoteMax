<h1 align="center">NoteMax</h1>

<p align="center">
  An expressive Material 3 note-taking app for Android, with dynamic color.<br>
  Built with Kotlin and Jetpack Compose.
</p>

<p align="center">
  <a href="https://github.com/mmxkarimi/NoteMax/actions/workflows/release.yaml"><img alt="Build and release Android APK" src="https://github.com/mmxkarimi/NoteMax/actions/workflows/release.yaml/badge.svg"></a>
  <a href="LICENSE"><img alt="License: Apache 2.0" src="https://img.shields.io/badge/License-Apache%202.0-blue.svg"></a>
  <img alt="Platform" src="https://img.shields.io/badge/platform-Android-3DDC84?logo=android&logoColor=white">
  <img alt="Kotlin" src="https://img.shields.io/badge/Kotlin-7F52FF?logo=kotlin&logoColor=white">
  <img alt="Jetpack Compose" src="https://img.shields.io/badge/Jetpack%20Compose-4285F4?logo=jetpackcompose&logoColor=white">
  <img alt="Material 3" src="https://img.shields.io/badge/Material%203-6750A4">
</p>

<!-- TODO: add screenshots, for example in docs/screenshots/ -->
<!--
<p align="center">
  <img src="docs/screenshots/light.png" width="240" alt="Light theme">
  <img src="docs/screenshots/dark.png" width="240" alt="Dark theme">
  <img src="docs/screenshots/editor.png" width="240" alt="Note editor">
</p>
-->

## ✨ Features

- 🎨 **Dynamic color:** Material You theming that follows your wallpaper, with light and dark modes
- 📝 **Rich editor:** checklists and markdown rendering
- 🌍 **RTL and LTR support:** write in any direction
- 🎞️ **Smooth animations** across the app
- 💾 **Local storage** with Room, so your notes stay on your device

## 🧰 Tech Stack

| Area     | Choice                      |
| -------- | --------------------------- |
| Language | Kotlin                      |
| UI       | Jetpack Compose, Material 3 |
| Storage  | Room                        |

## 🚀 Getting Started

### What you need

- JDK 17 or newer
- Android SDK

### Build

```bash
git clone https://github.com/mmxkarimi/NoteMax.git
cd NoteMax
./gradlew assembleDebug
```

The debug APK is saved in `app/build/outputs/apk/debug/`.

### Install on a device

```bash
./gradlew installDebug
```

## 🤝 Contributing

Ideas and fixes are welcome. Open an issue to talk about a change, then send a pull request.

## 📄 License

This project is licensed under the [Apache License 2.0](LICENSE).