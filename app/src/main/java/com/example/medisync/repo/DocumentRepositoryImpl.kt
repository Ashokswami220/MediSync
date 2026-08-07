package com.example.medisync.repo

import com.cloudinary.android.MediaManager.get
import com.example.medisync.model.DocumentMetadata
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.google.firebase.firestore.AggregateSource

class DocumentRepositoryImpl(private val firestore: FirebaseFirestore) : DocumentRepository {

    override suspend fun saveDocumentMetadata(
        documentName: String,
        fileUrl: String,
        linkedUser: String,
        linkedUserUid: String,
        linkedMember: String,
        publicId: String,
        resourceType: String,
        uploaderEmail: String
    ): Result<Unit> {
        return try {
            val documentData = DocumentMetadata(
                documentName = documentName,
                fileUrl = fileUrl,
                linkedUser = linkedUser,
                linkedUserUid = linkedUserUid,
                linkedMember = linkedMember,
                uploadedAt = System.currentTimeMillis(),
                publicId = publicId,
                resourceType = resourceType,
                uploaderEmail = uploaderEmail
            )

            firestore.collection("uploaded_documents")
                .add(documentData)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getDocuments(userUid: String?): Flow<List<DocumentMetadata>> = callbackFlow {
        var listenerRegistration: ListenerRegistration? = null

        if (userUid != null) {
            try {
                val userDoc = firestore.collection("users")
                    .document(userUid)
                    .get()
                    .await()
                val previousUids = (userDoc.get("previousUids") as? List<*>)
                    ?.filterIsInstance<String>() ?: emptyList()
                val allUids = listOf(userUid) + previousUids

                val query = firestore.collection("uploaded_documents")
                    .whereIn("linkedUserUid", allUids)
                listenerRegistration = query.addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        close(error)
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        val documents = snapshot.documents.mapNotNull { doc ->
                            doc.toObject(DocumentMetadata::class.java)
                                ?.copy(id = doc.id)
                        }
                            .sortedByDescending { it.uploadedAt }
                        trySend(documents)
                    }
                }
            } catch (e: Exception) {
                close(e)
            }
        } else {
            val query = firestore.collection("uploaded_documents")
            listenerRegistration = query.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val documents = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(DocumentMetadata::class.java)
                            ?.copy(id = doc.id)
                    }
                        .sortedByDescending { it.uploadedAt }
                    trySend(documents)
                }
            }
        }

        awaitClose {
            listenerRegistration?.remove()
        }
    }

    override suspend fun clearDataForUsers(uids: List<String>): Result<Unit> {
        return try {
            val batch = firestore.batch()
            for (uid in uids) {
                val snapshot = firestore.collection("uploaded_documents")
                    .whereEqualTo("linkedUserUid", uid)
                    .get()
                    .await()
                for (doc in snapshot.documents) {
                    destroyFromCloudinary(doc)
                    batch.delete(doc.reference)
                }
            }
            batch.commit()
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteDocument(docId: String): Result<Unit> {
        return try {
            val doc = firestore.collection("uploaded_documents")
                .document(docId)
                .get()
                .await()
            destroyFromCloudinary(doc)
            firestore.collection("uploaded_documents")
                .document(docId)
                .delete()
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun destroyFromCloudinary(doc: DocumentSnapshot) {
        var publicId = doc.getString("publicId")
        var resourceType = doc.getString("resourceType") ?: "image"

        if (publicId.isNullOrEmpty()) {
            val fileUrl = doc.getString("fileUrl") ?: ""
            if (fileUrl.isNotEmpty()) {
                resourceType = if (fileUrl.contains("/raw/")) "raw" else "image"
                val uploadIndex = fileUrl.indexOf("/upload/")
                if (uploadIndex != -1) {
                    val pathAfterUpload = fileUrl.substring(uploadIndex + 8)
                    val slashIndex = pathAfterUpload.indexOf("/")
                    if (slashIndex != -1) {
                        val fullPath = pathAfterUpload.substring(slashIndex + 1)
                        publicId = if (resourceType == "image") {
                            val lastDot = fullPath.lastIndexOf(".")
                            if (lastDot != -1) fullPath.substring(0, lastDot) else fullPath
                        } else {
                            fullPath
                        }
                    }
                }
            }
        }

        if (!publicId.isNullOrEmpty()) {
            try {
                withContext(Dispatchers.IO) {
                    get().cloudinary.uploader()
                        .destroy(
                            publicId,
                            mapOf("resource_type" to resourceType)
                        )
                }
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }
    }

    override suspend fun incrementReportOpenCount(): Result<Unit> {
        return try {
            val docRef = firestore.collection("system").document("stats")
            val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val today = formatter.format(Date())

            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(docRef)
                if (snapshot.exists()) {
                    val lastDate = snapshot.getString("lastOpenedDate") ?: ""
                    val currentTotal = snapshot.getLong("reportsOpenedCount") ?: 0L
                    var currentToday = snapshot.getLong("reportsOpenedTodayCount") ?: 0L
                    
                    if (lastDate == today) {
                        currentToday += 1
                    } else {
                        currentToday = 1
                    }
                    
                    transaction.update(docRef, "reportsOpenedCount", currentTotal + 1)
                    transaction.update(docRef, "reportsOpenedTodayCount", currentToday)
                    transaction.update(docRef, "lastOpenedDate", today)
                } else {
                    transaction.set(docRef, hashMapOf(
                        "reportsOpenedCount" to 1L,
                        "reportsOpenedTodayCount" to 1L,
                        "lastOpenedDate" to today
                    ))
                }
            }.await()
            Result.success(Unit)
        } catch (ex: Exception) {
            Result.failure(ex)
        }
    }

    override fun getReportOpenCount(): Flow<ReportStats> = callbackFlow {
        val listener = firestore.collection("system").document("stats")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(ReportStats())
                    return@addSnapshotListener
                }
                
                val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val today = formatter.format(Date())
                
                val lastDate = snapshot?.getString("lastOpenedDate") ?: ""
                val totalCount = snapshot?.getLong("reportsOpenedCount") ?: 0L
                val todayCount = if (lastDate == today) (snapshot?.getLong("reportsOpenedTodayCount") ?: 0L) else 0L
                
                trySend(ReportStats(totalOpened = totalCount, todayOpened = todayCount))
            }
        awaitClose { listener.remove() }
    }

    override fun getTotalReportsCount(): Flow<Long> = flow {
        try {
            val snapshot = firestore.collection("uploaded_documents").count().get(AggregateSource.SERVER).await()
            emit(snapshot.count)
        } catch (_: Exception) {
            emit(0L)
        }
    }
}
