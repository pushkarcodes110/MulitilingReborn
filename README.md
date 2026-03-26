# Multiling O Reborn

A modern, lightweight Android keyboard app that replaces **Multiling O Keyboard** with full Android 16 compatibility and sub-1MB size.

---

## Features

| Feature | Detail |
|---|---|
| **50+ Languages** | English (QWERTY/Dvorak/Colemak), Arabic, Russian, Hindi, Bengali, Korean, Japanese, Chinese (Pinyin), Thai, Hebrew, Greek, and 40+ more |
| **Emoji Panel** | 1000+ emoji across 9 categories with search |
| **6 Themes** | Dark Blue, AMOLED, Light, Material You, Forest, Ocean |
| **Smart Shift** | Auto-reset after one character; double-tap for caps lock |
| **Symbol Layouts** | Two symbol pages (123 and #+=) |
| **Swipe Actions** | Swipe left = backspace, swipe right = space |
| **Long-press Delete** | Deletes entire word |
| **Settings App** | Theme picker, text size, key roundness |
| **Android 16 Ready** | Canvas-based view, no deprecated KeyboardView API, proper edge-to-edge insets |
| **Size** | ~300–700KB release APK (with R8 + ProGuard shrinking) |
| **Permissions** | **Zero** dangerous permissions required |

---

## Building

### Requirements
- Android Studio Hedgehog or newer
- JDK 17 if building from the command line

### Steps

```bash
# Clone the repository
git clone <your-repo-url>
cd MultilingReborn

# Build the debug APK
./gradlew assembleDebug

# Optional: build the release APK (requires signing config)
./gradlew assembleRelease

# Output locations:
# app/build/outputs/apk/debug/app-debug.apk
# app/build/outputs/apk/release/app-release-unsigned.apk
```

### Easiest way to build

1. Open the project in Android Studio
2. Let Gradle sync
3. If prompted, let Android Studio install missing SDK components
4. Click **Run** or **Build > Build APK(s)**

### Notes for fresh machines

- `local.properties` is intentionally not committed because it contains a machine-specific Android SDK path.
- Android Studio usually creates `local.properties` automatically on first sync.
- If you build from the terminal, make sure the Android SDK is installed and `local.properties` points to it.

### Install on device

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

### First-time setup on device
1. Open the **Multiling O Reborn** launcher app
2. Tap **Enable Keyboard** → enable in Android settings
3. Tap **Switch to This Keyboard** → select it as active IME
4. Done! Tap 🌐 to cycle languages, ☺ for emoji

---

## Architecture

```
KeyboardService.kt      ← InputMethodService (IME entry point)
KeyboardView.kt         ← Full Canvas-based key rendering + touch
LayoutManager.kt        ← All 50+ language row definitions
EmojiPanel.kt           ← Emoji grid with categories + search
ThemePrefs.kt           ← 6 built-in color themes
SettingsActivity.kt     ← Launcher app for setup + customisation
KeyCode.kt              ← Key code constants
```

### Why Canvas instead of the old KeyboardView?
The `android.inputmethodservice.KeyboardView` class was **deprecated in API 29** and causes layout issues on Android 12+ edge-to-edge and on Android 16's enforced inset handling. This app renders every key via `Canvas.drawRoundRect()` directly, giving full control over sizing, theming, and future gesture support.

---

## Adding a New Language

In `LayoutManager.kt`, add a new `LangDef` to the `buildLanguageList()` function:

```kotlin
LangDef("xx", "My Language", listOf(
    row("a","b","c","d","e","f","g","h","i","j"),
    row("k","l","m","n","o","p","q","r","s"),
    row("t","u","v","w","x","y","z")
))
```

Characters with shift variants: `row("aA","bB",...)` — first char = lower, second = upper.

---

## Android 16 Compatibility Notes

- **Edge-to-edge**: `InputMethodService` handles insets automatically on API 30+; no `SOFT_INPUT_ADJUST_RESIZE` used.
- **IME window**: The keyboard view is returned from `onCreateInputView()` with dynamic height from `onMeasure()`, which cooperates correctly with the new predictive back gesture and gesture navigation bar.
- **compileSdk 35**: Update to 36 once Android 16 SDK is stable (mid-2025). No breaking API changes expected for the code in this project.

---

## License

Apache License 2.0. See [LICENSE](LICENSE).
