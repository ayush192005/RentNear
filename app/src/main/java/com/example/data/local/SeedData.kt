package com.example.data.local

import com.example.data.model.Property
import com.example.data.model.UserProfile
import com.example.data.model.UserRole

/**
 * Production SeedData configuration.
 * All demo accounts, demo houses, flats, shops, and sample entities have been completely removed.
 * The app initializes as an empty, production-ready environment for real users.
 */
object SeedData {
    val DEFAULT_OWNER = UserProfile.EMPTY.copy(
        role = UserRole.OWNER
    )

    val DEFAULT_TENANT = UserProfile.EMPTY.copy(
        role = UserRole.TENANT
    )

    fun getSampleProperties(ownerId: String = ""): List<Property> {
        return emptyList()
    }
}
