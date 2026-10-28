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
