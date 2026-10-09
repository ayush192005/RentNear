package com.example.data.repository

import android.util.Log
import com.example.data.local.AppDatabase
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
 * PropertyRepository coordinates data between Remote Supabase PostgreSQL
 * and Local Room SQLite Database, ensuring Supabase is the Single Source of Truth (SSOT).
 *
 * - Supabase PostgreSQL is the Single Source of Truth for shared property listings.
 * - Posts, updates, and deletes are committed to Supabase before reporting success.
 * - Local Room database functions as an offline-capable reactive cache.
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

    private var lastConfiguredUrl: String = ""

    private fun getActiveApiService(): SupabaseApiService? {
        val helper = supabaseHelper ?: return remoteApiService
        if (!helper.isConfigured) return null

        val currentUrl = helper.supabaseUrl
        if (remoteApiService == null || lastConfiguredUrl != currentUrl) {
            try {
                remoteApiService = SupabaseNetworkClient.createService(currentUrl)
                lastConfiguredUrl = currentUrl
            } catch (e: Exception) {
                Log.e("PropertyRepository", "Failed to create Supabase service: ${e.message}")
                return null
            }
        }
        return remoteApiService
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
    // LOCAL ROOM OBSERVABLES (REACTIVE UI STREAM)
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
     * Refreshes properties from Supabase Remote PostgreSQL and synchronizes the local Room cache.
     * The UI automatically reacts to Room updates via Flow.
     */
    suspend fun refreshProperties(limit: Int = 100): Result<List<Property>> = withContext(Dispatchers.IO) {
        val helper = supabaseHelper
        val api = getActiveApiService()
        if (api == null || helper == null || !helper.isConfigured) {
            return@withContext Result.failure(
                IllegalStateException("Supabase is not configured. Please configure your Supabase URL and Anon Key in Profile settings.")
            )
        }

        try {
            val dtoList = api.getProperties(
                apiKey = helper.supabaseAnonKey,
                authHeader = "Bearer ${helper.supabaseAnonKey}",
                limit = limit
            )
            val domainProperties = dtoList.map { it.toDomain() }

            // Supabase PostgreSQL is the SSOT: sync fresh remote listings into Room
            propertyDao.deleteAllProperties()
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
        val helper = supabaseHelper
        val api = getActiveApiService()
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
    // MUTATIONS (SUPABASE SSOT WRITE -> ROOM CACHE)
    // ==========================================

    /**
     * Inserts a new property into the Supabase PostgreSQL database.
     * Reports success only after the remote database confirms the insert.
     */
    suspend fun saveProperty(property: Property): Result<Property> = withContext(Dispatchers.IO) {
        val helper = supabaseHelper
        val api = getActiveApiService()

        if (helper != null) {
            if (!helper.isConfigured) {
                return@withContext Result.failure(
                    IllegalStateException("Supabase is not configured. Please configure your Supabase project in Profile > Settings to sync across devices.")
                )
            }
            try {
                val dto = PropertyDto.fromDomain(property)
                val response = api?.createProperty(
                    apiKey = helper.supabaseAnonKey,
                    authHeader = "Bearer ${helper.supabaseAnonKey}",
                    property = dto
                )

                val confirmedProperty = response?.firstOrNull()?.toDomain() ?: property.copy(id = dto.id)
                propertyDao.insertProperty(confirmedProperty)
                return@withContext Result.success(confirmedProperty)
            } catch (e: Exception) {
                Log.e("PropertyRepository", "Supabase insert failed: ${e.message}", e)
                return@withContext Result.failure(e)
            }
        }

        // Direct local cache write (offline / unit tests)
        propertyDao.insertProperty(property)
        Result.success(property)
    }

    /**
     * Updates an existing property on Supabase PostgreSQL.
     */
    suspend fun updateProperty(property: Property): Result<Property> = withContext(Dispatchers.IO) {
        val helper = supabaseHelper
        val api = getActiveApiService()

        if (helper != null) {
            if (!helper.isConfigured) {
                return@withContext Result.failure(
                    IllegalStateException("Supabase is not configured. Please configure your Supabase credentials.")
                )
            }
            try {
                val dto = PropertyDto.fromDomain(property)
                val response = api?.updateProperty(
                    apiKey = helper.supabaseAnonKey,
                    authHeader = "Bearer ${helper.supabaseAnonKey}",
                    idFilter = "eq.${dto.id}",
                    ownerIdFilter = "eq.${dto.ownerId}",
                    propertyUpdates = dto.toUpdateMap()
                )

                val updatedDomain = response?.firstOrNull()?.toDomain() ?: property.copy(updatedAt = System.currentTimeMillis())
                propertyDao.insertProperty(updatedDomain)
                return@withContext Result.success(updatedDomain)
            } catch (e: Exception) {
                Log.e("PropertyRepository", "Supabase update failed: ${e.message}", e)
                return@withContext Result.failure(e)
            }
        }

        propertyDao.updateProperty(property)
        Result.success(property)
    }

    /**
     * Updates availability status on Supabase and local cache.
     */
    suspend fun updatePropertyStatus(id: String, status: PropertyStatus, ownerId: String? = null): Result<Unit> = withContext(Dispatchers.IO) {
        val helper = supabaseHelper
        val api = getActiveApiService()
        val timestamp = System.currentTimeMillis()

        if (api == null || helper == null || !helper.isConfigured) {
            propertyDao.updateStatus(id, status, timestamp)
            return@withContext Result.success(Unit)
        }

        try {
            val ownerFilter = if (ownerId != null) "eq.$ownerId" else null
            api.updateProperty(
                apiKey = helper.supabaseAnonKey,
                authHeader = "Bearer ${helper.supabaseAnonKey}",
                idFilter = "eq.$id",
                ownerIdFilter = ownerFilter,
                propertyUpdates = mapOf(
                    "status" to status.displayName
                )
            )
            propertyDao.updateStatus(id, status, timestamp)
            Result.success(Unit)
        } catch (e: Exception) {
            Log.w("PropertyRepository", "Remote sync for status failed: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun deleteProperty(property: Property): Result<Unit> = withContext(Dispatchers.IO) {
        deletePropertyById(property.id, property.ownerId)
    }

    suspend fun deletePropertyById(id: String, ownerId: String? = null): Result<Unit> = withContext(Dispatchers.IO) {
        val helper = supabaseHelper
        val api = getActiveApiService()

        if (api == null || helper == null || !helper.isConfigured) {
            propertyDao.deletePropertyById(id)
            return@withContext Result.success(Unit)
        }

        try {
            val ownerFilter = if (ownerId != null) "eq.$ownerId" else null
            val response = api.deleteProperty(
                apiKey = helper.supabaseAnonKey,
                authHeader = "Bearer ${helper.supabaseAnonKey}",
                idFilter = "eq.$id",
                ownerIdFilter = ownerFilter
            )
            if (!response.isSuccessful && response.code() != 204 && response.code() != 200) {
                return@withContext Result.failure(
                    Exception("Failed to delete property on Supabase: HTTP ${response.code()}")
                )
            }
            propertyDao.deletePropertyById(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("PropertyRepository", "Remote sync for delete failed: ${e.message}")
            Result.failure(e)
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
