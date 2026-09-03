package com.example.ui

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.ErrorView
import com.example.ui.components.ExitConfirmationDialog
import com.example.ui.components.WebViewContainer

@Composable
fun WebBrowserScreen(
    viewModel: WebViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val webCommand by viewModel.webCommand.collectAsStateWithLifecycle()

    var showExitDialog by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .systemBarsPadding()
    ) {
        // Main Fullscreen WebView
        WebViewContainer(
            viewModel = viewModel,
            command = webCommand,
            modifier = Modifier.fillMaxSize(),
            onExitRequested = {
                showExitDialog = true
            }
        )

        // Subtle loading indicator at the very top of the screen
        AnimatedVisibility(
            visible = uiState.isLoading,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            LinearProgressIndicator(
                progress = { uiState.loadProgress / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.5.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = Color.Transparent
            )
        }

        // Error fallback overlay with retry if connection fails
        if (uiState.errorMessage != null) {
            ErrorView(
                errorMessage = uiState.errorMessage ?: "",
                onRetry = { viewModel.reload() },
                onOpenSaved = { viewModel.openSavedSheet() }
            )
        }

        // Exit Popup Floating Window with glowing animation
        if (showExitDialog) {
            ExitConfirmationDialog(
                onConfirmExit = {
                    showExitDialog = false
                    (context as? Activity)?.finish()
                },
                onDismiss = {
                    showExitDialog = false
                }
            )
        }
    }
}


