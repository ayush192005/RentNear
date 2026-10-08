package com.example.data.remote.dto

import com.example.data.model.FurnishingStatus
import com.example.data.model.ListedBy
import com.example.data.model.Property
import com.example.data.model.PropertyStatus
import com.example.data.model.PropertyType
import com.example.data.model.WaterSupply
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PropertyDto(
    @Json(name = "id") val id: String,
    @Json(name = "owner_id") val ownerId: String,
    @Json(name = "owner_name") val ownerName: String? = null,
    @Json(name = "title") val title: String,
    @Json(name = "description") val description: String? = null,
    @Json(name = "property_type") val propertyType: String = "FLAT",
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
    @Json(name = "is_sample") val isSample: Boolean? = null,
    @Json(name = "created_at") val createdAt: Long? = null,
    @Json(name = "updated_at") val updatedAt: Long? = null
) {
    fun toDomain(): Property {
        return Property(
            id = id,
            ownerId = ownerId,
            ownerName = ownerName ?: "Property Owner",
            title = title,
            description = description ?: "",
            propertyType = try {
                PropertyType.valueOf(propertyType.uppercase())
            } catch (_: Exception) {
                PropertyType.FLAT
            },
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
            furnishingStatus = try {
                FurnishingStatus.valueOf(furnishingStatus?.uppercase() ?: "")
            } catch (_: Exception) {
                FurnishingStatus.UNFURNISHED
            },
            hasParking = hasParking ?: false,
            waterSupply = try {
                WaterSupply.valueOf(waterSupply?.uppercase() ?: "")
            } catch (_: Exception) {
                WaterSupply.CORPORATION
            },
            hasElectricityBackup = hasElectricityBackup ?: false,
            hasBalcony = hasBalcony ?: false,
            amenities = amenities ?: emptyList(),
            contactPhone = contactPhone ?: "+91 98765 43210",
            whatsappNumber = whatsappNumber ?: "919876543210",
            listedBy = try {
                ListedBy.valueOf(listedBy?.uppercase() ?: "")
            } catch (_: Exception) {
                ListedBy.OWNER
            },
            status = try {
                PropertyStatus.valueOf(status?.uppercase() ?: "")
            } catch (_: Exception) {
                PropertyStatus.AVAILABLE
            },
            isFeatured = isFeatured ?: false,
            imageUrls = imageUrls ?: emptyList(),
            isSample = isSample ?: false,
            createdAt = createdAt ?: System.currentTimeMillis(),
            updatedAt = updatedAt ?: System.currentTimeMillis()
        )
    }

    companion object {
        fun fromDomain(property: Property): PropertyDto {
            return PropertyDto(
                id = property.id,
                ownerId = property.ownerId,
                ownerName = property.ownerName,
                title = property.title,
                description = property.description,
                propertyType = property.propertyType.name,
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
                furnishingStatus = property.furnishingStatus.name,
                hasParking = property.hasParking,
                waterSupply = property.waterSupply.name,
                hasElectricityBackup = property.hasElectricityBackup,
                hasBalcony = property.hasBalcony,
                amenities = property.amenities,
                contactPhone = property.contactPhone,
                whatsappNumber = property.whatsappNumber,
                listedBy = property.listedBy.name,
                status = property.status.name,
                isFeatured = property.isFeatured,
                imageUrls = property.imageUrls,
                isSample = property.isSample,
                createdAt = property.createdAt,
                updatedAt = property.updatedAt
            )
        }
    }
}
