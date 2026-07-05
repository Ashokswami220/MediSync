package com.example.medisync.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "settings")

class SettingsManager(private val context: Context) {
    companion object {
        val HAPTICS_KEY = booleanPreferencesKey("haptics_enabled")
        val APPEARANCE_KEY = stringPreferencesKey("appearance_theme") // "System", "Light", "Dark"
    }

    val hapticsFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[HAPTICS_KEY] ?: true // default true
    }

    val appearanceFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[APPEARANCE_KEY] ?: "System"
    }

    suspend fun setHapticsEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[HAPTICS_KEY] = enabled
        }
    }

    suspend fun setAppearanceTheme(theme: String) {
        context.dataStore.edit { preferences ->
            preferences[APPEARANCE_KEY] = theme
        }
    }
}
