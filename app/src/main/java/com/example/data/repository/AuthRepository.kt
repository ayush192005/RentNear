package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.local.AppDatabase
import com.example.data.model.UserProfile
import com.example.data.model.UserRole
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class AuthRepository(
    private val db: AppDatabase,
    private val context: Context
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("rentnear_auth_prefs", Context.MODE_PRIVATE)

    private val _currentUser = MutableStateFlow<UserProfile>(UserProfile.EMPTY)
    val currentUser: StateFlow<UserProfile> = _currentUser.asStateFlow()

    private val _isUserLoggedIn = MutableStateFlow(prefs.getBoolean("is_user_logged_in", false))
    val isUserLoggedIn: StateFlow<Boolean> = _isUserLoggedIn.asStateFlow()

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

    suspend fun login(email: String, password: String): Result<UserProfile> {
        val trimmedEmail = email.trim()
        if (trimmedEmail.isBlank() || password.isBlank()) {
            return Result.failure(IllegalArgumentException("Email and password cannot be empty"))
        }

        val user = db.userDao().getUserByEmail(trimmedEmail)
        return if (user != null) {
            setActiveUser(user, isLoggedIn = true)
            Result.success(user)
        } else {
            // If user doesn't exist, create profile on first login for seamless onboarding
            val isOwner = trimmedEmail.contains("owner", ignoreCase = true)
            val name = trimmedEmail.substringBefore("@").replace(".", " ").replaceFirstChar { it.uppercase() }
            val newUser = UserProfile(
                id = UUID.randomUUID().toString(),
                email = trimmedEmail,
                fullName = name,
                role = if (isOwner) UserRole.OWNER else UserRole.TENANT
            )
            db.userDao().insertUser(newUser)
            setActiveUser(newUser, isLoggedIn = true)
            Result.success(newUser)
        }
    }

    suspend fun signup(
        email: String,
        password: String,
        fullName: String,
        phone: String,
        role: UserRole
    ): Result<UserProfile> {
        val trimmedEmail = email.trim()
        if (trimmedEmail.isBlank() || password.length < 6) {
            return Result.failure(IllegalArgumentException("Please provide a valid email and minimum 6-character password"))
        }
        if (fullName.isBlank()) {
            return Result.failure(IllegalArgumentException("Full name is required"))
        }

        val existing = db.userDao().getUserByEmail(trimmedEmail)
        if (existing != null) {
            return Result.failure(IllegalArgumentException("An account with this email already exists. Please login."))
        }

        val newUser = UserProfile(
            id = UUID.randomUUID().toString(),
            email = trimmedEmail,
            fullName = fullName.trim(),
            phone = phone.trim(),
            role = role
        )
        db.userDao().insertUser(newUser)
        setActiveUser(newUser, isLoggedIn = true)
        return Result.success(newUser)
    }

    suspend fun updateProfile(fullName: String, phone: String, role: UserRole): Result<UserProfile> {
        val current = _currentUser.value
        val updated = current.copy(
            fullName = fullName.trim(),
            phone = phone.trim(),
            role = role
        )
        db.userDao().updateUser(updated)
        setActiveUser(updated, isLoggedIn = true)
        return Result.success(updated)
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
