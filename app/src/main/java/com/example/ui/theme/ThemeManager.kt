package com.example.ui.theme

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class AppThemeMode {
    LIGHT,
    DARK,
    SYSTEM
}

/**
 * Manages light / dark theme modes across the entire RentNear app.
 * Persists selection in SharedPreferences for seamless user experience.
 */
object ThemeManager {
    private const val PREFS_NAME = "rentnear_theme_prefs"
    private const val KEY_THEME = "app_theme_mode"
    private var prefs: SharedPreferences? = null

    private val _themeMode = MutableStateFlow(AppThemeMode.LIGHT)
    val themeMode: StateFlow<AppThemeMode> = _themeMode.asStateFlow()

    fun init(context: Context) {
        val sp = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs = sp
        val saved = sp.getString(KEY_THEME, AppThemeMode.LIGHT.name) ?: AppThemeMode.LIGHT.name
        _themeMode.value = runCatching { AppThemeMode.valueOf(saved) }.getOrDefault(AppThemeMode.LIGHT)
    }

    fun setThemeMode(mode: AppThemeMode) {
        _themeMode.value = mode
        prefs?.edit()?.putString(KEY_THEME, mode.name)?.apply()
    }

    fun toggleDarkMode() {
        val next = if (_themeMode.value == AppThemeMode.DARK) AppThemeMode.LIGHT else AppThemeMode.DARK
        setThemeMode(next)
    }
}
