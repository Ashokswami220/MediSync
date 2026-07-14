package com.example.medisync.repo

import com.example.medisync.model.UserProfile
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    /** Creates or overwrites a user profile in Firestore. */
    suspend fun createUserProfile(userProfile: UserProfile): Result<Unit>
    
    /** Fetches a user profile by UID and observes it for changes. */
    fun getUserProfile(uid: String): Flow<UserProfile?>
    
    /** Updates specific fields in an existing user profile. */
    suspend fun updateUserProfile(uid: String, updates: Map<String, Any>): Result<Unit>

    /** Fetches all users from local cache while updating from network. */
    fun getAllUsers(): Flow<List<UserProfile>>
}
