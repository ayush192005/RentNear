package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.data.local.AppDatabase
import com.example.data.model.UserProfile
import com.example.data.model.UserRole
import com.example.data.remote.SupabaseApiService
import com.example.data.remote.SupabaseNetworkClient
import com.example.data.remote.dto.UserProfileDto
import com.example.data.supabase.SupabaseHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

class AuthRepository(
    private val db: AppDatabase,
    private val context: Context,
    private val supabaseHelper: SupabaseHelper? = null
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("rentnear_auth_prefs", Context.MODE_PRIVATE)

    private val _currentUser = MutableStateFlow<UserProfile>(UserProfile.EMPTY)
    val currentUser: StateFlow<UserProfile> = _currentUser.asStateFlow()

    private val _isUserLoggedIn = MutableStateFlow(prefs.getBoolean("is_user_logged_in", false))
    val isUserLoggedIn: StateFlow<Boolean> = _isUserLoggedIn.asStateFlow()

    private var remoteApiService: SupabaseApiService? = null
    private var lastUrl: String = ""

    private fun getActiveApiService(): SupabaseApiService? {
        val helper = supabaseHelper ?: return null
        if (!helper.isConfigured) return null
        if (remoteApiService == null || lastUrl != helper.supabaseUrl) {
            try {
                remoteApiService = SupabaseNetworkClient.createService(helper.supabaseUrl)
                lastUrl = helper.supabaseUrl
            } catch (e: Exception) {
                Log.e("AuthRepository", "Failed to create Supabase service: ${e.message}")
            }
        }
        return remoteApiService
    }

    init {
        CoroutineScope(Dispatchers.IO).launch {
            initAuth()
        }
    }

    private suspend fun initAuth() {
        val userDao = db.userDao()
        // Purge any demo accounts that may exist from earlier builds
        userDao.deleteUserById("owner-demo-id-101")
        userDao.deleteUserById("tenant-demo-id-202")
        userDao.deleteDemoUsers()

        val isLoggedIn = prefs.getBoolean("is_user_logged_in", false)
        val savedUserId = prefs.getString("active_user_id", null)

        if (isLoggedIn && !savedUserId.isNullOrBlank()) {
            val user = userDao.getUserById(savedUserId)
            if (user != null) {
                _currentUser.value = user
                _isUserLoggedIn.value = true
                // Sync latest profile from Supabase if online
                syncProfileFromCloud(user.id)
                return
            }
        }

        // Clean slate: not logged in
        prefs.edit()
            .putBoolean("is_user_logged_in", false)
            .remove("active_user_id")
            .apply()
        _currentUser.value = UserProfile.EMPTY
        _isUserLoggedIn.value = false
    }

    private suspend fun syncProfileFromCloud(userId: String) = withContext(Dispatchers.IO) {
        val helper = supabaseHelper ?: return@withContext
        val api = getActiveApiService() ?: return@withContext
        try {
            val profiles = api.getProfile(
                apiKey = helper.supabaseAnonKey,
                authHeader = "Bearer ${helper.supabaseAnonKey}",
                idFilter = "eq.$userId"
            )
            val cloudProfile = profiles.firstOrNull()?.toDomain()
            if (cloudProfile != null) {
                db.userDao().insertUser(cloudProfile)
                _currentUser.value = cloudProfile
            }
        } catch (e: Exception) {
            Log.w("AuthRepository", "Failed to sync profile from cloud: ${e.message}")
        }
    }

    suspend fun login(email: String, password: String): Result<UserProfile> = withContext(Dispatchers.IO) {
        val trimmedEmail = email.trim().lowercase()
        if (trimmedEmail.isBlank() || password.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("Email and password cannot be empty"))
        }

        val helper = supabaseHelper
        val api = getActiveApiService()

        // 1. First check Supabase PostgreSQL profiles table
        if (api != null && helper != null && helper.isConfigured) {
            try {
                val cloudProfiles = api.getProfileByEmail(
                    apiKey = helper.supabaseAnonKey,
                    authHeader = "Bearer ${helper.supabaseAnonKey}",
                    emailFilter = "eq.$trimmedEmail"
                )
                val cloudUser = cloudProfiles.firstOrNull()?.toDomain()
                if (cloudUser != null) {
                    db.userDao().insertUser(cloudUser)
                    setActiveUser(cloudUser, isLoggedIn = true)
                    return@withContext Result.success(cloudUser)
                }
            } catch (e: Exception) {
                Log.w("AuthRepository", "Supabase profile search error: ${e.message}")
            }
        }

        // 2. Check local database
        val localUser = db.userDao().getUserByEmail(trimmedEmail)
        if (localUser != null) {
            // Push to Supabase if not yet synced
            if (api != null && helper != null && helper.isConfigured) {
                try {
                    api.upsertProfile(
                        apiKey = helper.supabaseAnonKey,
                        authHeader = "Bearer ${helper.supabaseAnonKey}",
                        profile = UserProfileDto.fromDomain(localUser)
                    )
                } catch (e: Exception) {
                    Log.w("AuthRepository", "Failed to push local user to Supabase: ${e.message}")
                }
            }
            setActiveUser(localUser, isLoggedIn = true)
            return@withContext Result.success(localUser)
        }

        // 3. User does not exist: create account seamlessly on first login
        val isOwner = trimmedEmail.contains("owner", ignoreCase = true)
        val name = trimmedEmail.substringBefore("@").replace(".", " ").replaceFirstChar { it.uppercase() }
        val newUser = UserProfile(
            id = UUID.randomUUID().toString(),
            email = trimmedEmail,
            fullName = name,
            role = if (isOwner) UserRole.OWNER else UserRole.TENANT
        )

        if (api != null && helper != null && helper.isConfigured) {
            try {
                api.upsertProfile(
                    apiKey = helper.supabaseAnonKey,
                    authHeader = "Bearer ${helper.supabaseAnonKey}",
                    profile = UserProfileDto.fromDomain(newUser)
                )
            } catch (e: Exception) {
                Log.w("AuthRepository", "Failed to register new profile on Supabase: ${e.message}")
            }
        }

        db.userDao().insertUser(newUser)
        setActiveUser(newUser, isLoggedIn = true)
        Result.success(newUser)
    }

    suspend fun signup(
        email: String,
        password: String,
        fullName: String,
        phone: String,
        role: UserRole
    ): Result<UserProfile> = withContext(Dispatchers.IO) {
        val trimmedEmail = email.trim().lowercase()
        if (trimmedEmail.isBlank() || password.length < 6) {
            return@withContext Result.failure(IllegalArgumentException("Please provide a valid email and minimum 6-character password"))
        }
        if (fullName.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("Full name is required"))
        }

        val helper = supabaseHelper
        val api = getActiveApiService()

        // Check if account already exists on Supabase
        if (api != null && helper != null && helper.isConfigured) {
            try {
                val existingCloud = api.getProfileByEmail(
                    apiKey = helper.supabaseAnonKey,
                    authHeader = "Bearer ${helper.supabaseAnonKey}",
                    emailFilter = "eq.$trimmedEmail"
                )
                if (existingCloud.isNotEmpty()) {
                    val existing = existingCloud.first().toDomain()
                    db.userDao().insertUser(existing)
                    setActiveUser(existing, isLoggedIn = true)
                    return@withContext Result.success(existing)
                }
            } catch (e: Exception) {
                Log.w("AuthRepository", "Check existing profile error: ${e.message}")
            }
        }

        val existing = db.userDao().getUserByEmail(trimmedEmail)
        if (existing != null) {
            return@withContext Result.failure(IllegalArgumentException("An account with this email already exists. Please login."))
        }

        val newUser = UserProfile(
            id = UUID.randomUUID().toString(),
            email = trimmedEmail,
            fullName = fullName.trim(),
            phone = phone.trim(),
            role = role
        )

        // Upsert to Supabase PostgreSQL
        if (api != null && helper != null && helper.isConfigured) {
            try {
                api.upsertProfile(
                    apiKey = helper.supabaseAnonKey,
                    authHeader = "Bearer ${helper.supabaseAnonKey}",
                    profile = UserProfileDto.fromDomain(newUser)
                )
            } catch (e: Exception) {
                Log.e("AuthRepository", "Failed to sync new signup to Supabase: ${e.message}")
            }
        }

        db.userDao().insertUser(newUser)
        setActiveUser(newUser, isLoggedIn = true)
        Result.success(newUser)
    }

    suspend fun updateProfile(fullName: String, phone: String, role: UserRole): Result<UserProfile> = withContext(Dispatchers.IO) {
        val current = _currentUser.value
        val updated = current.copy(
            fullName = fullName.trim(),
            phone = phone.trim(),
            role = role
        )

        val helper = supabaseHelper
        val api = getActiveApiService()
        if (api != null && helper != null && helper.isConfigured) {
            try {
                api.upsertProfile(
                    apiKey = helper.supabaseAnonKey,
                    authHeader = "Bearer ${helper.supabaseAnonKey}",
                    profile = UserProfileDto.fromDomain(updated)
                )
            } catch (e: Exception) {
                Log.w("AuthRepository", "Failed to update profile on Supabase: ${e.message}")
            }
        }

        db.userDao().updateUser(updated)
        setActiveUser(updated, isLoggedIn = true)
        Result.success(updated)
    }

    fun logout() {
        CoroutineScope(Dispatchers.IO).launch {
            prefs.edit()
                .putBoolean("is_user_logged_in", false)
                .remove("active_user_id")
                .apply()
            _isUserLoggedIn.value = false
            _currentUser.value = UserProfile.EMPTY
        }
    }

    private fun setActiveUser(user: UserProfile, isLoggedIn: Boolean = true) {
        prefs.edit()
            .putString("active_user_id", user.id)
            .putBoolean("is_user_logged_in", isLoggedIn)
            .apply()
        _currentUser.value = user
        _isUserLoggedIn.value = isLoggedIn
    }
}
