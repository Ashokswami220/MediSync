package com.example.medisync.ui.screens.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medisync.repo.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

import kotlinx.coroutines.flow.combine
import com.example.medisync.data.repository.DocumentRepository
import com.example.medisync.model.DocumentMetadata
import com.google.firebase.auth.FirebaseAuth

class UserListViewModel(
    private val userRepository: UserRepository,
    private val documentRepository: DocumentRepository
) : ViewModel() {

    private val _usersState = MutableStateFlow<List<UserAdminModel>>(emptyList())
    val usersState: StateFlow<List<UserAdminModel>> = _usersState.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        FirebaseAuth.getInstance().addAuthStateListener { auth ->
            if (auth.currentUser != null) {
                fetchUsers()
            } else {
                _usersState.value = emptyList()
            }
        }
    }

    private fun fetchUsers() {
        viewModelScope.launch {
            val usersFlow = userRepository.getAllUsers()
                .catch { _ ->
                    _isLoading.value = false
                }
            
            combine(usersFlow, documentRepository.getDocuments(null)) { profiles, docs ->
                // Sort docs by newest
                val sortedDocs = docs.sortedByDescending { it.uploadedAt }
                
                // Map profiles
                val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
                val adminModels = profiles
                    .filter { it.uid != currentUserId }
                    .map { profile ->
                    val userDocs = sortedDocs.filter { it.linkedUserUid == profile.uid }
                    val latestDoc = userDocs.firstOrNull()
                    
                    val fullName = "${profile.firstName} ${profile.lastName}".trim()
                    
                    val lastReportName = latestDoc?.documentName ?: "No Reports Yet"
                    val lastReportTimeMs = latestDoc?.uploadedAt ?: profile.accountCreatedTime
                    val displayTime = formatTime(lastReportTimeMs)
                    
                    UserAdminModel(
                        uid = profile.uid,
                        name = fullName.ifEmpty { "Unknown User" },
                        lastReportName = lastReportName,
                        lastReportTime = displayTime,
                        hasViewed = false, // Default to false
                        timestamp = lastReportTimeMs // Store timestamp for sorting
                    )
                }
                
                // Sort by default: Newest first based on latest report
                adminModels.sortedByDescending { it.timestamp }
            }.catch { _ ->
                _isLoading.value = false
                _usersState.value = emptyList()
            }.collect { adminModels ->
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

    fun deleteSelectedUsers(uids: List<String>, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            // Also clear data when deleting users
            documentRepository.clearDataForUsers(uids)
            val result = userRepository.deleteUsers(uids)
            _isLoading.value = false
            if (result.isSuccess) {
                onResult(true, "Successfully deleted users")
            } else {
                onResult(false, result.exceptionOrNull()?.message ?: "Failed to delete users")
            }
        }
    }

    fun clearDataForSelectedUsers(uids: List<String>, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = documentRepository.clearDataForUsers(uids)
            _isLoading.value = false
            if (result.isSuccess) {
                onResult(true, "Successfully cleared data")
            } else {
                onResult(false, result.exceptionOrNull()?.message ?: "Failed to clear data")
            }
        }
    }
}
