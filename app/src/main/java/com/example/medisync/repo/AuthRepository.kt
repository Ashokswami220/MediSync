package com.example.medisync.repo

import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    /** Flow of the current authenticated user. Null if not logged in. */
    val currentUser: Flow<FirebaseUser?>
    
    /** Returns the current user synchronously. */
    fun getCurrentUserSync(): FirebaseUser?
    
    /** Logs the user out. */
    suspend fun signOut()
}
