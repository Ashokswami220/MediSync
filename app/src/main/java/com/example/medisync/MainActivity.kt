package com.example.medisync

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.example.medisync.ui.navigation.NavApp
import com.example.medisync.ui.theme.LocalAppearance
import com.example.medisync.ui.theme.MediSyncTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val appearanceState = remember { mutableStateOf("System") }
            val darkTheme = when (appearanceState.value) {
                "Light" -> false
                "Dark" -> true
                else -> isSystemInDarkTheme()
            }
            CompositionLocalProvider(LocalAppearance provides appearanceState) {
                MediSyncTheme(darkTheme = darkTheme) {
                    NavApp()
                }
            }
        }
    }
}