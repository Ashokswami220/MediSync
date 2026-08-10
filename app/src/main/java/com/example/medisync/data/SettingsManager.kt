package com.example.medisync.data

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

private val Context.dataStore by preferencesDataStore(name = "settings")

class SettingsManager(private val context: Context) {
    companion object {
        val HAPTICS_KEY = booleanPreferencesKey("haptics_enabled")
        val APPEARANCE_KEY = stringPreferencesKey("appearance_theme")
        val ONBOARDING_COMPLETED_KEY = booleanPreferencesKey("onboarding_completed_v2")
        val USER_ROLE_KEY = stringPreferencesKey("user_role")
        val LANGUAGE_KEY = stringPreferencesKey("app_language")
    }

    val hapticsFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[HAPTICS_KEY] ?: true // default true
    }

    val appearanceFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[APPEARANCE_KEY] ?: "Light"
    }

    val onboardingCompletedFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[ONBOARDING_COMPLETED_KEY] ?: false
    }

    val userRoleFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[USER_ROLE_KEY] ?: "USER"
    }

    val languageFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[LANGUAGE_KEY] ?: "English"
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

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[ONBOARDING_COMPLETED_KEY] = completed
        }
    }

    suspend fun setUserRole(role: String) {
        context.dataStore.edit { preferences ->
            preferences[USER_ROLE_KEY] = role
        }
    }

    suspend fun setLanguage(language: String) {
        context.dataStore.edit { preferences ->
            preferences[LANGUAGE_KEY] = language
        }
        withContext(Dispatchers.Main) {
            val tag = if (language == "Hindi") "hi" else "en"
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(tag))
        }
    }
}
