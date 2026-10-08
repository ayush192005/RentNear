package com.example.data.local.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entities.Favorite
import com.example.data.model.Property
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {
    // --- INSERT ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: Favorite)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addFavorite(favorite: Favorite) = insertFavorite(favorite)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(favorites: List<Favorite>)

    // --- RETRIEVE FAVORITE PROPERTY IDS ---
    @Query("SELECT propertyId FROM favorites WHERE userId = :userId")
    fun getFavoritePropertyIdsFlow(userId: String): Flow<List<String>>

    @Query("SELECT propertyId FROM favorites WHERE userId = :userId")
    suspend fun getFavoritePropertyIds(userId: String): List<String>

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE userId = :userId AND propertyId = :propertyId)")
    fun isFavoriteFlow(userId: String, propertyId: String): Flow<Boolean>

    @Query("SELECT * FROM favorites WHERE userId = :userId AND propertyId = :propertyId LIMIT 1")
    suspend fun getFavorite(userId: String, propertyId: String): Favorite?

    @Query("SELECT * FROM favorites WHERE userId = :userId ORDER BY createdAt DESC")
    fun getAllFavoritesForUser(userId: String): Flow<List<Favorite>>

    @Query("""
        SELECT p.* FROM properties p
        INNER JOIN favorites f ON p.id = f.propertyId
        WHERE f.userId = :userId
        ORDER BY f.createdAt DESC
    """)
    fun getFavoritePropertiesFlow(userId: String): Flow<List<Property>>

    // --- DELETE ---
    @Delete
    suspend fun deleteFavorite(favorite: Favorite)

    @Query("DELETE FROM favorites WHERE userId = :userId AND propertyId = :propertyId")
    suspend fun removeFavorite(userId: String, propertyId: String)

    @Query("DELETE FROM favorites WHERE userId = :userId")
    suspend fun clearUserFavorites(userId: String)
}
