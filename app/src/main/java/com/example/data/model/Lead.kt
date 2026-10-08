package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Lead capture record representing an initial contact inquiry
 * submitted by a tenant/seeker before accessing owner contact details.
 */
@Entity(tableName = "leads")
data class Lead(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val propertyId: String,
    val propertyTitle: String,
    val ownerId: String = "",
    val userName: String,
    val userPhone: String,
    val message: String = "",
    val contactMethod: String = "CALL", // "CALL" or "WHATSAPP"
    val timestamp: Long = System.currentTimeMillis()
)
