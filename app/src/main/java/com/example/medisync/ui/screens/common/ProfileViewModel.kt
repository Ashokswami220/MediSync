package com.example.medisync.ui.screens.common

import kotlinx.coroutines.Job
import kotlinx.coroutines.CancellationException

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medisync.model.UserProfile
import com.example.medisync.repo.AuthRepository
import com.example.medisync.repo.UserRepository
import com.example.medisync.repo.DocumentRepository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
    private val userRepo: UserRepository,
    private val documentRepo: DocumentRepository
) : ViewModel() {

    private val _profileState = MutableStateFlow<ProfileState>(ProfileState.Loading)
    val profileState: StateFlow<ProfileState> = _profileState.asStateFlow()

    private val _updateState = MutableStateFlow<ProfileUpdateState>(ProfileUpdateState.Idle)
    val updateState: StateFlow<ProfileUpdateState> = _updateState.asStateFlow()

    init {
        loadProfile()
    }

    private var profileJob: Job? = null

    fun loadProfile() {
        if (_profileState.value !is ProfileState.Success) {
            _profileState.value = ProfileState.Loading
        }
        profileJob?.cancel()
        profileJob = viewModelScope.launch {
            try {
                val currentUser = authRepo.getCurrentUserSync()
                if (currentUser != null) {
                    userRepo.getUserProfile(currentUser.uid).collect { profile ->
                        if (profile != null) {
                            _profileState.value = ProfileState.Success(profile)
                        } else {
                            // If null is emitted (e.g., from empty cache), keep state as Loading 
                            // until server responds, or fallback if it persists.
                            if (_profileState.value !is ProfileState.Success) {
                                _profileState.value = ProfileState.Loading
                            }
                        }
                    }
                } else {
                    _profileState.value = ProfileState.Error("User not logged in.")
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
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
                if (e is CancellationException) throw e
                _updateState.value =
                    ProfileUpdateState.Error(e.message ?: "Failed to update profile")
            }
        }
    }

    fun resetUpdateState() {
        _updateState.value = ProfileUpdateState.Idle
    }

    fun deleteAccount(onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                val user = authRepo.getCurrentUserSync()
                val uid = user?.uid
                if (uid != null) {
                    documentRepo.clearDataForUsers(listOf(uid))
                    userRepo.deleteUsers(listOf(uid))
                }
                authRepo.signOut()
                onResult(true, "Account deleted successfully")
            } catch (e: Exception) {
                onResult(false, e.message ?: "Failed to wipe account data")
            }
        }
    }

    fun deleteData(onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                val uid = authRepo.getCurrentUserSync()?.uid
                if (uid != null) {
                    documentRepo.clearDataForUsers(listOf(uid))
                    userRepo.updateUserProfile(uid, mapOf("members" to emptyList<String>()))
                }
                onResult(true, "Data cleared successfully")
            } catch (e: Exception) {
                onResult(false, e.message ?: "Failed to clear data")
            }
        }
    }
}
