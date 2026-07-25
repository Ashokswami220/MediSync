package com.example.medisync.model

data class UserProfile(
    val uid: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val phoneNumber: String = "",
    val role: UserRole = UserRole.USER,
    val bloodType: String = "",
    val bloodPressure: String = "",
    val bloodSugar: String = "",
    val members: List<String> = emptyList(),
    val accountCreatedTime: Long = System.currentTimeMillis(),
    val avatarUrl: String = "",
    val email: String = "",
    val documents: List<String> = emptyList()
)
