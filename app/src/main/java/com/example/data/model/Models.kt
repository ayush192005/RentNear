package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

enum class UserRole(val displayName: String) {
    TENANT("Tenant / Seeker"),
    OWNER("Property Owner"),
    ADMIN("Administrator")
}

@Entity(tableName = "profiles")
data class UserProfile(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val email: String,
    val fullName: String,
    val phone: String = "",
    val role: UserRole = UserRole.TENANT,
    val avatarUrl: String = "",
    val createdAt: Long = System.currentTimeMillis()
) {
    companion object {
        val EMPTY = UserProfile(
            id = "",
            email = "",
            fullName = "",
            phone = "",
            role = UserRole.TENANT,
            avatarUrl = ""
        )
    }
}

enum class PropertyType(val displayName: String) {
    HOUSE("House"),
    FLAT("Flat"),
    ROOM("Room"),
    SHOP("Shop"),
    OFFICE("Office"),
    PG("PG"),
    OTHER("Other");

    companion object {
        fun fromString(value: String): PropertyType {
            return entries.find { it.name.equals(value, ignoreCase = true) || it.displayName.equals(value, ignoreCase = true) } ?: OTHER
        }
    }
}

enum class PropertyStatus(val displayName: String) {
    AVAILABLE("Available"),
    RENTED("Rented"),
    SOLD("Sold"),
    HIDDEN("Hidden");

    companion object {
        fun fromString(value: String): PropertyStatus {
            return entries.find { it.name.equals(value, ignoreCase = true) || it.displayName.equals(value, ignoreCase = true) } ?: AVAILABLE
        }
    }
}

enum class FurnishingStatus(val displayName: String) {
    FURNISHED("Furnished"),
    SEMI_FURNISHED("Semi-Furnished"),
    UNFURNISHED("Unfurnished");

    companion object {
        fun fromString(value: String): FurnishingStatus {
            return entries.find { it.name.equals(value, ignoreCase = true) || it.displayName.equals(value, ignoreCase = true) } ?: UNFURNISHED
        }
    }
}

enum class WaterSupply(val displayName: String) {
    HOURS_24_7("24/7"),
    CORPORATION("Corporation"),
    BOREWELL("Borewell"),
    OTHER("Other");

    companion object {
        fun fromString(value: String): WaterSupply {
            return entries.find {
                it.name.equals(value, ignoreCase = true) ||
                it.displayName.equals(value, ignoreCase = true) ||
                (value.contains("24", ignoreCase = true) && it == HOURS_24_7)
            } ?: CORPORATION
        }
    }
}

enum class ListedBy(val displayName: String) {
    OWNER("Owner"),
    BROKER("Broker");

    companion object {
        fun fromString(value: String): ListedBy {
            return entries.find {
                it.name.equals(value, ignoreCase = true) ||
                it.displayName.equals(value, ignoreCase = true)
            } ?: OWNER
        }
    }
}

enum class SortOption(val displayName: String) {
    LOWEST_PRICE("Lowest Price"),
    HIGHEST_PRICE("Highest Price"),
    NEWEST("Newest"),
    LARGEST_AREA("Largest Area")
}

@Entity(
    tableName = "properties",
    indices = [
        Index(value = ["city"]),
        Index(value = ["locality"]),
        Index(value = ["propertyType"]),
        Index(value = ["rent"]),
        Index(value = ["areaSqft"]),
        Index(value = ["ownerId"]),
        Index(value = ["status"]),
        Index(value = ["createdAt"])
    ]
)
data class Property(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val ownerId: String,
    val ownerName: String = "Property Owner",
    val title: String,
    val description: String = "",
    val propertyType: PropertyType = PropertyType.FLAT,
    val rent: Int,
    val securityDeposit: Int = rent * 2,
    val areaSqft: Int,
    val bedrooms: Int = 1,
    val bathrooms: Int = 1,
    val address: String,
    val locality: String = "",
    val city: String,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val furnishingStatus: FurnishingStatus = FurnishingStatus.UNFURNISHED,
    val hasParking: Boolean = false,
    val waterSupply: WaterSupply = WaterSupply.CORPORATION,
    val hasElectricityBackup: Boolean = false,
    val hasBalcony: Boolean = false,
    val amenities: List<String> = emptyList(),
    val contactPhone: String = "+91 98765 43210",
    val whatsappNumber: String = "919876543210",
    val listedBy: ListedBy = ListedBy.OWNER,
    val status: PropertyStatus = PropertyStatus.AVAILABLE,
    val isFeatured: Boolean = false,
    val imageUrls: List<String> = emptyList(),
    val isSample: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    val primaryImageUrl: String?
        get() = imageUrls.firstOrNull()?.takeIf { it.isNotBlank() }
}

typealias Favorite = com.example.data.local.entities.Favorite

data class PropertyFilter(
    val searchQuery: String = "",
    val city: String? = null,
    val locality: String? = null,
    val propertyType: PropertyType? = null,
    val minRent: Int? = null,
    val maxRent: Int? = null,
    val minArea: Int? = null,
    val maxArea: Int? = null,
    val bedrooms: Int? = null,
    val bathrooms: Int? = null,
    val furnishingStatus: FurnishingStatus? = null,
    val hasParking: Boolean? = null,
    val waterSupply: WaterSupply? = null,
    val hasBalcony: Boolean? = null,
    val hasElectricityBackup: Boolean? = null,
    val listedBy: ListedBy? = null,
    val status: PropertyStatus? = PropertyStatus.AVAILABLE,
    val sortOption: SortOption = SortOption.NEWEST,
    val aiSearchPrompt: String? = null
)
