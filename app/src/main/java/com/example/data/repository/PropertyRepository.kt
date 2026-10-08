package com.example.data.repository

import android.util.Log
import com.example.data.local.AppDatabase
import com.example.data.local.SeedData
import com.example.data.local.daos.FavoriteDao
import com.example.data.local.daos.LeadDao
import com.example.data.local.daos.PropertyDao
import com.example.data.local.entities.Favorite
import com.example.data.model.Lead
import com.example.data.model.Property
import com.example.data.model.PropertyFilter
import com.example.data.model.PropertyStatus
import com.example.data.model.SortOption
import com.example.data.remote.SupabaseApiService
import com.example.data.remote.SupabaseNetworkClient
import com.example.data.remote.dto.FavoriteDto
import com.example.data.remote.dto.PropertyDto
import com.example.data.supabase.SupabaseHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/**
 * PropertyRepository orchestrates data between the Remote Supabase API
 * and the Local Room Database, implementing the Single Source of Truth (SSOT) pattern.
 *
 * - Room is the Single Source of Truth: UI always observes Room Flow streams.
 * - Network refreshes update Room, which automatically emits changes to the UI.
 * - Write operations persist to Room immediately, then sync with Supabase when connected.
 */
class PropertyRepository(
    val propertyDao: PropertyDao,
    val favoriteDao: FavoriteDao,
    val leadDao: LeadDao? = null,
    private var remoteApiService: SupabaseApiService? = null,
    private val supabaseHelper: SupabaseHelper? = null
) {
    // Convenience constructor accepting AppDatabase
    constructor(
        db: AppDatabase,
        remoteApiService: SupabaseApiService? = null,
        supabaseHelper: SupabaseHelper? = null
    ) : this(
        propertyDao = db.propertyDao(),
        favoriteDao = db.favoriteDao(),
        leadDao = db.leadDao(),
        remoteApiService = remoteApiService,
        supabaseHelper = supabaseHelper
    )

    private fun getActiveApiService(): SupabaseApiService? {
        if (remoteApiService != null) return remoteApiService
        val helper = supabaseHelper ?: return null
        if (!helper.isConfigured) return null
        return try {
            val service = SupabaseNetworkClient.createService(helper.supabaseUrl)
            remoteApiService = service
            service
        } catch (e: Exception) {
            Log.e("PropertyRepository", "Failed to create Supabase service: ${e.message}")
            null
        }
    }

    // ==========================================
    // SEED & CACHE MANAGEMENT
    // ==========================================

    suspend fun ensureSeeded() = withContext(Dispatchers.IO) {
        propertyDao.deleteDemoProperties()
        propertyDao.deleteSampleProperties()
    }

    suspend fun resetSampleData(ownerId: String = "") = withContext(Dispatchers.IO) {
        propertyDao.deleteDemoProperties()
        propertyDao.deleteSampleProperties()
    }

    suspend fun clearSampleData() = withContext(Dispatchers.IO) {
        propertyDao.deleteDemoProperties()
        propertyDao.deleteSampleProperties()
    }

    suspend fun clearAllProperties() = withContext(Dispatchers.IO) {
        propertyDao.deleteAllProperties()
    }

    // ==========================================
    // SINGLE SOURCE OF TRUTH (ROOM OBSERVABLES)
    // ==========================================

    fun getAllCitiesFlow(): Flow<List<String>> = propertyDao.getAllCitiesFlow()

    fun getFeaturedPropertiesFlow(): Flow<List<Property>> =
        propertyDao.getFeaturedPropertiesFlow()

    fun getRecentPropertiesFlow(limit: Int = 10): Flow<List<Property>> =
        propertyDao.getRecentPropertiesFlow(limit)

    fun getPropertyByIdFlow(id: String): Flow<Property?> =
        propertyDao.getPropertyByIdFlow(id)

    suspend fun getPropertyById(id: String): Property? = withContext(Dispatchers.IO) {
        propertyDao.getPropertyById(id)
    }

    fun getPropertiesByOwnerFlow(ownerId: String): Flow<List<Property>> =
        propertyDao.getPropertiesByOwnerFlow(ownerId)

    fun filterPropertiesFlow(filter: PropertyFilter): Flow<List<Property>> {
        return propertyDao.getAllPropertiesFlow().map { allProperties ->
            allProperties.filter { property ->
                // Status filter
                if (filter.status != null && property.status != filter.status) {
                    return@filter false
                }

                // Search query matching
                if (filter.searchQuery.isNotBlank()) {
                    val query = filter.searchQuery.trim().lowercase()
                    val matches = property.title.lowercase().contains(query) ||
                            property.address.lowercase().contains(query) ||
                            property.locality.lowercase().contains(query) ||
                            property.city.lowercase().contains(query) ||
                            property.description.lowercase().contains(query)
                    if (!matches) return@filter false
                }

                // City filter
                if (!filter.city.isNullOrBlank() && !property.city.equals(filter.city, ignoreCase = true)) {
                    return@filter false
                }

                // Property type
                if (filter.propertyType != null && property.propertyType != filter.propertyType) {
                    return@filter false
                }

                // Price filters
                if (filter.minRent != null && property.rent < filter.minRent) return@filter false
                if (filter.maxRent != null && property.rent > filter.maxRent) return@filter false

                // Area filters
                if (filter.minArea != null && property.areaSqft < filter.minArea) return@filter false
                if (filter.maxArea != null && property.areaSqft > filter.maxArea) return@filter false

                // Bedrooms & Bathrooms
                if (filter.bedrooms != null && property.bedrooms != filter.bedrooms) return@filter false
                if (filter.bathrooms != null && property.bathrooms != filter.bathrooms) return@filter false

                // Furnishing
                if (filter.furnishingStatus != null && property.furnishingStatus != filter.furnishingStatus) return@filter false

                // Parking & Amenities
                if (filter.hasParking == true && !property.hasParking) return@filter false
                if (filter.waterSupply != null && property.waterSupply != filter.waterSupply) return@filter false
                if (filter.hasBalcony == true && !property.hasBalcony) return@filter false
                if (filter.hasElectricityBackup == true && !property.hasElectricityBackup) return@filter false

                // Listed by
                if (filter.listedBy != null && property.listedBy != filter.listedBy) return@filter false

                true
            }.let { filtered ->
                when (filter.sortOption) {
                    SortOption.LOWEST_PRICE -> filtered.sortedBy { it.rent }
                    SortOption.HIGHEST_PRICE -> filtered.sortedByDescending { it.rent }
                    SortOption.NEWEST -> filtered.sortedByDescending { it.createdAt }
                    SortOption.LARGEST_AREA -> filtered.sortedByDescending { it.areaSqft }
                }
            }
        }
    }

    // ==========================================
    // REMOTE REFRESH & SYNCHRONIZATION (SSOT)
    // ==========================================

    /**
     * Refreshes properties from Supabase Remote API and updates the local Room database.
     * The UI automatically reacts to Room updates via Flow.
     */
    suspend fun refreshProperties(limit: Int = 50): Result<List<Property>> = withContext(Dispatchers.IO) {
        val api = getActiveApiService()
        val helper = supabaseHelper
        if (api == null || helper == null || !helper.isConfigured) {
            // Local fallback when Supabase is not configured
            ensureSeeded()
            return@withContext Result.success(emptyList())
        }

        try {
            val dtoList = api.getProperties(
                apiKey = helper.supabaseAnonKey,
                authHeader = "Bearer ${helper.supabaseAnonKey}",
                limit = limit
            )
            val domainProperties = dtoList.map { it.toDomain() }
            if (domainProperties.isNotEmpty()) {
                propertyDao.insertAll(domainProperties)
            }
            Result.success(domainProperties)
        } catch (e: Exception) {
            Log.w("PropertyRepository", "Failed to refresh from remote: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Refreshes a single property by ID from Supabase and caches it into Room.
     */
    suspend fun refreshPropertyById(id: String): Result<Property?> = withContext(Dispatchers.IO) {
        val api = getActiveApiService()
        val helper = supabaseHelper
        if (api == null || helper == null || !helper.isConfigured) {
            return@withContext Result.success(propertyDao.getPropertyById(id))
        }

        try {
            val dtoList = api.getPropertyById(
                apiKey = helper.supabaseAnonKey,
                authHeader = "Bearer ${helper.supabaseAnonKey}",
                idFilter = "eq.$id"
            )
            val prop = dtoList.firstOrNull()?.toDomain()
            if (prop != null) {
                propertyDao.insertProperty(prop)
            }
            Result.success(prop)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==========================================
    // MUTATIONS (PERSIST TO ROOM THEN SYNC REMOTE)
    // ==========================================

    suspend fun saveProperty(property: Property) = withContext(Dispatchers.IO) {
        // 1. Write to Room (Single Source of Truth)
        propertyDao.insertProperty(property)

        // 2. Sync to Supabase if configured
        val api = getActiveApiService()
        val helper = supabaseHelper
        if (api != null && helper != null && helper.isConfigured) {
            try {
                val dto = PropertyDto.fromDomain(property)
                api.createProperty(
                    apiKey = helper.supabaseAnonKey,
                    authHeader = "Bearer ${helper.supabaseAnonKey}",
                    property = dto
                )
            } catch (e: Exception) {
                Log.w("PropertyRepository", "Remote sync for save failed: ${e.message}")
            }
        }
    }

    suspend fun updateProperty(property: Property) = withContext(Dispatchers.IO) {
        val updated = property.copy(updatedAt = System.currentTimeMillis())
        propertyDao.updateProperty(updated)

        val api = getActiveApiService()
        val helper = supabaseHelper
        if (api != null && helper != null && helper.isConfigured) {
            try {
                val updates = mapOf(
                    "title" to updated.title,
                    "description" to updated.description,
                    "property_type" to updated.propertyType.name,
                    "rent" to updated.rent,
                    "security_deposit" to updated.securityDeposit,
                    "area_sqft" to updated.areaSqft,
                    "bedrooms" to updated.bedrooms,
                    "bathrooms" to updated.bathrooms,
                    "address" to updated.address,
                    "locality" to updated.locality,
                    "city" to updated.city,
                    "latitude" to updated.latitude,
                    "longitude" to updated.longitude,
                    "furnishing_status" to updated.furnishingStatus.name,
                    "has_parking" to updated.hasParking,
                    "water_supply" to updated.waterSupply.name,
                    "has_electricity_backup" to updated.hasElectricityBackup,
                    "has_balcony" to updated.hasBalcony,
                    "contact_phone" to updated.contactPhone,
                    "whatsapp_number" to updated.whatsappNumber,
                    "status" to updated.status.name,
                    "updated_at" to updated.updatedAt
                )
                api.updateProperty(
                    apiKey = helper.supabaseAnonKey,
                    authHeader = "Bearer ${helper.supabaseAnonKey}",
                    idFilter = "eq.${updated.id}",
                    propertyUpdates = updates
                )
            } catch (e: Exception) {
                Log.w("PropertyRepository", "Remote sync for update failed: ${e.message}")
            }
        }
    }

    suspend fun updatePropertyStatus(id: String, status: PropertyStatus) = withContext(Dispatchers.IO) {
        val timestamp = System.currentTimeMillis()
        propertyDao.updateStatus(id, status, timestamp)

        val api = getActiveApiService()
        val helper = supabaseHelper
        if (api != null && helper != null && helper.isConfigured) {
            try {
                api.updateProperty(
                    apiKey = helper.supabaseAnonKey,
                    authHeader = "Bearer ${helper.supabaseAnonKey}",
                    idFilter = "eq.$id",
                    propertyUpdates = mapOf(
                        "status" to status.name,
                        "updated_at" to timestamp
                    )
                )
            } catch (e: Exception) {
                Log.w("PropertyRepository", "Remote sync for status failed: ${e.message}")
            }
        }
    }

    suspend fun deleteProperty(property: Property) = withContext(Dispatchers.IO) {
        deletePropertyById(property.id)
    }

    suspend fun deletePropertyById(id: String) = withContext(Dispatchers.IO) {
        propertyDao.deletePropertyById(id)

        val api = getActiveApiService()
        val helper = supabaseHelper
        if (api != null && helper != null && helper.isConfigured) {
            try {
                api.deleteProperty(
                    apiKey = helper.supabaseAnonKey,
                    authHeader = "Bearer ${helper.supabaseAnonKey}",
                    idFilter = "eq.$id"
                )
            } catch (e: Exception) {
                Log.w("PropertyRepository", "Remote sync for delete failed: ${e.message}")
            }
        }
    }

    // ==========================================
    // FAVORITES (LOCAL SSOT & REMOTE SYNC)
    // ==========================================

    fun getFavoritePropertyIdsFlow(userId: String): Flow<List<String>> =
        favoriteDao.getFavoritePropertyIdsFlow(userId)

    fun getFavoritePropertiesFlow(userId: String): Flow<List<Property>> =
        favoriteDao.getFavoritePropertiesFlow(userId)

    fun isFavoriteFlow(userId: String, propertyId: String): Flow<Boolean> =
        favoriteDao.isFavoriteFlow(userId, propertyId)

    suspend fun toggleFavorite(userId: String, propertyId: String, currentIsFav: Boolean) = withContext(Dispatchers.IO) {
        if (currentIsFav) {
            favoriteDao.removeFavorite(userId, propertyId)
        } else {
            favoriteDao.addFavorite(Favorite(userId = userId, propertyId = propertyId))
        }

        val api = getActiveApiService()
        val helper = supabaseHelper
        if (api != null && helper != null && helper.isConfigured) {
            try {
                if (currentIsFav) {
                    api.removeFavorite(
                        apiKey = helper.supabaseAnonKey,
                        authHeader = "Bearer ${helper.supabaseAnonKey}",
                        userIdFilter = "eq.$userId",
                        propertyIdFilter = "eq.$propertyId"
                    )
                } else {
                    api.addFavorite(
                        apiKey = helper.supabaseAnonKey,
                        authHeader = "Bearer ${helper.supabaseAnonKey}",
                        favorite = FavoriteDto(userId = userId, propertyId = propertyId)
                    )
                }
            } catch (e: Exception) {
                Log.w("PropertyRepository", "Remote sync for favorite toggle failed: ${e.message}")
            }
        }
    }

    // ==========================================
    // DIRECT CACHE ACCESS
    // ==========================================

    suspend fun cacheProperties(properties: List<Property>) = withContext(Dispatchers.IO) {
        propertyDao.insertAll(properties)
    }

    suspend fun cacheProperty(property: Property) = withContext(Dispatchers.IO) {
        propertyDao.insertProperty(property)
    }

    fun observeCachedProperties(): Flow<List<Property>> =
        propertyDao.getAllPropertiesFlow()

    // ==========================================
    // LEAD CAPTURE MANAGEMENT
    // ==========================================

    suspend fun recordLead(lead: Lead) = withContext(Dispatchers.IO) {
        leadDao?.insertLead(lead)
    }

    fun getLeadsForPropertyFlow(propertyId: String): Flow<List<Lead>>? {
        return leadDao?.getLeadsForPropertyFlow(propertyId)
    }

    fun getLeadsForOwnerFlow(ownerId: String): Flow<List<Lead>>? {
        return leadDao?.getLeadsForOwnerFlow(ownerId)
    }

    fun getAllLeadsFlow(): Flow<List<Lead>>? {
        return leadDao?.getAllLeadsFlow()
    }
}
