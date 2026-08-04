package com.example.medisync.ui.screens.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medisync.repo.AuthRepository
import com.example.medisync.repo.DocumentRepository
import com.example.medisync.repo.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class UserAdminModel(
    val uid: String,
    val name: String,
    val lastReportName: String,
    val lastReportTime: String,
    val hasViewed: Boolean,
    val timestamp: Long = 0L,
    val avatarUrl: String = "",
    val phoneNumber: String = "",
    val email: String = ""
)

class UserListViewModel(
    private val userRepository: UserRepository,
    private val documentRepository: DocumentRepository,
    private val authRepo: AuthRepository
) : ViewModel() {

    private val _usersState = MutableStateFlow<List<UserAdminModel>>(emptyList())
    val usersState: StateFlow<List<UserAdminModel>> = _usersState.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private var isFetchingStarted = false

    fun fetchUsers() {
        if (isFetchingStarted) return
        isFetchingStarted = true
        _isLoading.value = true
        viewModelScope.launch {
            val usersFlow = userRepository.getAllUsers()
                .catch { _ ->
                    _isLoading.value = false
                }

            combine(usersFlow, documentRepository.getDocuments(null)) { profiles, docs ->
                // Sort docs by newest
                val sortedDocs = docs.sortedByDescending { it.uploadedAt }

                // Map profiles
                val currentUserId = authRepo.getCurrentUserUid()

                // Collect all UIDs that are claimed via previousUids
                val claimedPlaceholderUids = profiles
                    .flatMap { it.previousUids }
                    .toSet()

                val adminModels = profiles
                    .filter { it.uid != currentUserId }
                    .filter { it.uid !in claimedPlaceholderUids }
                    .map { profile ->
                        val allUids = listOf(profile.uid) + profile.previousUids
                        val userDocs = sortedDocs.filter { it.linkedUserUid in allUids }
                        val latestDoc = userDocs.firstOrNull()

                        val fullName = "${profile.firstName} ${profile.lastName}".trim()

                        UserAdminModel(
                            uid = profile.uid,
                            name = fullName.ifEmpty { "Unknown User" },
                            lastReportName = latestDoc?.documentName ?: "No Reports Yet",
                            lastReportTime = if (latestDoc != null) {
                                val formatter =
                                    SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                                formatter.format(Date(latestDoc.uploadedAt))
                            } else "",
                            hasViewed = false, // Default to false
                            timestamp = latestDoc?.uploadedAt ?: profile.accountCreatedTime,
                            avatarUrl = profile.avatarUrl,
                            phoneNumber = profile.phoneNumber,
                            email = profile.email
                        )
                    }

                // Sort by default: Newest first based on latest report
                adminModels.sortedByDescending { it.timestamp }
            }.catch { _ ->
                _isLoading.value = false
                _usersState.value = emptyList()
            }
                .collect { adminModels ->
                    _usersState.value = adminModels
                    _isLoading.value = false
                }
        }
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
                val updates = mapOf(
                    "members" to emptyList<String>(),
                    "bloodType" to "",
                    "bloodPressure" to "",
                    "bloodSugar" to "",
                    "documents" to emptyList<String>()
                )
                uids.forEach { uid ->
                    userRepository.updateUserProfile(uid, updates)
                }
                onResult(true, "Successfully cleared data")
            } else {
                onResult(false, result.exceptionOrNull()?.message ?: "Failed to clear data")
            }
        }
    }

    fun createPlaceholderUser(
        firstName: String,
        lastName: String,
        contactMethod: String, // "phone" or "email"
        contactValue: String,
        onResult: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                // Check if user already exists
                val exists = userRepository.checkUserExists(contactValue)
                if (exists) {
                    onResult(false, "This user already exists")
                    return@launch
                }

                val placeholderUid = java.util.UUID.randomUUID()
                    .toString()
                val profile = com.example.medisync.model.UserProfile(
                    uid = placeholderUid,
                    firstName = firstName,
                    lastName = lastName,
                    phoneNumber = if (contactMethod == "phone") contactValue else "",
                    email = if (contactMethod == "email") contactValue else "",
                    role = com.example.medisync.model.UserRole.USER,
                    isPlaceholder = true
                )

                val result = userRepository.createUserProfile(profile)
                if (result.isSuccess) {
                    onResult(true, "Placeholder user created")
                } else {
                    onResult(false, result.exceptionOrNull()?.message ?: "Failed to create user")
                }
            } catch (e: Exception) {
                onResult(false, e.message ?: "An error occurred")
            }
        }
    }
}
