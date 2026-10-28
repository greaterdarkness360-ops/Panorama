package com.natanael.panorama

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.natanael.panorama.model.PanoramaUiEvent
import com.natanael.panorama.ui.PanoramaMainScreen
import com.natanael.panorama.ui.PanoramaViewModel
import com.natanael.panorama.ui.components.SettingsSheet

class MainActivity : ComponentActivity() {

    private val viewModel: PanoramaViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        @Suppress("DEPRECATION")
        window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER)

        setContent {
            PanoramaTheme {
                val uiState by viewModel.uiState.collectAsState()

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Transparent
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        PanoramaMainScreen(
                            uiState = uiState,
                            onEvent = viewModel::onEvent
                        )

                        if (uiState.isSettingsOpen) {
                            SettingsSheet(
                                uiState = uiState,
                                onEvent = viewModel::onEvent,
                                onDismiss = {
                                    viewModel.onEvent(PanoramaUiEvent.SetSettingsVisible(false))
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.onEvent(PanoramaUiEvent.OnRefreshApps)
    }
}

@Composable
fun PanoramaTheme(content: @Composable () -> Unit) {
    val darkColors = darkColorScheme(
        primary = Color.White,
        onPrimary = Color.Black,
        surface = Color(0xFF1E1E1E),
        onSurface = Color.White,
        background = Color.Transparent,
        onBackground = Color.White
    )

    MaterialTheme(
        colorScheme = darkColors,
        content = content
    )
}
