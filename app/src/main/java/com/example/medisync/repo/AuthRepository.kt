package com.example.medisync.repo

import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    /** Flow of the current authenticated user. Null if not logged in. */
    val currentUser: Flow<FirebaseUser?>

    /** Returns the current user synchronously. */
    fun getCurrentUserSync(): FirebaseUser?

    /** Returns the current user's UID, or null if not logged in. */
    fun getCurrentUserUid(): String?

    /** Signs in with the given Firebase credential. Returns the authenticated user. */
    suspend fun signInWithCredential(credential: AuthCredential): FirebaseUser?

    /** Logs the user out. */
    suspend fun signOut()

    /** Deletes the currently authenticated user from Firebase Auth. */
    suspend fun deleteCurrentUser(): Result<Unit>
}
