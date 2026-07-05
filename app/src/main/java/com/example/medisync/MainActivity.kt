package com.example.medisync

import android.graphics.Color.TRANSPARENT
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle.Companion.auto
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.example.medisync.data.SettingsManager
import com.example.medisync.ui.navigation.NavApp
import com.example.medisync.ui.theme.LocalAppearance
import com.example.medisync.ui.theme.MediSyncTheme
import com.example.medisync.utils.HapticHelper

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        HapticHelper.init(this)
        val settingsManager = SettingsManager(this)

        setContent {
            val storedAppearance by settingsManager.appearanceFlow.collectAsState(initial = "System")
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

            CompositionLocalProvider(LocalAppearance provides appearanceState) {
                MediSyncTheme(darkTheme = darkTheme) {
                    NavApp()
                }
            }
        }
    }
}