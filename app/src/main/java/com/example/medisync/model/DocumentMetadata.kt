package com.example.medisync.model

data class DocumentMetadata(
    var id: String = "",
    var documentName: String = "",
    var fileUrl: String = "",
    var linkedUser: String = "",
    var linkedUserUid: String = "",
    var linkedMember: String = "",
    var uploadedAt: Long = 0L,
    var publicId: String = "",
    var resourceType: String = "",
    var uploaderEmail: String = ""
)
