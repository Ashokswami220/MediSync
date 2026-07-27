package com.example.medisync

import android.graphics.Color.TRANSPARENT
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle.Companion.auto
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.medisync.data.SettingsManager
import com.example.medisync.ui.components.CustomToast
import com.example.medisync.ui.navigation.NavApp
import com.example.medisync.ui.navigation.Routes
import com.example.medisync.ui.theme.LocalAppearance
import com.example.medisync.ui.theme.MediSyncTheme
import com.example.medisync.utils.GlobalToastManager
import com.example.medisync.utils.HapticHelper

import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {
    private val settingsManager: SettingsManager by inject()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        HapticHelper.init()

        setContent {
            val storedAppearance by settingsManager.appearanceFlow.collectAsState(initial = "Light")
            val appearanceState = remember(storedAppearance) { mutableStateOf(storedAppearance) }

            LaunchedEffect(appearanceState.value) {
                if (appearanceState.value != storedAppearance) {
                    settingsManager.setAppearanceTheme(appearanceState.value)
                }
            }

            val darkTheme = when (appearanceState.value) {
                "Light" -> false
                "Dark" -> true
                else -> isSystemInDarkTheme()
            }

            DisposableEffect(darkTheme) {
                enableEdgeToEdge(
                    statusBarStyle = auto(
                        TRANSPARENT,
                        TRANSPARENT,
                    ) { darkTheme },
                    navigationBarStyle = auto(
                        TRANSPARENT,
                        TRANSPARENT,
                    ) { darkTheme }
                )
                onDispose {}
            }

            val onboardingCompleted by settingsManager.onboardingCompletedFlow.collectAsState(
                initial = null
            )

            if (onboardingCompleted == null) {
                // Wait for the flow to emit the first value
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black)
                )
                return@setContent
            }

            val startDest = if (onboardingCompleted == true) {
                Routes.MAIN_TABS
            } else {
                Routes.CAROUSEL
            }

            CompositionLocalProvider(LocalAppearance provides appearanceState) {
                MediSyncTheme(darkTheme = darkTheme) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        NavApp(startDestination = startDest)

                        val toastState by GlobalToastManager.toastState.collectAsState()
                        CustomToast(
                            message = toastState.message,
                            isVisible = toastState.isVisible,
                            icon = toastState.icon,
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 105.dp),
                            onDismiss = { GlobalToastManager.dismissToast() }
                        )
                    }
                }
            }
        }
    }
}