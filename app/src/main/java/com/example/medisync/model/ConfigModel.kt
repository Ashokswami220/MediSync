package com.example.medisync.model

import java.util.UUID

data class ContactModel(
    val id: String = UUID.randomUUID()
        .toString(),
    val name: String = "",
    val role: String = "Doctor",
    val experience: String = "",
    val phone: String = "",
    val imageResName: String = "holding_flowers", // "doctor1", "doctor2", "holding_flowers"
    val headingItem: Boolean = false,
    val category: String = "Doctor"
)

data class AppConfig(
    val contacts: List<ContactModel> = emptyList()
)
