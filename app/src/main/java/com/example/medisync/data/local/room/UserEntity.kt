package com.example.medisync.data.local.room

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.medisync.model.MemberVitals
import com.example.medisync.model.UserProfile
import com.example.medisync.model.UserRole

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val uid: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val phoneNumber: String,
    val role: String, // Store enum as String
    val bloodType: String,
    val bloodPressure: String,
    val bloodSugar: String,
    val bloodTypeLastUpdated: Long,
    val bloodPressureLastUpdated: Long,
    val bloodSugarLastUpdated: Long,
    val members: List<String>,
    val accountCreatedTime: Long,
    val avatarUrl: String,
    val documents: List<String>,
    val isPlaceholder: Boolean,
    val claimedByUid: String?,
    val previousUids: List<String>,
    val memberVitals: Map<String, MemberVitals>
) {
    fun toUserProfile(): UserProfile {
        return UserProfile(
            uid = uid,
            firstName = firstName,
            lastName = lastName,
            email = email,
            phoneNumber = phoneNumber,
            role = try {
                UserRole.valueOf(role)
            } catch (_: Exception) {
                UserRole.USER
            },
            bloodType = bloodType,
            bloodPressure = bloodPressure,
            bloodSugar = bloodSugar,
            bloodTypeLastUpdated = bloodTypeLastUpdated,
            bloodPressureLastUpdated = bloodPressureLastUpdated,
            bloodSugarLastUpdated = bloodSugarLastUpdated,
            members = members,
            accountCreatedTime = accountCreatedTime,
            avatarUrl = avatarUrl,
            documents = documents,
            isPlaceholder = isPlaceholder,
            claimedByUid = claimedByUid,
            previousUids = previousUids,
            memberVitals = memberVitals
        )
    }

    companion object {
        fun fromUserProfile(profile: UserProfile): UserEntity {
            return UserEntity(
                uid = profile.uid,
                firstName = profile.firstName,
                lastName = profile.lastName,
                email = profile.email,
                phoneNumber = profile.phoneNumber,
                role = profile.role.name,
                bloodType = profile.bloodType,
                bloodPressure = profile.bloodPressure,
                bloodSugar = profile.bloodSugar,
                bloodTypeLastUpdated = profile.bloodTypeLastUpdated,
                bloodPressureLastUpdated = profile.bloodPressureLastUpdated,
                bloodSugarLastUpdated = profile.bloodSugarLastUpdated,
                members = profile.members,
                accountCreatedTime = profile.accountCreatedTime,
                avatarUrl = profile.avatarUrl,
                documents = profile.documents,
                isPlaceholder = profile.isPlaceholder,
                claimedByUid = profile.claimedByUid,
                previousUids = profile.previousUids,
                memberVitals = profile.memberVitals
            )
        }
    }
}
