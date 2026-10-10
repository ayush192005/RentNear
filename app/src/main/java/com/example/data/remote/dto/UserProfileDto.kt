package com.example.data.remote.dto

import com.example.data.model.UserProfile
import com.example.data.model.UserRole
import com.squareup.moshi.Json
import java.util.UUID

data class UserProfileDto(
    @Json(name = "id") val id: String,
    @Json(name = "email") val email: String,
    @Json(name = "full_name") val fullName: String? = null,
    @Json(name = "phone") val phone: String? = null,
    @Json(name = "role") val role: String? = "tenant",
    @Json(name = "avatar_url") val avatarUrl: String? = null,
    @Json(name = "created_at") val createdAt: String? = null
) {
    fun toDomain(): UserProfile {
        val parsedTime = try {
            if (!createdAt.isNullOrBlank()) {
                java.time.Instant.parse(createdAt).toEpochMilli()
            } else System.currentTimeMillis()
        } catch (_: Exception) {
            createdAt?.toLongOrNull() ?: System.currentTimeMillis()
        }

        val domainRole = try {
            UserRole.valueOf(role?.trim()?.uppercase() ?: "TENANT")
        } catch (_: Exception) {
            if (role?.contains("owner", ignoreCase = true) == true) UserRole.OWNER else UserRole.TENANT
        }

        return UserProfile(
            id = id,
            email = email,
            fullName = fullName ?: "User",
            phone = phone ?: "",
            role = domainRole,
            avatarUrl = avatarUrl ?: "",
            createdAt = parsedTime
        )
    }

    companion object {
        fun fromDomain(user: UserProfile): UserProfileDto {
            val validId = try {
                UUID.fromString(user.id).toString()
            } catch (_: Exception) {
                UUID.nameUUIDFromBytes(user.id.toByteArray()).toString()
            }

            return UserProfileDto(
                id = validId,
                email = user.email.trim().lowercase(),
                fullName = user.fullName.trim(),
                phone = user.phone.trim(),
                role = if (user.role == UserRole.OWNER) "owner" else "tenant",
                avatarUrl = user.avatarUrl,
                createdAt = null
            )
        }
    }
}
