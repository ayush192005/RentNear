package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.UserProfile
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM profiles WHERE id = :id")
    suspend fun getUserById(id: String): UserProfile?

    @Query("SELECT * FROM profiles WHERE id = :id")
    fun getUserByIdFlow(id: String): Flow<UserProfile?>

    @Query("SELECT * FROM profiles WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserProfile?

    @Query("SELECT * FROM profiles ORDER BY createdAt ASC")
    fun getAllUsersFlow(): Flow<List<UserProfile>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserProfile)

    @Update
    suspend fun updateUser(user: UserProfile)

    @Query("SELECT COUNT(*) FROM profiles")
    suspend fun countUsers(): Int

    @Query("DELETE FROM profiles WHERE id = :id")
    suspend fun deleteUserById(id: String)

    @Query("DELETE FROM profiles WHERE email LIKE '%@rentnear.com' OR id IN ('owner-demo-id-101', 'tenant-demo-id-202')")
    suspend fun deleteDemoUsers()
}
