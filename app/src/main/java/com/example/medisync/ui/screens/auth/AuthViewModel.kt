package com.example.medisync.ui.screens.auth

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medisync.R
import com.example.medisync.model.UserProfile
import com.example.medisync.repo.AuthRepository
import com.example.medisync.repo.UserRepository
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object NeedsInfo : AuthState()
    object Success : AuthState()
    object LoggedOut : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel(
    private val authRepo: AuthRepository,
    private val userRepo: UserRepository,
    private val application: Application
) : ViewModel() {

    private val credentialManager = CredentialManager.create(application)
    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()
    fun checkInitialAuthState() {
        if (_authState.value != AuthState.Idle) return
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val currentUser = authRepo.getCurrentUserSync()
                if (currentUser != null) {
                    checkIfUserNeedsInfo(currentUser)
                } else {
                    _authState.value = AuthState.LoggedOut
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Failed to check auth state")
            }
        }
    }

    fun resetState() {
        _authState.value = AuthState.Idle
    }

    fun signOut() {
        viewModelScope.launch {
            authRepo.signOut()
            _authState.value = AuthState.LoggedOut
        }
    }

    fun signInWithGoogle(context: Context) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val googleIdOption: GetGoogleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(application.getString(R.string.default_web_client_id))
                    .setAutoSelectEnabled(false)
                    .build()

                val request: GetCredentialRequest = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                val result = credentialManager.getCredential(
                    request = request,
                    context = context,
                )

                val credential = result.credential
                if (credential is CustomCredential &&
                    credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                ) {
                    val googleIdTokenCredential =
                        GoogleIdTokenCredential.createFrom(credential.data)
                    val idToken = googleIdTokenCredential.idToken

                    // Auth with Firebase
                    val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                    val user = authRepo.signInWithCredential(firebaseCredential)
                    if (user != null) {
                        checkIfUserNeedsInfo(user)
                    } else {
                        _authState.value = AuthState.Error("Firebase Auth failed.")
                    }
                } else {
                    _authState.value = AuthState.Error("Unexpected credential type.")
                }
            } catch (e: GetCredentialCancellationException) {
                Log.e("AuthViewModel", "User cancelled Google Sign In", e)
                _authState.value = AuthState.Error("You cancelled the selector")
            } catch (e: NoCredentialException) {
                Log.e("AuthViewModel", "No credentials available", e)
                _authState.value = AuthState.Error("Credentials not available, please try again.")
            } catch (e: GetCredentialException) {
                Log.e("AuthViewModel", "Credential Manager Error", e)
                _authState.value = AuthState.Error(e.localizedMessage ?: "Google Sign In Failed")
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Google Sign In Failed", e)
                _authState.value = AuthState.Error(e.localizedMessage ?: "Unknown error occurred")
            }
        }
    }

    private suspend fun checkIfUserNeedsInfo(user: FirebaseUser) {
        try {
            val result = userRepo.getUserProfileSync(user.uid)
            val profile = result.getOrNull()
            if (profile == null || profile.firstName.isEmpty() || profile.phoneNumber.isEmpty()) {
                _authState.value = AuthState.NeedsInfo
            } else {
                val updates = mutableMapOf<String, Any>()
                if (profile.email.isEmpty() && !user.email.isNullOrEmpty()) {
                    updates["email"] = user.email!!
                }
                if (profile.avatarUrl.isEmpty() && user.photoUrl != null) {
                    updates["avatarUrl"] = user.photoUrl.toString()
                }
                if (updates.isNotEmpty()) {
                    userRepo.updateUserProfile(user.uid, updates)
                }
                _authState.value = AuthState.Success
            }
        } catch (e: Exception) {
            _authState.value = AuthState.Error(e.message ?: "Failed to read user data")
        }
    }

    fun completeProfile(firstName: String, lastName: String, phoneNumber: String) {
        if (firstName.trim().length < 2) {
            _authState.value = AuthState.Error("First name must be at least 2 characters")
            return
        }
        if (phoneNumber.trim().length < 10) {
            _authState.value = AuthState.Error("Please enter a valid phone number (min 10 digits)")
            return
        }

        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val currentUser = authRepo.getCurrentUserSync()
                if (currentUser != null) {
                    // Try to fetch existing to preserve roles, Blood type, etc.
                    val existingResult = userRepo.getUserProfileSync(currentUser.uid)
                    val existingProfile = existingResult.getOrNull()
                    val newProfile = existingProfile?.copy(
                        firstName = firstName,
                        lastName = lastName,
                        phoneNumber = phoneNumber,
                        email = currentUser.email ?: existingProfile.email,
                        avatarUrl = currentUser.photoUrl?.toString() ?: existingProfile.avatarUrl
                    ) ?: UserProfile(
                        uid = currentUser.uid,
                        firstName = firstName,
                        lastName = lastName,
                        phoneNumber = phoneNumber,
                        email = currentUser.email ?: "",
                        avatarUrl = currentUser.photoUrl?.toString() ?: ""
                    )

                    val result = userRepo.createUserProfile(newProfile)
                    if (result.isSuccess) {
                        _authState.value = AuthState.Success
                    } else {
                        _authState.value = AuthState.Error("Failed to save profile.")
                    }
                } else {
                    _authState.value = AuthState.Error("No authenticated user found.")
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Failed to complete profile")
            }
        }
    }
}