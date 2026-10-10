package com.example.di

import android.content.Context
import androidx.room.Room
import com.example.data.local.AppDatabase
import com.example.data.local.daos.FavoriteDao
import com.example.data.local.daos.PropertyDao
import com.example.data.local.UserDao
import com.example.data.repository.AuthRepository
import com.example.data.repository.PropertyRepository

/**
 * Dependency Injection Module for initializing and providing Room local storage components.
 */
object DatabaseModule {

    private const val DATABASE_NAME = "rentnear_database.db"

    @Volatile
    private var databaseInstance: AppDatabase? = null

    /**
     * Provides the singleton AppDatabase instance.
     */
    fun provideDatabase(context: Context): AppDatabase {
        return databaseInstance ?: synchronized(this) {
            databaseInstance ?: Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                DATABASE_NAME
            )
                .fallbackToDestructiveMigration(true)
                .build()
                .also { databaseInstance = it }
        }
    }

    /**
     * Provides the singleton AppDatabase instance (alias).
     */
    fun provideAppDatabase(context: Context): AppDatabase = provideDatabase(context)

    /**
     * Provides the PropertyDao instance.
     */
    fun providePropertyDao(database: AppDatabase): PropertyDao {
        return database.propertyDao()
    }

    /**
     * Provides the FavoriteDao instance.
     */
    fun provideFavoriteDao(database: AppDatabase): FavoriteDao {
        return database.favoriteDao()
    }

    /**
     * Provides the UserDao instance.
     */
    fun provideUserDao(database: AppDatabase): UserDao {
        return database.userDao()
    }

    /**
     * Provides the PropertyRepository instance using injected database.
     */
    fun providePropertyRepository(
        database: AppDatabase,
        remoteApiService: com.example.data.remote.SupabaseApiService? = null,
        supabaseHelper: com.example.data.supabase.SupabaseHelper? = null
    ): PropertyRepository {
        return PropertyRepository(database, remoteApiService, supabaseHelper)
    }

    /**
     * Provides the AuthRepository instance using injected database and context.
     */
    fun provideAuthRepository(
        database: AppDatabase,
        context: Context,
        supabaseHelper: com.example.data.supabase.SupabaseHelper? = null
    ): AuthRepository {
        return AuthRepository(database, context, supabaseHelper)
    }
}
