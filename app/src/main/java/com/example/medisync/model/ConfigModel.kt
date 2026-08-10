package com.example.medisync.model

import java.util.UUID

data class ContactModel(
    val id: String = UUID.randomUUID()
        .toString(),
    val name: String = "",
    val role: String = "Pharmacist",
    val experience: String = "",
    val phone: String = "",
    val imageResName: String = "holding_flowers" // "doctor1", "doctor2", "holding_flowers"
)

data class AppConfig(
    val contacts: List<ContactModel> = emptyList()
)
