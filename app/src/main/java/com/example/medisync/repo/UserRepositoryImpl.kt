package com.example.medisync.repo

import com.example.medisync.model.UserProfile
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import com.example.medisync.data.local.room.UserDao
import com.example.medisync.data.local.room.UserEntity
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class UserRepositoryImpl(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val userDao: UserDao
) : UserRepository {

    private val usersCollection = firestore.collection("users")

    override suspend fun createUserProfile(userProfile: UserProfile): Result<Unit> {
        return try {
            usersCollection.document(userProfile.uid).set(userProfile).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getUserProfile(uid: String): Flow<UserProfile?> = callbackFlow {
        val listenerRegistration = usersCollection.document(uid).addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            if (snapshot != null && snapshot.exists()) {
                val profile = snapshot.toObject(UserProfile::class.java)
                trySend(profile)
            } else {
                trySend(null)
            }
        }
        awaitClose { listenerRegistration.remove() }
    }

    override suspend fun updateUserProfile(uid: String, updates: Map<String, Any>): Result<Unit> {
        return try {
            usersCollection.document(uid).update(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getAllUsers(): Flow<List<UserProfile>> = channelFlow {
        // 1. Launch a background job to fetch latest from Firestore and save to Room
        launch {
            try {
                val snapshot = usersCollection.get().await()
                val profiles = snapshot.toObjects(UserProfile::class.java)
                userDao.insertUsers(profiles.map { UserEntity.fromUserProfile(it) })
            } catch (e: Exception) {
                // Network errors are ignored here, relying on offline Room cache
            }
        }

        // 2. Continually emit from Room Database (Single Source of Truth)
        userDao.getAllUsers().collect { entities ->
            send(entities.map { it.toUserProfile() })
        }
    }
}
