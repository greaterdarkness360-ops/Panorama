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
