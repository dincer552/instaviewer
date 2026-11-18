# InstaViewer

Android app for opening Instagram links without installing the Instagram app.

## Features
- Paste an Instagram URL or use the Android Share menu.
- Accepts shared text and Instagram HTTPS links.
- Opens public Instagram pages in an embedded WebView.
- No Instagram username or password is collected.

## Build
Requires JDK 17, Android SDK 35, and Gradle 8.9.

```bash
gradle assembleDebug
```

APK: `app/build/outputs/apk/debug/app-debug.apk`

## Important limitation
Instagram controls access to its content. Public posts may open, but Instagram can still require login or restrict playback. This app does not bypass private-account permissions, login walls, or platform restrictions.
