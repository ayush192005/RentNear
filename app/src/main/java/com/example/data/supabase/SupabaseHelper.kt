package com.example.data.supabase

import android.content.Context
import android.content.SharedPreferences
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

class SupabaseHelper(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("rentnear_supabase_prefs", Context.MODE_PRIVATE)

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    var supabaseUrl: String
        get() {
            val saved = prefs.getString("supabase_url", "")?.trim() ?: ""
            if (saved.isNotBlank()) return saved
            return try {
                val buildConfigUrl = BuildConfig.SUPABASE_URL.trim()
                if (buildConfigUrl.startsWith("http") && !buildConfigUrl.contains("your-project")) buildConfigUrl else ""
            } catch (_: Exception) {
                ""
            }
        }
        set(value) = prefs.edit().putString("supabase_url", value.trim().removeSuffix("/")).apply()

    var supabaseAnonKey: String
        get() {
            val saved = prefs.getString("supabase_anon_key", "")?.trim() ?: ""
            if (saved.isNotBlank()) return saved
            return try {
                val buildConfigKey = BuildConfig.SUPABASE_ANON_KEY.trim()
                if (buildConfigKey.isNotBlank() && !buildConfigKey.startsWith("your-")) buildConfigKey else ""
            } catch (_: Exception) {
                ""
            }
        }
        set(value) = prefs.edit().putString("supabase_anon_key", value.trim()).apply()

    val isConfigured: Boolean
        get() = supabaseUrl.isNotBlank() && supabaseAnonKey.isNotBlank()

    fun setCredentials(url: String, key: String) {
        prefs.edit()
            .putString("supabase_url", url.trim().removeSuffix("/"))
            .putString("supabase_anon_key", key.trim())
            .apply()
    }

    suspend fun testConnection(): Result<String> = withContext(Dispatchers.IO) {
        if (!isConfigured) {
            return@withContext Result.failure(IllegalStateException("Supabase URL or Anon Key is missing. Please configure credentials."))
        }

        try {
            val cleanUrl = supabaseUrl.removeSuffix("/")
            val request = Request.Builder()
                .url("$cleanUrl/rest/v1/properties?select=id&limit=1")
                .addHeader("apikey", supabaseAnonKey)
                .addHeader("Authorization", "Bearer $supabaseAnonKey")
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                Result.success("Connected to Supabase PostgreSQL properties table successfully!")
            } else {
                val errorBody = response.body?.string() ?: ""
                Result.failure(Exception("Supabase response HTTP ${response.code}: $errorBody"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
