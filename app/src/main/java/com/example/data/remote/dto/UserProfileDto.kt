package com.example.data.remote.dto

import com.example.data.model.UserProfile
import com.example.data.model.UserRole
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UserProfileDto(
    @Json(name = "id") val id: String,
    @Json(name = "email") val email: String,
    @Json(name = "full_name") val fullName: String? = null,
    @Json(name = "phone") val phone: String? = null,
    @Json(name = "role") val role: String? = "TENANT",
    @Json(name = "avatar_url") val avatarUrl: String? = null,
    @Json(name = "created_at") val createdAt: Long? = null
) {
    fun toDomain(): UserProfile {
        return UserProfile(
            id = id,
            email = email,
            fullName = fullName ?: "User",
            phone = phone ?: "",
            role = try {
                UserRole.valueOf(role?.uppercase() ?: "")
            } catch (_: Exception) {
                UserRole.TENANT
            },
            avatarUrl = avatarUrl ?: "",
            createdAt = createdAt ?: System.currentTimeMillis()
        )
    }

    companion object {
        fun fromDomain(user: UserProfile): UserProfileDto {
            return UserProfileDto(
                id = user.id,
                email = user.email,
                fullName = user.fullName,
                phone = user.phone,
                role = user.role.name,
                avatarUrl = user.avatarUrl,
                createdAt = user.createdAt
            )
        }
    }
}
