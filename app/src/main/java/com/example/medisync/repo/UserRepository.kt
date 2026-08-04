package com.example.medisync.repo

import com.example.medisync.model.UserProfile
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    /** Creates or overwrites a user profile in Firestore. */
    suspend fun createUserProfile(userProfile: UserProfile): Result<Unit>

    /** Fetches a user profile by UID and observes it for changes. */
    fun getUserProfile(uid: String): Flow<UserProfile?>

    /** Fetches a user profile by UID synchronously from the server. */
    suspend fun getUserProfileSync(uid: String): Result<UserProfile?>

    /** Updates specific fields in an existing user profile. */
    suspend fun updateUserProfile(uid: String, updates: Map<String, Any>): Result<Unit>

    /** Fetches all users from local cache while updating from network. */
    fun getAllUsers(): Flow<List<UserProfile>>

    /** Deletes multiple users from Firestore and local database. */
    suspend fun deleteUsers(uids: List<String>): Result<Unit>
    
    /** Checks if a user profile exists with the given phone number or email. */
    suspend fun checkUserExists(phoneOrEmail: String): Boolean
    
    /** Finds a placeholder user by phone number or email. */
    suspend fun findPlaceholder(phoneOrEmail: String): UserProfile?

    /** Claims a placeholder: transfers all documents to the real user UID and updates the linkedUser name. */
    suspend fun claimPlaceholder(placeholderUid: String, realUserUid: String, realUserName: String): Result<Unit>
}
