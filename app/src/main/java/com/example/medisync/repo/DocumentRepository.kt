package com.example.medisync.repo

import com.example.medisync.model.DocumentMetadata
import kotlinx.coroutines.flow.Flow

data class ReportStats(
    val totalOpened: Long = 0L,
    val todayOpened: Long = 0L
)

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

    suspend fun incrementReportOpenCount(): Result<Unit>

    fun getReportOpenCount(): Flow<ReportStats>

    fun getTotalReportsCount(): Flow<Long>
}
