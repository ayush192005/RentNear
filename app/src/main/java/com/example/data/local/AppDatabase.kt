package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.data.local.daos.PropertyDao
import com.example.data.local.daos.FavoriteDao
import com.example.data.local.daos.LeadDao
import com.example.data.local.entities.Favorite
import com.example.data.model.Lead
import com.example.data.model.Property
import com.example.data.model.UserProfile

@Database(
    entities = [
        Property::class,
        UserProfile::class,
        Favorite::class,
        Lead::class
    ],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun propertyDao(): PropertyDao
    abstract fun favoriteDao(): FavoriteDao
    abstract fun userDao(): UserDao
    abstract fun leadDao(): LeadDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "rentnear_database.db"
                )
                    .fallbackToDestructiveMigration(true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
