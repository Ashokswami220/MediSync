package com.example.medisync.ui.screens.common

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medisync.model.UserProfile
import com.example.medisync.repo.AuthRepository
import com.example.medisync.repo.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

sealed class ProfileState {
    object Loading : ProfileState()
    data class Success(val profile: UserProfile) : ProfileState()
    data class Error(val message: String) : ProfileState()
}

sealed class ProfileUpdateState {
    object Idle : ProfileUpdateState()
    object Saving : ProfileUpdateState()
    object Success : ProfileUpdateState()
    data class Error(val message: String) : ProfileUpdateState()
}

class ProfileViewModel(
    private val authRepo: AuthRepository,
    private val userRepo: UserRepository
) : ViewModel() {

    private val _profileState = MutableStateFlow<ProfileState>(ProfileState.Loading)
    val profileState: StateFlow<ProfileState> = _profileState.asStateFlow()

    private val _updateState = MutableStateFlow<ProfileUpdateState>(ProfileUpdateState.Idle)
    val updateState: StateFlow<ProfileUpdateState> = _updateState.asStateFlow()

    init {
        loadProfile()
    }

    fun loadProfile() {
        viewModelScope.launch {
            _profileState.value = ProfileState.Loading
            try {
                val currentUser = authRepo.getCurrentUserSync()
                if (currentUser != null) {
                    val profile = userRepo.getUserProfile(currentUser.uid).first()
                    if (profile != null) {
                        _profileState.value = ProfileState.Success(profile)
                    } else {
                        _profileState.value = ProfileState.Error("Profile not found.")
                    }
                } else {
                    _profileState.value = ProfileState.Error("User not logged in.")
                }
            } catch (e: Exception) {
                _profileState.value = ProfileState.Error(e.message ?: "Failed to load profile")
            }
        }
    }

    fun updateProfile(firstName: String, lastName: String, phoneNumber: String) {
        viewModelScope.launch {
            _updateState.value = ProfileUpdateState.Saving
            try {
                val currentUser = authRepo.getCurrentUserSync()
                if (currentUser != null) {
                    val updates = mapOf(
                        "firstName" to firstName,
                        "lastName" to lastName,
                        "phoneNumber" to phoneNumber
                    )
                    val result = userRepo.updateUserProfile(currentUser.uid, updates)
                    if (result.isSuccess) {
                        _updateState.value = ProfileUpdateState.Success
                        loadProfile() // Reload latest
                    } else {
                        _updateState.value = ProfileUpdateState.Error("Failed to update profile.")
                    }
                } else {
                    _updateState.value = ProfileUpdateState.Error("User not logged in.")
                }
            } catch (e: Exception) {
                _updateState.value = ProfileUpdateState.Error(e.message ?: "Failed to update profile")
            }
        }
    }
    
    fun resetUpdateState() {
        _updateState.value = ProfileUpdateState.Idle
    }
}
