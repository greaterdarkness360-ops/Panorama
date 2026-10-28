package com.natanael.panorama.ui

import android.app.Application
import android.content.Intent
import android.provider.Settings
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.natanael.panorama.data.AppRepository
import com.natanael.panorama.data.SettingsRepository
import com.natanael.panorama.model.AppItem
import com.natanael.panorama.model.PanoramaUiEvent
import com.natanael.panorama.model.PanoramaUiState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PanoramaViewModel(application: Application) : AndroidViewModel(application) {

    private val appRepository = AppRepository(application)
    private val settingsRepository = SettingsRepository(application)

    private val _uiState = MutableStateFlow(PanoramaUiState())
    val uiState: StateFlow<PanoramaUiState> = _uiState.asStateFlow()

    init {
        observeSettingsAndApps()
        startClockTicker()
    }

    private fun observeSettingsAndApps() {
        viewModelScope.launch {
            combine(
                settingsRepository.hiddenPackagesFlow,
                settingsRepository.favoritePackagesFlow
            ) { hiddenSet, favoriteSet ->
                Pair(hiddenSet, favoriteSet)
            }.collect { (hiddenSet, favoriteSet) ->
                _uiState.update { currentState ->
                    currentState.copy(
                        hiddenPackageNames = hiddenSet,
                        favoritePackageNames = favoriteSet
                    )
                }
                loadApps(hiddenSet, favoriteSet)
            }
        }
    }

    fun loadApps(
        hiddenSet: Set<String> = _uiState.value.hiddenPackageNames,
        favoriteSet: Set<String> = _uiState.value.favoritePackageNames
    ) {
        viewModelScope.launch {
            val apps = appRepository.getInstalledApps(hiddenSet, favoriteSet)
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

    private fun startClockTicker() {
        viewModelScope.launch {
            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            val dateFormat = SimpleDateFormat("EEEE, d MMMM", Locale.getDefault())
            while (true) {
                val now = Date()
                val timeStr = timeFormat.format(now)
                val dateStr = dateFormat.format(now)
                _uiState.update {
                    it.copy(
                        currentTimeString = timeStr,
                        currentDateString = dateStr
                    )
                }
                delay(1000L)
            }
        }
    }

    fun onEvent(event: PanoramaUiEvent) {
        when (event) {
            is PanoramaUiEvent.OnLetterScrub -> {
                val letter = event.letter
                _uiState.update { currentState ->
                    val filtered = computeFilteredApps(
                        allApps = currentState.allApps,
                        letter = letter
                    )
                    currentState.copy(
                        selectedLetter = letter,
                        isScrubbing = (letter != null),
                        filteredApps = filtered
                    )
                }
            }

            is PanoramaUiEvent.OnAppClicked -> {
                launchApp(event.app)
            }

            is PanoramaUiEvent.OnToggleAppVisibility -> {
                viewModelScope.launch {
                    settingsRepository.toggleAppVisibility(event.packageName, event.hide)
                }
            }

            is PanoramaUiEvent.OnToggleFavorite -> {
                viewModelScope.launch {
                    settingsRepository.toggleFavorite(event.packageName, event.favorite)
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
        if (letter == null) return emptyList()
        val visibleApps = allApps.filter { !it.isHidden }
        return when (letter) {
            '☆' -> visibleApps.filter { it.isFavorite }
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
            val intent = getApplication<Application>().packageManager.getLaunchIntentForPackage(app.packageName)
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                getApplication<Application>().startActivity(intent)
            } else {
                val explicitIntent = Intent(Intent.ACTION_MAIN).apply {
                    addCategory(Intent.CATEGORY_LAUNCHER)
                    setClassName(app.packageName, app.activityName)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                getApplication<Application>().startActivity(explicitIntent)
            }
        } catch (e: Exception) {
            e.printStackTrace()
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
