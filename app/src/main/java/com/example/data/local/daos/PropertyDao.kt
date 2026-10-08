package com.example.data.local.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Property
import com.example.data.model.PropertyStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface PropertyDao {
    // --- CREATE ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProperty(property: Property)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(property: Property) = insertProperty(property)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(properties: List<Property>)

    // --- READ ---
    @Query("SELECT * FROM properties ORDER BY createdAt DESC")
    fun getAllProperties(): Flow<List<Property>>

    @Query("SELECT * FROM properties ORDER BY createdAt DESC")
    fun getAllPropertiesFlow(): Flow<List<Property>> = getAllProperties()

    @Query("SELECT * FROM properties WHERE status = 'AVAILABLE' ORDER BY createdAt DESC")
    fun getAvailablePropertiesFlow(): Flow<List<Property>>

    @Query("SELECT * FROM properties WHERE status = 'AVAILABLE' AND isFeatured = 1 ORDER BY createdAt DESC LIMIT 10")
    fun getFeaturedPropertiesFlow(): Flow<List<Property>>

    @Query("SELECT * FROM properties WHERE status = 'AVAILABLE' ORDER BY createdAt DESC LIMIT :limit")
    fun getRecentPropertiesFlow(limit: Int = 10): Flow<List<Property>>

    @Query("SELECT * FROM properties WHERE id = :id")
    suspend fun getPropertyById(id: String): Property?

    @Query("SELECT * FROM properties WHERE id = :id")
    fun getPropertyByIdFlow(id: String): Flow<Property?>

    @Query("SELECT * FROM properties WHERE ownerId = :ownerId ORDER BY createdAt DESC")
    fun getPropertiesByOwnerFlow(ownerId: String): Flow<List<Property>>

    @Query("SELECT DISTINCT city FROM properties WHERE city != '' ORDER BY city ASC")
    fun getAllCitiesFlow(): Flow<List<String>>

    @Query("SELECT COUNT(*) FROM properties")
    suspend fun countAll(): Int

    @Query("SELECT COUNT(*) FROM properties WHERE isSample = 1")
    suspend fun getSampleCount(): Int

    // --- UPDATE ---
    @Update
    suspend fun updateProperty(property: Property)

    @Update
    suspend fun update(property: Property) = updateProperty(property)

    @Query("UPDATE properties SET status = :status, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateStatus(id: String, status: PropertyStatus, updatedAt: Long = System.currentTimeMillis())

    // --- DELETE ---
    @Delete
    suspend fun deleteProperty(property: Property)

    @Delete
    suspend fun delete(property: Property) = deleteProperty(property)

    @Query("DELETE FROM properties WHERE id = :id")
    suspend fun deletePropertyById(id: String)

    @Query("DELETE FROM properties WHERE isSample = 1 OR ownerId IN ('owner-demo-id-101', 'tenant-demo-id-202') OR id LIKE 'prop-%'")
    suspend fun deleteDemoProperties()

    @Query("DELETE FROM properties WHERE isSample = 1")
    suspend fun deleteSampleProperties()

    @Query("DELETE FROM properties")
    suspend fun deleteAllProperties()
}
