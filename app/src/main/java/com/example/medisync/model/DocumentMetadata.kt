package com.example.medisync.model

data class DocumentMetadata(
    // Note: These fields MUST be var with default values so that Firebase's 
    // toObject() can use the no-arg constructor and set the fields via reflection.
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
