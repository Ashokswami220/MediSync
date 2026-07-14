package com.example.medisync.ui.screens.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medisync.model.UserProfile
import com.example.medisync.repo.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class UserListViewModel(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _usersState = MutableStateFlow<List<UserAdminModel>>(emptyList())
    val usersState: StateFlow<List<UserAdminModel>> = _usersState.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        fetchUsers()
    }

    private fun fetchUsers() {
        viewModelScope.launch {
            userRepository.getAllUsers()
                .catch { e ->
                    // Handle error if necessary
                    _isLoading.value = false
                }
                .collect { profiles ->
                    // Map real UserProfile data to UserAdminModel for the UI
                    val adminModels = profiles.map { profile ->
                        val fullName = "${profile.firstName} ${profile.lastName}".trim()
                        val displayTime = formatTime(profile.accountCreatedTime)
                        
                        UserAdminModel(
                            name = fullName.ifEmpty { "Unknown User" },
                            lastReportName = if (profile.documents.isNotEmpty()) "Uploaded ${profile.documents.size} Docs" else "No Reports Yet",
                            lastReportTime = displayTime,
                            hasViewed = false // Default to false for now, unless tracked in profile
                        )
                    }
                    _usersState.value = adminModels
                    _isLoading.value = false
                }
        }
    }

    private fun formatTime(millis: Long): String {
        if (millis <= 0) return "Unknown"
        val formatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        return formatter.format(Date(millis))
    }
}
