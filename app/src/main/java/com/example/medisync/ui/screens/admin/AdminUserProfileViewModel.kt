package com.example.medisync.ui.screens.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medisync.model.MemberVitals
import com.example.medisync.model.UserProfile
import com.example.medisync.repo.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class AdminUserProfileViewModel(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile.asStateFlow()

    fun loadUser(userUid: String) {
        viewModelScope.launch {
            userRepository.getUserProfile(userUid)
                .catch { e -> e.printStackTrace() }
                .collect { profile ->
                    _userProfile.value = profile
                }
        }
    }

    fun updateUserField(
        uid: String, fieldName: String, value: Any, onResult: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            val result = userRepository.updateUserProfile(uid, mapOf(fieldName to value))
            if (result.isSuccess) {
                onResult(true, "Updated successfully")
            } else {
                onResult(false, result.exceptionOrNull()?.message ?: "Failed to update")
            }
        }
    }

    fun updateMemberVital(
        uid: String, memberName: String, field: String, value: String,
        onResult: (Boolean, String) -> Unit
    ) {
        val currentProfile = _userProfile.value ?: return
        val currentVitals = currentProfile.memberVitals[memberName] ?: MemberVitals()

        val updatedVitals = when (field) {
            "bloodType" -> currentVitals.copy(
                bloodType = value, bloodTypeLastUpdated = System.currentTimeMillis()
            )

            "bloodPressure" -> currentVitals.copy(
                bloodPressure = value, bloodPressureLastUpdated = System.currentTimeMillis()
            )

            "bloodSugar" -> currentVitals.copy(
                bloodSugar = value, bloodSugarLastUpdated = System.currentTimeMillis()
            )

            else -> currentVitals
        }

        val newMap = currentProfile.memberVitals.toMutableMap()
        newMap[memberName] = updatedVitals

        updateUserField(uid, "memberVitals", newMap, onResult)
    }

    fun updateUserFields(
        uid: String, updates: Map<String, Any>, onResult: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            val result = userRepository.updateUserProfile(uid, updates)
            if (result.isSuccess) {
                onResult(true, "Updated successfully")
            } else {
                onResult(false, result.exceptionOrNull()?.message ?: "Failed to update")
            }
        }
    }
}
