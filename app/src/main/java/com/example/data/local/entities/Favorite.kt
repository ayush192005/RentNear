package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import com.example.data.model.Property

@Entity(
    tableName = "favorites",
    primaryKeys = ["userId", "propertyId"],
    foreignKeys = [
        ForeignKey(
            entity = Property::class,
            parentColumns = ["id"],
            childColumns = ["propertyId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["propertyId"]),
        Index(value = ["userId"])
    ]
)
data class Favorite(
    val userId: String,
    val propertyId: String,
    val createdAt: Long = System.currentTimeMillis()
)
