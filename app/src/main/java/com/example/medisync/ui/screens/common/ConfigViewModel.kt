package com.example.medisync.ui.screens.common

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medisync.model.AppConfig
import com.example.medisync.repo.ConfigRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ConfigViewModel(
    private val configRepository: ConfigRepository
) : ViewModel() {

    val appConfig: StateFlow<AppConfig> = configRepository.getConfig()
        .catch { emit(AppConfig()) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AppConfig()
        )

    fun updateConfig(config: AppConfig) {
        viewModelScope.launch {
            configRepository.updateConfig(config)
        }
    }
}
