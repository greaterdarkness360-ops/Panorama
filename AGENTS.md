# AGENTS.md - Panorama Android Home Launcher Guidelines

## Project Overview
"Panorama" (package: `com.natanael.panorama`) is a high-performance 120FPS minimal Android Home Launcher supporting Dual/Clone Apps built with Jetpack Compose, Kotlin, MVI architecture, and StateFlow.

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

import android.content.ComponentName
import android.os.UserHandle
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.ImageBitmap

@Immutable
data class AppItem(
    val uniqueKey: String,
    val packageName: String,
    val activityName: String,
    val componentName: ComponentName,
    val userHandle: UserHandle,
    val label: String,
    val isClone: Boolean = false,
    val iconBitmap: ImageBitmap? = null,
    val isHidden: Boolean = false,
    val isFavorite: Boolean = false
)

@Immutable
data class PanoramaUiState(
    val allApps: List<AppItem> = emptyList(),
    val favoriteApps: List<AppItem> = emptyList(),
    val filteredApps: List<AppItem> = emptyList(),
    val hiddenKeys: Set<String> = emptySet(),
    val favoriteKeys: Set<String> = emptySet(),
    val selectedLetter: Char? = null, // null or '☆' displays Favorites
    val isSettingsOpen: Boolean = false
)

sealed interface PanoramaUiEvent {
    data class OnLetterSelected(val letter: Char?) : PanoramaUiEvent
    data object OnClearLetterFilter : PanoramaUiEvent
    data class OnAppClicked(val app: AppItem) : PanoramaUiEvent
    data class OnToggleAppVisibility(val uniqueKey: String, val hide: Boolean) : PanoramaUiEvent
    data class OnToggleFavorite(val uniqueKey: String, val favorite: Boolean) : PanoramaUiEvent
    data class SetSettingsVisible(val visible: Boolean) : PanoramaUiEvent
    data object OnSwitchLauncherRequested : PanoramaUiEvent
    data object OnRefreshApps : PanoramaUiEvent
}
```

## Critical Technical Specifications (120FPS & Dual/Clone Apps)
1. **Dual/Clone Apps Support (`LauncherApps` & `UserManager`):**
   - Query apps via `LauncherApps` and `UserManager.userProfiles`.
   - `uniqueKey = "${info.componentName.packageName}_${info.componentName.className}_${userHandle.hashCode()}"`.
   - Detect clone apps when `userHandle != android.os.Process.myUserHandle()`.
   - Launch apps using `launcherApps.startMainActivity(info.componentName, userHandle, null, null)`.
2. **Pre-decoded Bitmaps (`Dispatchers.Default`):**
   - Convert `info.getBadgedIcon(0)` to `ImageBitmap` (compact `96x96` px) once in background thread during repository loading.
   - Render `app.iconBitmap` directly in UI with zero runtime memory allocations or bitmap decoding during scroll.
3. **Isolated Clock/Date Component:**
   - Time/Date state managed locally inside `@Composable fun HeaderClockDate()` using `LaunchedEffect(Unit)` updating every 10 seconds.
   - ViewModel state is NOT touched every second to eliminate unnecessary UI recompositions during scroll gestures.
4. **Zero-Latency Wave Scrubber & Sticky Selection:**
   - Single `pointerInput(Unit)` using `awaitEachGesture` to avoid gesture detector conflicts and touch-slop latency.
   - GPU-accelerated `Modifier.graphicsLayer` transforms (`translationX`, `scaleX`, `scaleY`).
   - Sticky letter selection: when finger lifts (`touchY = -1f`), `selectedLetter` remains active until cleared explicitly (by tapping `☆`, single-tapping background, pressing Back button, or launching an app).
5. **Ergonomic Background Gestures & Navigation:**
   - Single tap on empty space: `OnClearLetterFilter`.
   - Double tap on empty space: `OnSwitchLauncherRequested`.
   - Long press on empty space: `SetSettingsVisible(true)`.
   - System Back button clears active letter filter if filtering, or closes settings if open.
