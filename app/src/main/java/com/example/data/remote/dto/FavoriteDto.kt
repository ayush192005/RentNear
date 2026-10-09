package com.example.data.remote.dto

import com.example.data.local.entities.Favorite
import com.squareup.moshi.Json
import java.util.UUID

data class FavoriteDto(
    @Json(name = "user_id") val userId: String,
    @Json(name = "property_id") val propertyId: String,
    @Json(name = "created_at") val createdAt: String? = null
) {
    fun toDomain(): Favorite {
        val parsedTime = try {
            if (!createdAt.isNullOrBlank()) {
                java.time.Instant.parse(createdAt).toEpochMilli()
            } else System.currentTimeMillis()
        } catch (_: Exception) {
            createdAt?.toLongOrNull() ?: System.currentTimeMillis()
        }

        return Favorite(
            userId = userId,
            propertyId = propertyId,
            createdAt = parsedTime
        )
    }

    companion object {
        private fun ensureUuid(input: String): String {
            return try {
                UUID.fromString(input).toString()
            } catch (_: Exception) {
                UUID.nameUUIDFromBytes(input.toByteArray()).toString()
            }
        }

        fun fromDomain(favorite: Favorite): FavoriteDto {
            return FavoriteDto(
                userId = favorite.userId,
                propertyId = favorite.propertyId,
                createdAt = null
            )
        }
    }
}
