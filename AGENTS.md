# AGENTS.md - Panorama Android Home Launcher Guidelines

## Project Overview
"Panorama" (package: `com.natanael.panorama`) is a minimal, elegant Android Home Launcher built with Jetpack Compose, Kotlin, MVI architecture, and StateFlow.

## Build & Gradle Rules
- Build system: Kotlin DSL (`build.gradle.kts`, `settings.gradle.kts`).
- Root `build.gradle.kts` plugins:
  - `com.android.application` version `8.2.2`
  - `org.jetbrains.kotlin.android` version `1.9.22`
- App `app/build.gradle.kts`:
  - `namespace = "com.natanael.panorama"`
  - `compileSdk = 34`, `minSdk = 26`, `targetSdk = 34`
  - Java 17 compatibility (`jvmTarget = "17"`, `kotlinCompilerExtensionVersion = "1.5.8"`)
  - Compose BOM `2024.02.01`, Material 3, `material-icons-extended`, `lifecycle-viewmodel-compose:2.7.0`, `datastore-preferences:1.0.0`
- Build command for CI/CD and local verification: `gradle assembleDebug --stacktrace` (Do NOT use `./gradlew` as `gradle-wrapper.jar` is omitted).

## Data Contracts (`com.natanael.panorama.model.AppContracts.kt`)
The following data structures are frozen contracts:
```kotlin
package com.natanael.panorama.model

import android.graphics.drawable.Drawable
import androidx.compose.runtime.Immutable

@Immutable
data class AppItem(
    val packageName: String,
    val activityName: String,
    val label: String,
    val icon: Drawable? = null,
    val isHidden: Boolean = false,
    val isFavorite: Boolean = false
)

@Immutable
data class PanoramaUiState(
    val allApps: List<AppItem> = emptyList(),
    val favoriteApps: List<AppItem> = emptyList(),
    val filteredApps: List<AppItem> = emptyList(),
    val hiddenPackageNames: Set<String> = emptySet(),
    val favoritePackageNames: Set<String> = emptySet(),
    val selectedLetter: Char? = null,
    val isScrubbing: Boolean = false,
    val isSettingsOpen: Boolean = false,
    val currentTimeString: String = "",
    val currentDateString: String = ""
)

sealed interface PanoramaUiEvent {
    data class OnLetterScrub(val letter: Char?) : PanoramaUiEvent
    data class OnAppClicked(val app: AppItem) : PanoramaUiEvent
    data class OnToggleAppVisibility(val packageName: String, val hide: Boolean) : PanoramaUiEvent
    data class OnToggleFavorite(val packageName: String, val favorite: Boolean) : PanoramaUiEvent
    data class SetSettingsVisible(val visible: Boolean) : PanoramaUiEvent
    data object OnSwitchLauncherRequested : PanoramaUiEvent
    data object OnRefreshApps : PanoramaUiEvent
}
```

## Key Technical Specifications
1. **Manifest & Role:** Request `QUERY_ALL_PACKAGES`. `MainActivity` configured with `launchMode="singleTask"`, `clearTaskOnLaunch="true"`, `stateNotNeeded="true"`, and home launcher intent filters (`MAIN`, `CATEGORY_HOME`, `CATEGORY_DEFAULT`).
2. **Transparent Wallpaper:** Theme `Theme.Panorama.Wallpaper` with transparent window background and `android:windowShowWallpaper="true"`. In code, apply `WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER`.
3. **Right-aligned Clock & Date Header:** Text shadow for readability on all wallpapers. Tapping clock launches `AlarmClock.ACTION_SHOW_ALARMS`; tapping date launches `Intent.CATEGORY_APP_CALENDAR`.
4. **Alphabet Wave Scrubber:** Right edge vertical strip (`☆`, `A-Z`, `#`). Smooth wave horizontal offset curve towards the left based on touch vertical distance. Subtle haptic feedback (`HapticFeedbackType.TextHandleMove`) on letter change.
5. **Right-aligned App List:** Right-aligned app cards. When scrubbing letter, show apps starting with that letter. When idle, show favorite apps (`☆`). Safe icon conversion using `Bitmap.createBitmap` and `android.graphics.Canvas` wrapped in `remember(drawable)`.
6. **Double-tap & Settings:** Double tap empty space triggers default launcher selector (`Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS` / `CATEGORY_HOME` intent chooser). Long press opens settings sheet/dialog.
7. **Identity Watermark:** Display **"Panorama • by Natanael"** in settings bottom and subtly under header.
