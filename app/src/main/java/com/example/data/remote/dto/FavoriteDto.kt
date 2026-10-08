package com.example.data.remote.dto

import com.example.data.local.entities.Favorite
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class FavoriteDto(
    @Json(name = "user_id") val userId: String,
    @Json(name = "property_id") val propertyId: String,
    @Json(name = "created_at") val createdAt: Long? = null
) {
    fun toDomain(): Favorite {
        return Favorite(
            userId = userId,
            propertyId = propertyId,
            createdAt = createdAt ?: System.currentTimeMillis()
        )
    }

    companion object {
        fun fromDomain(favorite: Favorite): FavoriteDto {
            return FavoriteDto(
                userId = favorite.userId,
                propertyId = favorite.propertyId,
                createdAt = favorite.createdAt
            )
        }
    }
}
