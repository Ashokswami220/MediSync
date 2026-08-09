package com.example.medisync.model

import com.google.firebase.firestore.PropertyName

data class UserProfile(
    val uid: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val phoneNumber: String = "",
    val role: UserRole = UserRole.USER,
    val bloodType: String = "",
    val bloodPressure: String = "",
    val bloodSugar: String = "",
    val bloodTypeLastUpdated: Long = 0L,
    val bloodPressureLastUpdated: Long = 0L,
    val bloodSugarLastUpdated: Long = 0L,
    val members: List<String> = emptyList(),
    val accountCreatedTime: Long = System.currentTimeMillis(),
    val avatarUrl: String = "",
    val email: String = "",
    val documents: List<String> = emptyList(),
    @get:PropertyName("isPlaceholder")
    @set:PropertyName("isPlaceholder")
    var isPlaceholder: Boolean = false,
    val claimedByUid: String? = null,
    val previousUids: List<String> = emptyList(),
    val memberVitals: Map<String, MemberVitals> = emptyMap(),
    val fcmToken: String = ""
)

data class MemberVitals(
    val bloodType: String = "",
    val bloodPressure: String = "",
    val bloodSugar: String = "",
    val bloodTypeLastUpdated: Long = 0L,
    val bloodPressureLastUpdated: Long = 0L,
    val bloodSugarLastUpdated: Long = 0L
)
