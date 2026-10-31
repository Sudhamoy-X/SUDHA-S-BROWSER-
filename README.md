# SUDHA BROWSER

**Your Web. Your Way.**

A fresh native Android browser project built independently with Kotlin, Jetpack Compose and Android WebView.

## Principles
- Build from scratch; do not reuse NYXORA or previous project source.
- Study publicly observable features and implement original code and UI.
- No fake controls: shipped controls must work.
- Privacy-first defaults and clear permission requests.
- Ship in tested phases.

## Technology
- Kotlin 2.1.21, Jetpack Compose / Material 3
- Android WebView
- Min SDK 26 (Android 8.0)
- JDK 17 / Gradle 8.11.1

## Build
Run `gradle assembleDebug`. APK: `app/build/outputs/apk/debug/app-debug.apk`.
Every push to `main` triggers GitHub Actions and uploads the debug APK artifact.

## Roadmap
1. Foundation: navigation, address/search, lifecycle and error handling.
2. Core browser: tabs, bookmarks, history, downloads and private sessions.
3. Privacy: ad/content blocking, tracking protection, data controls and site permissions.
4. Power tools: reader mode, translation integration, QR, PDF, image tools and gestures.
5. Media: video assistant, compatible-player controls and casting.
6. Collaboration: secure Browse Together rooms and Watch Together sync.
7. Hardening: tests, performance, accessibility and security review.

## Soul Browser public feature study
The published feature inventory includes translation, data saving, clean mode, block-area controls, download tools, layout/menu/search/tab-bar/gesture customization, one-handed mode, themes, screen filter, reader mode, text-to-speech, casting, keyboard security, app lock, incognito, scrolling screenshots, image-only viewing, image saving, video download, QR, PDF, photo tools and DNS via Android VPN APIs. These are research inputs, not copied source or assets.

Reference: https://play.google.com/store/apps/details?id=com.mycompany.app.soulbrowser
