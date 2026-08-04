package com.example.medisync.repo

import com.example.medisync.model.AppConfig
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class ConfigRepositoryImpl(
    firestore: FirebaseFirestore
) : ConfigRepository {

    private val configRef = firestore.collection("config").document("appConfig")

    override fun getConfig(): Flow<AppConfig> = callbackFlow {
        val listener = configRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(AppConfig())
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                val config = snapshot.toObject(AppConfig::class.java)
                if (config != null) {
                    trySend(config)
                } else {
                    trySend(AppConfig())
                }
            } else {
                trySend(AppConfig())
            }
        }
        awaitClose { listener.remove() }
    }

    override suspend fun updateConfig(config: AppConfig): Result<Unit> = runCatching {
        configRef.set(config).await()
    }
}
