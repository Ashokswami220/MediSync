package com.example.medisync.repo

import androidx.room.withTransaction
import com.example.medisync.data.local.room.MediSyncDatabase
import com.example.medisync.data.local.room.UserDao
import com.example.medisync.data.local.room.UserEntity
import com.example.medisync.model.UserProfile
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class UserRepositoryImpl(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val userDao: UserDao,
    private val db: MediSyncDatabase
) : UserRepository {

    private val usersCollection = firestore.collection("users")

    override suspend fun createUserProfile(userProfile: UserProfile): Result<Unit> {
        return try {
            usersCollection.document(userProfile.uid)
                .set(userProfile)
                .await()
            userDao.insertUsers(listOf(UserEntity.fromUserProfile(userProfile)))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getUserProfile(uid: String): Flow<UserProfile?> = callbackFlow {
        val listenerRegistration = usersCollection.document(uid)
            .addSnapshotListener { snapshot, error ->
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

    override suspend fun getUserProfileSync(uid: String): Result<UserProfile?> {
        return try {
            val snapshot = usersCollection.document(uid)
                .get()
                .await()
            if (snapshot.exists()) {
                Result.success(snapshot.toObject(UserProfile::class.java))
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateUserProfile(uid: String, updates: Map<String, Any>): Result<Unit> {
        return try {
            usersCollection.document(uid)
                .update(updates)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getAllUsers(): Flow<List<UserProfile>> = channelFlow {
        // 1. Launch a background job to fetch latest from Firestore and save to Room
        launch {
            try {
                val snapshot = usersCollection.get()
                    .await()
                android.util.Log.d("UserRepositoryImpl", "Fetched ${snapshot.documents.size} from Firestore")
                val profiles = snapshot.documents.mapNotNull { doc ->
                    try {
                        val profile = doc.toObject(UserProfile::class.java)
                        val isPlaceholder = doc.getBoolean("isPlaceholder") ?: doc.getBoolean("placeholder") ?: false
                        profile?.copy(uid = doc.id, isPlaceholder = isPlaceholder)
                    } catch (e: Exception) {
                        android.util.Log.e("UserRepositoryImpl", "Error mapping user document ${doc.id}", e)
                        null
                    }
                }
                android.util.Log.d("UserRepositoryImpl", "Mapped ${profiles.size} profiles")
                db.withTransaction {
                    userDao.deleteAllUsers()
                    userDao.insertUsers(profiles.map { UserEntity.fromUserProfile(it) })
                }
                android.util.Log.d("UserRepositoryImpl", "Inserted ${profiles.size} users into Room")
            } catch (e: Exception) {
                android.util.Log.e("UserRepositoryImpl", "Error fetching users from Firestore", e)
                // Network errors are ignored here, relying on offline Room cache
            }
        }

        // 2. Continually emit from Room Database (Single Source of Truth)
        userDao.getAllUsers()
            .collect { entities ->
                android.util.Log.d("UserRepositoryImpl", "Room emitted ${entities.size} users")
                send(entities.map { it.toUserProfile() })
            }
    }

    override suspend fun deleteUsers(uids: List<String>): Result<Unit> {
        return try {
            val batch = firestore.batch()
            uids.forEach { uid ->
                batch.delete(usersCollection.document(uid))
            }
            batch.commit()
                .await()

            db.withTransaction {
                userDao.deleteUsersByUids(uids)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun checkUserExists(phoneOrEmail: String): Boolean {
        return try {
            val phoneQuery = usersCollection.whereEqualTo("phoneNumber", phoneOrEmail)
                .get()
                .await()
            if (!phoneQuery.isEmpty) return true

            val emailQuery = usersCollection.whereEqualTo("email", phoneOrEmail)
                .get()
                .await()
            if (!emailQuery.isEmpty) return true

            false
        } catch (_: Exception) {
            false
        }
    }

    override suspend fun findPlaceholder(phoneOrEmail: String): UserProfile? {
        return try {
            android.util.Log.d("MediSync", "findPlaceholder searching for: '$phoneOrEmail'")

            // Query by phoneNumber (single field — no composite index needed)
            val phoneQuery = usersCollection
                .whereEqualTo("phoneNumber", phoneOrEmail)
                .get()
                .await()

            android.util.Log.d("MediSync", "Phone query returned ${phoneQuery.size()} documents")

            for (doc in phoneQuery.documents) {
                // Read raw fields — handle both "isPlaceholder" and "placeholder" (JavaBeans naming issue)
                val isPlaceholder = doc.getBoolean("isPlaceholder") ?: doc.getBoolean("placeholder") ?: false
                val claimedByUid = doc.getString("claimedByUid")
                android.util.Log.d("MediSync", "  Doc ${doc.id}: isPlaceholder=$isPlaceholder, claimedByUid=$claimedByUid, fields=${doc.data?.keys}")
                if (isPlaceholder && claimedByUid == null) {
                    android.util.Log.d("MediSync", "  → MATCH! Returning placeholder ${doc.id}")
                    return doc.toObject(UserProfile::class.java)?.copy(uid = doc.id, isPlaceholder = true)
                }
            }

            // Query by email (single field — no composite index needed)
            val emailQuery = usersCollection
                .whereEqualTo("email", phoneOrEmail)
                .get()
                .await()

            android.util.Log.d("MediSync", "Email query returned ${emailQuery.size()} documents")

            for (doc in emailQuery.documents) {
                val isPlaceholder = doc.getBoolean("isPlaceholder") ?: doc.getBoolean("placeholder") ?: false
                val claimedByUid = doc.getString("claimedByUid")
                android.util.Log.d("MediSync", "  Doc ${doc.id}: isPlaceholder=$isPlaceholder, claimedByUid=$claimedByUid, fields=${doc.data?.keys}")
                if (isPlaceholder && claimedByUid == null) {
                    android.util.Log.d("MediSync", "  → MATCH! Returning placeholder ${doc.id}")
                    return doc.toObject(UserProfile::class.java)?.copy(uid = doc.id, isPlaceholder = true)
                }
            }

            android.util.Log.d("MediSync", "No placeholder found for: '$phoneOrEmail'")
            null
        } catch (e: Exception) {
            android.util.Log.e("MediSync", "findPlaceholder FAILED for: '$phoneOrEmail'", e)
            null
        }
    }

    override suspend fun claimPlaceholder(
        placeholderUid: String,
        realUserUid: String,
        realUserName: String
    ): Result<Unit> {
        return try {
            android.util.Log.d("MediSync", "Claiming placeholder $placeholderUid → $realUserUid ($realUserName)")

            // 1. FIRST: Mark placeholder as claimed (this must succeed)
            usersCollection.document(placeholderUid)
                .update("claimedByUid", realUserUid)
                .await()
            android.util.Log.d("MediSync", "Marked placeholder as claimed")

            // 2. Transfer all documents: update linkedUserUid and linkedUser name
            val docsSnapshot = firestore.collection("uploaded_documents")
                .whereEqualTo("linkedUserUid", placeholderUid)
                .get()
                .await()

            if (!docsSnapshot.isEmpty) {
                val batch = firestore.batch()
                for (doc in docsSnapshot.documents) {
                    batch.update(
                        doc.reference,
                        mapOf(
                            "linkedUserUid" to realUserUid,
                            "linkedUser" to realUserName
                        )
                    )
                }
                batch.commit().await()
                android.util.Log.d("MediSync", "Transferred ${docsSnapshot.size()} documents")
            } else {
                android.util.Log.d("MediSync", "No documents to transfer")
            }

            // 3. Try to delete the old placeholder (best-effort)
            try {
                usersCollection.document(placeholderUid).delete().await()
                db.withTransaction { userDao.deleteUsersByUids(listOf(placeholderUid)) }
                android.util.Log.d("MediSync", "Deleted placeholder from Firestore + Room")
            } catch (deleteEx: Exception) {
                // Delete failed (e.g. security rules) — that's OK, it's already marked as claimed
                android.util.Log.w("MediSync", "Could not delete placeholder (claimed anyway): ${deleteEx.message}")
            }

            Result.success(Unit)
        } catch (e: Exception) {
            android.util.Log.e("MediSync", "claimPlaceholder FAILED", e)
            Result.failure(e)
        }
    }
}
