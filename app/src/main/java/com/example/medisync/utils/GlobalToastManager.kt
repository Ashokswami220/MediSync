package com.example.medisync.utils

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

object GlobalToastManager {
    private val _toastState = MutableStateFlow(ToastState())
    val toastState: StateFlow<ToastState> = _toastState.asStateFlow()

    fun showToast(
        message: String, icon: ImageVector = Icons.Default.Info, durationMillis: Long = 3000L
    ) {
        _toastState.value = ToastState(isVisible = true, message = message, icon = icon)
        CoroutineScope(Dispatchers.Main).launch {
            delay(durationMillis.milliseconds)
            dismissToast()
        }
    }

    fun dismissToast() {
        _toastState.value = _toastState.value.copy(isVisible = false)
    }
}

data class ToastState(
    val isVisible: Boolean = false,
    val message: String = "",
    val icon: ImageVector = Icons.Default.Info
)
