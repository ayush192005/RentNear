package com.example.data.remote.dto

import com.example.data.model.FurnishingStatus
import com.example.data.model.ListedBy
import com.example.data.model.Property
import com.example.data.model.PropertyStatus
import com.example.data.model.PropertyType
import com.example.data.model.WaterSupply
import com.squareup.moshi.Json
import java.util.UUID

data class PropertyDto(
    @Json(name = "id") val id: String,
    @Json(name = "owner_id") val ownerId: String,
    @Json(name = "owner_name") val ownerName: String? = null,
    @Json(name = "title") val title: String,
    @Json(name = "description") val description: String? = null,
    @Json(name = "property_type") val propertyType: String = "Flat",
    @Json(name = "rent") val rent: Int,
    @Json(name = "security_deposit") val securityDeposit: Int? = null,
    @Json(name = "area_sqft") val areaSqft: Int,
    @Json(name = "bedrooms") val bedrooms: Int? = null,
    @Json(name = "bathrooms") val bathrooms: Int? = null,
    @Json(name = "address") val address: String,
    @Json(name = "locality") val locality: String? = null,
    @Json(name = "city") val city: String,
    @Json(name = "latitude") val latitude: Double? = null,
    @Json(name = "longitude") val longitude: Double? = null,
    @Json(name = "furnishing_status") val furnishingStatus: String? = null,
    @Json(name = "has_parking") val hasParking: Boolean? = null,
    @Json(name = "water_supply") val waterSupply: String? = null,
    @Json(name = "has_electricity_backup") val hasElectricityBackup: Boolean? = null,
    @Json(name = "has_balcony") val hasBalcony: Boolean? = null,
    @Json(name = "amenities") val amenities: List<String>? = null,
    @Json(name = "contact_phone") val contactPhone: String? = null,
    @Json(name = "whatsapp_number") val whatsappNumber: String? = null,
    @Json(name = "listed_by") val listedBy: String? = null,
    @Json(name = "status") val status: String? = null,
    @Json(name = "is_featured") val isFeatured: Boolean? = null,
    @Json(name = "image_urls") val imageUrls: List<String>? = null,
    @Json(name = "created_at") val createdAt: String? = null,
    @Json(name = "updated_at") val updatedAt: String? = null
) {
    fun toDomain(): Property {
        return Property(
            id = id,
            ownerId = ownerId,
            ownerName = ownerName ?: "Property Owner",
            title = title,
            description = description ?: "",
            propertyType = PropertyType.fromString(propertyType),
            rent = rent,
            securityDeposit = securityDeposit ?: (rent * 2),
            areaSqft = areaSqft,
            bedrooms = bedrooms ?: 1,
            bathrooms = bathrooms ?: 1,
            address = address,
            locality = locality ?: "",
            city = city,
            latitude = latitude ?: 0.0,
            longitude = longitude ?: 0.0,
            furnishingStatus = FurnishingStatus.fromString(furnishingStatus ?: ""),
            hasParking = hasParking ?: false,
            waterSupply = WaterSupply.fromString(waterSupply ?: ""),
            hasElectricityBackup = hasElectricityBackup ?: false,
            hasBalcony = hasBalcony ?: false,
            amenities = amenities ?: emptyList(),
            contactPhone = contactPhone ?: "+91 98765 43210",
            whatsappNumber = whatsappNumber ?: "919876543210",
            listedBy = ListedBy.fromString(listedBy ?: ""),
            status = PropertyStatus.fromString(status ?: ""),
            isFeatured = isFeatured ?: false,
            imageUrls = imageUrls ?: emptyList(),
            isSample = false,
            createdAt = parseIsoOrEpoch(createdAt),
            updatedAt = parseIsoOrEpoch(updatedAt)
        )
    }

    fun toUpdateMap(): Map<String, Any?> = mapOf(
        "title" to title,
        "description" to (description ?: ""),
        "property_type" to propertyType,
        "rent" to rent,
        "security_deposit" to (securityDeposit ?: 0),
        "area_sqft" to areaSqft,
        "bedrooms" to (bedrooms ?: 1),
        "bathrooms" to (bathrooms ?: 1),
        "address" to address,
        "locality" to (locality ?: ""),
        "city" to city,
        "latitude" to (latitude ?: 0.0),
        "longitude" to (longitude ?: 0.0),
        "furnishing_status" to (furnishingStatus ?: "Unfurnished"),
        "has_parking" to (hasParking ?: false),
        "water_supply" to (waterSupply ?: "Corporation"),
        "has_electricity_backup" to (hasElectricityBackup ?: false),
        "has_balcony" to (hasBalcony ?: false),
        "amenities" to (amenities ?: emptyList<String>()),
        "contact_phone" to (contactPhone ?: ""),
        "whatsapp_number" to (whatsappNumber ?: ""),
        "listed_by" to (listedBy ?: "Owner"),
        "status" to (status ?: "Available"),
        "is_featured" to (isFeatured ?: false),
        "image_urls" to (imageUrls ?: emptyList<String>()),
        "owner_name" to (ownerName ?: "")
    )

    companion object {
        private fun ensureValidUuid(raw: String): String {
            return try {
                UUID.fromString(raw).toString()
            } catch (_: Exception) {
                UUID.nameUUIDFromBytes(raw.toByteArray()).toString()
            }
        }

        private fun parseIsoOrEpoch(raw: String?): Long {
            if (raw.isNullOrBlank()) return System.currentTimeMillis()
            return try {
                java.time.Instant.parse(raw).toEpochMilli()
            } catch (_: Exception) {
                raw.toLongOrNull() ?: System.currentTimeMillis()
            }
        }

        fun fromDomain(property: Property): PropertyDto {
            return PropertyDto(
                id = property.id,
                ownerId = property.ownerId,
                ownerName = property.ownerName,
                title = property.title,
                description = property.description,
                propertyType = property.propertyType.displayName,
                rent = property.rent,
                securityDeposit = property.securityDeposit,
                areaSqft = property.areaSqft,
                bedrooms = property.bedrooms,
                bathrooms = property.bathrooms,
                address = property.address,
                locality = property.locality,
                city = property.city,
                latitude = property.latitude,
                longitude = property.longitude,
                furnishingStatus = property.furnishingStatus.displayName,
                hasParking = property.hasParking,
                waterSupply = property.waterSupply.displayName,
                hasElectricityBackup = property.hasElectricityBackup,
                hasBalcony = property.hasBalcony,
                amenities = property.amenities,
                contactPhone = property.contactPhone,
                whatsappNumber = property.whatsappNumber,
                listedBy = property.listedBy.displayName,
                status = property.status.displayName,
                isFeatured = property.isFeatured,
                imageUrls = property.imageUrls,
                createdAt = null,
                updatedAt = null
            )
        }
    }
}
