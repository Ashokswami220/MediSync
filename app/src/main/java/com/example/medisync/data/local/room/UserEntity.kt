package com.example.medisync.data.local.room

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.medisync.model.UserProfile
import com.example.medisync.model.UserRole

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val uid: String,
    val firstName: String,
    val lastName: String,
    val phoneNumber: String,
    val role: String, // Store enum as String
    val bloodType: String,
    val bloodPressure: String,
    val bloodSugar: String,
    val members: List<String>,
    val accountCreatedTime: Long,
    val avatarUrl: String,
    val documents: List<String>
) {
    fun toUserProfile(): UserProfile {
        return UserProfile(
            uid = uid,
            firstName = firstName,
            lastName = lastName,
            phoneNumber = phoneNumber,
            role = try { UserRole.valueOf(role) } catch (_: Exception) { UserRole.USER },
            bloodType = bloodType,
            bloodPressure = bloodPressure,
            bloodSugar = bloodSugar,
            members = members,
            accountCreatedTime = accountCreatedTime,
            avatarUrl = avatarUrl,
            documents = documents
        )
    }

    companion object {
        fun fromUserProfile(profile: UserProfile): UserEntity {
            return UserEntity(
                uid = profile.uid,
                firstName = profile.firstName,
                lastName = profile.lastName,
                phoneNumber = profile.phoneNumber,
                role = profile.role.name,
                bloodType = profile.bloodType,
                bloodPressure = profile.bloodPressure,
                bloodSugar = profile.bloodSugar,
                members = profile.members,
                accountCreatedTime = profile.accountCreatedTime,
                avatarUrl = profile.avatarUrl,
                documents = profile.documents
            )
        }
    }
}
