package com.example.data.local

import androidx.room.TypeConverter
import com.example.data.model.FurnishingStatus
import com.example.data.model.ListedBy
import com.example.data.model.PropertyStatus
import com.example.data.model.PropertyType
import com.example.data.model.UserRole
import com.example.data.model.WaterSupply

class Converters {
    @TypeConverter
    fun fromStringList(value: List<String>?): String {
        return value?.joinToString(separator = "|||") ?: ""
    }

    @TypeConverter
    fun toStringList(value: String?): List<String> {
        if (value.isNullOrBlank()) return emptyList()
        return value.split("|||").filter { it.isNotBlank() }
    }

    @TypeConverter
    fun fromUserRole(value: UserRole?): String = value?.name ?: UserRole.TENANT.name

    @TypeConverter
    fun toUserRole(value: String?): UserRole = value?.let {
        try { UserRole.valueOf(it) } catch (_: Exception) { UserRole.TENANT }
    } ?: UserRole.TENANT

    @TypeConverter
    fun fromPropertyType(value: PropertyType?): String = value?.name ?: PropertyType.FLAT.name

    @TypeConverter
    fun toPropertyType(value: String?): PropertyType = value?.let {
        try { PropertyType.valueOf(it) } catch (_: Exception) { PropertyType.FLAT }
    } ?: PropertyType.FLAT

    @TypeConverter
    fun fromPropertyStatus(value: PropertyStatus?): String = value?.name ?: PropertyStatus.AVAILABLE.name

    @TypeConverter
    fun toPropertyStatus(value: String?): PropertyStatus = value?.let {
        try { PropertyStatus.valueOf(it) } catch (_: Exception) { PropertyStatus.AVAILABLE }
    } ?: PropertyStatus.AVAILABLE

    @TypeConverter
    fun fromFurnishingStatus(value: FurnishingStatus?): String = value?.name ?: FurnishingStatus.UNFURNISHED.name

    @TypeConverter
    fun toFurnishingStatus(value: String?): FurnishingStatus = value?.let {
        try { FurnishingStatus.valueOf(it) } catch (_: Exception) { FurnishingStatus.UNFURNISHED }
    } ?: FurnishingStatus.UNFURNISHED

    @TypeConverter
    fun fromWaterSupply(value: WaterSupply?): String = value?.name ?: WaterSupply.CORPORATION.name

    @TypeConverter
    fun toWaterSupply(value: String?): WaterSupply = value?.let {
        try { WaterSupply.valueOf(it) } catch (_: Exception) { WaterSupply.CORPORATION }
    } ?: WaterSupply.CORPORATION

    @TypeConverter
    fun fromListedBy(value: ListedBy?): String = value?.name ?: ListedBy.OWNER.name

    @TypeConverter
    fun toListedBy(value: String?): ListedBy = value?.let {
        try { ListedBy.valueOf(it) } catch (_: Exception) { ListedBy.OWNER }
    } ?: ListedBy.OWNER
}
