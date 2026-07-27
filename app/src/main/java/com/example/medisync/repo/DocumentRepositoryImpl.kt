package com.example.medisync.repo

import com.cloudinary.android.MediaManager.get
import com.example.medisync.model.DocumentMetadata
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

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
        // Use whereEqualTo for standard users so Firestore security rules don't block the read
        val query: Query = if (userUid != null) {
            firestore.collection("uploaded_documents").whereEqualTo("linkedUserUid", userUid)
        } else {
            firestore.collection("uploaded_documents")
        }

        val listenerRegistration = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }

            if (snapshot != null) {
                val documents = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(DocumentMetadata::class.java)?.copy(id = doc.id)
                }
                    .sortedByDescending { it.uploadedAt }
                trySend(documents)
            }
        }

        awaitClose {
            listenerRegistration.remove()
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
}
