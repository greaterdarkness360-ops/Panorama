package com.natanael.panorama.ui

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.natanael.panorama.model.PanoramaUiEvent
import com.natanael.panorama.model.PanoramaUiState
import com.natanael.panorama.ui.components.AlphabetWaveScrubber
import com.natanael.panorama.ui.components.AppListRightAligned
import com.natanael.panorama.ui.components.HeaderClockDate

@Composable
fun PanoramaMainScreen(
    uiState: PanoramaUiState,
    onEvent: (PanoramaUiEvent) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = {
                        onEvent(PanoramaUiEvent.OnSwitchLauncherRequested)
                    },
                    onLongPress = {
                        onEvent(PanoramaUiEvent.SetSettingsVisible(true))
                    }
                )
            }
            .systemBarsPadding()
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            HeaderClockDate(
                currentTimeString = uiState.currentTimeString,
                currentDateString = uiState.currentDateString
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val displayApps = if (uiState.isScrubbing || uiState.selectedLetter != null) {
                    uiState.filteredApps
                } else {
                    uiState.favoriteApps
                }

                AppListRightAligned(
                    apps = displayApps,
                    isScrubbing = uiState.isScrubbing || uiState.selectedLetter != null,
                    selectedLetter = uiState.selectedLetter,
                    onAppClick = { app ->
                        onEvent(PanoramaUiEvent.OnAppClicked(app))
                    },
                    modifier = Modifier.weight(1f)
                )

                AlphabetWaveScrubber(
                    selectedLetter = uiState.selectedLetter,
                    onLetterSelected = { letter ->
                        onEvent(PanoramaUiEvent.OnLetterScrub(letter))
                    }
                )
            }
        }
    }
}
