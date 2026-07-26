package com.example.medisync.repo

import com.example.medisync.model.DocumentMetadata
import kotlinx.coroutines.flow.Flow

interface DocumentRepository {
    suspend fun saveDocumentMetadata(
        documentName: String,
        fileUrl: String,
        linkedUser: String,
        linkedUserUid: String,
        linkedMember: String,
        publicId: String,
        resourceType: String,
        uploaderEmail: String
    ): Result<Unit>

    fun getDocuments(userUid: String? = null): Flow<List<DocumentMetadata>>

    suspend fun clearDataForUsers(uids: List<String>): Result<Unit>

    suspend fun deleteDocument(docId: String): Result<Unit>
}
