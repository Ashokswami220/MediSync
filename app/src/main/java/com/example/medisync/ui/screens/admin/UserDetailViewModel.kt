package com.example.medisync.ui.screens.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medisync.repo.DocumentRepository
import com.example.medisync.model.DocumentMetadata
import com.example.medisync.model.UserProfile
import com.example.medisync.repo.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class UserDetailViewModel(
    private val userRepository: UserRepository,
    private val documentRepository: DocumentRepository
) : ViewModel() {

    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile.asStateFlow()

    private val _documents = MutableStateFlow<List<DocumentMetadata>>(emptyList())
    val documents: StateFlow<List<DocumentMetadata>> = _documents.asStateFlow()

    fun loadUser(userUid: String) {
        viewModelScope.launch {
            userRepository.getUserProfile(userUid)
                .catch { e -> e.printStackTrace() }
                .collect { profile ->
                    _userProfile.value = profile
                }
        }
        viewModelScope.launch {
            documentRepository.getDocuments(userUid)
                .catch { e -> e.printStackTrace() }
                .collect { docs ->
                    _documents.value = docs
                }
        }
    }

    fun deleteUser(uid: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            documentRepository.clearDataForUsers(listOf(uid))
            val result = userRepository.deleteUsers(listOf(uid))
            if (result.isSuccess) {
                onResult(true, "Successfully deleted user")
            } else {
                onResult(false, result.exceptionOrNull()?.message ?: "Failed to delete user")
            }
        }
    }

    fun clearUserData(uid: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val result = documentRepository.clearDataForUsers(listOf(uid))
            if (result.isSuccess) {
                onResult(true, "Successfully cleared data")
            } else {
                onResult(false, result.exceptionOrNull()?.message ?: "Failed to clear data")
            }
        }
    }

    fun deleteReport(docId: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val result = documentRepository.deleteDocument(docId)
            if (result.isSuccess) {
                onResult(true, "Report deleted successfully")
            } else {
                onResult(false, result.exceptionOrNull()?.message ?: "Failed to delete report")
            }
        }
    }

    fun addMember(uid: String, memberName: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val user = _userProfile.value
            if (user != null) {
                if (user.members.contains(memberName) || user.firstName == memberName) {
                    onResult(false, "Member already exists")
                    return@launch
                }
                val newMembers = user.members + memberName
                val result = userRepository.updateUserProfile(uid, mapOf("members" to newMembers))
                if (result.isSuccess) {
                    _userProfile.value = user.copy(members = newMembers)
                    onResult(true, "Member added successfully")
                } else {
                    onResult(false, "Failed to add member")
                }
            }
        }
    }
}
