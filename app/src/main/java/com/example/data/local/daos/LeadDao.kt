package com.example.data.local.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.Lead
import kotlinx.coroutines.flow.Flow

@Dao
interface LeadDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLead(lead: Lead)

    @Query("SELECT * FROM leads ORDER BY timestamp DESC")
    fun getAllLeadsFlow(): Flow<List<Lead>>

    @Query("SELECT * FROM leads WHERE propertyId = :propertyId ORDER BY timestamp DESC")
    fun getLeadsForPropertyFlow(propertyId: String): Flow<List<Lead>>

    @Query("SELECT * FROM leads WHERE ownerId = :ownerId ORDER BY timestamp DESC")
    fun getLeadsForOwnerFlow(ownerId: String): Flow<List<Lead>>

    @Query("SELECT COUNT(*) FROM leads")
    suspend fun countLeads(): Int
}
