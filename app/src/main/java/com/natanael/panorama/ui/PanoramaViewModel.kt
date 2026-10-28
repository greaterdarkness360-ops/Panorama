package com.natanael.panorama.ui

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.pm.LauncherApps
import android.provider.Settings
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.natanael.panorama.data.AppRepository
import com.natanael.panorama.data.SettingsRepository
import com.natanael.panorama.model.AppItem
import com.natanael.panorama.model.PanoramaUiEvent
import com.natanael.panorama.model.PanoramaUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PanoramaViewModel(application: Application) : AndroidViewModel(application) {

    private val appRepository = AppRepository(application)
    private val settingsRepository = SettingsRepository(application)
    private val launcherApps = application.getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps

    private val _uiState = MutableStateFlow(PanoramaUiState())
    val uiState: StateFlow<PanoramaUiState> = _uiState.asStateFlow()

    init {
        observeSettingsAndApps()
    }

    private fun observeSettingsAndApps() {
        viewModelScope.launch {
            combine(
                settingsRepository.hiddenKeysFlow,
                settingsRepository.favoriteKeysFlow
            ) { hiddenKeys, favoriteKeys ->
                Pair(hiddenKeys, favoriteKeys)
            }.collect { (hiddenKeys, favoriteKeys) ->
                _uiState.update { currentState ->
                    currentState.copy(
                        hiddenKeys = hiddenKeys,
                        favoriteKeys = favoriteKeys
                    )
                }
                loadApps(hiddenKeys, favoriteKeys)
            }
        }
    }

    fun loadApps(
        hiddenKeys: Set<String> = _uiState.value.hiddenKeys,
        favoriteKeys: Set<String> = _uiState.value.favoriteKeys
    ) {
        viewModelScope.launch {
            val apps = appRepository.getInstalledApps(hiddenKeys, favoriteKeys)
            val favorites = apps.filter { !it.isHidden && it.isFavorite }
            _uiState.update { currentState ->
                val updatedFiltered = computeFilteredApps(
                    allApps = apps,
                    letter = currentState.selectedLetter
                )
                currentState.copy(
                    allApps = apps,
                    favoriteApps = favorites,
                    filteredApps = updatedFiltered
                )
            }
        }
    }

    fun onEvent(event: PanoramaUiEvent) {
        when (event) {
            is PanoramaUiEvent.OnLetterSelected -> {
                val letter = event.letter
                _uiState.update { currentState ->
                    val filtered = computeFilteredApps(
                        allApps = currentState.allApps,
                        letter = letter
                    )
                    currentState.copy(
                        selectedLetter = letter,
                        filteredApps = filtered
                    )
                }
            }

            is PanoramaUiEvent.OnClearLetterFilter -> {
                _uiState.update { currentState ->
                    currentState.copy(
                        selectedLetter = null,
                        filteredApps = emptyList()
                    )
                }
            }

            is PanoramaUiEvent.OnAppClicked -> {
                launchApp(event.app)
                _uiState.update { currentState ->
                    currentState.copy(
                        selectedLetter = null,
                        filteredApps = emptyList()
                    )
                }
            }

            is PanoramaUiEvent.OnToggleAppVisibility -> {
                viewModelScope.launch {
                    settingsRepository.toggleAppVisibility(event.uniqueKey, event.hide)
                }
            }

            is PanoramaUiEvent.OnToggleFavorite -> {
                viewModelScope.launch {
                    settingsRepository.toggleFavorite(event.uniqueKey, event.favorite)
                }
            }

            is PanoramaUiEvent.SetSettingsVisible -> {
                _uiState.update { it.copy(isSettingsOpen = event.visible) }
            }

            is PanoramaUiEvent.OnSwitchLauncherRequested -> {
                openDefaultLauncherSettings()
            }

            is PanoramaUiEvent.OnRefreshApps -> {
                loadApps()
            }
        }
    }

    private fun computeFilteredApps(
        allApps: List<AppItem>,
        letter: Char?
    ): List<AppItem> {
        if (letter == null || letter == '☆') return emptyList()
        val visibleApps = allApps.filter { !it.isHidden }
        return when (letter) {
            '#' -> visibleApps.filter { app ->
                val firstChar = app.label.firstOrNull() ?: ' '
                !firstChar.isLetter()
            }
            else -> visibleApps.filter { app ->
                app.label.startsWith(letter, ignoreCase = true)
            }
        }
    }

    private fun launchApp(app: AppItem) {
        try {
            launcherApps.startMainActivity(
                app.componentName,
                app.userHandle,
                null,
                null
            )
        } catch (e: Exception) {
            e.printStackTrace()
            try {
                val intent = getApplication<Application>().packageManager.getLaunchIntentForPackage(app.packageName)
                if (intent != null) {
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    getApplication<Application>().startActivity(intent)
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
    }

    private fun openDefaultLauncherSettings() {
        val context = getApplication<Application>()
        try {
            val intent = Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            try {
                val homeIntent = Intent(Intent.ACTION_MAIN).apply {
                    addCategory(Intent.CATEGORY_HOME)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                val chooser = Intent.createChooser(homeIntent, "Select Launcher").apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(chooser)
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
    }
}
