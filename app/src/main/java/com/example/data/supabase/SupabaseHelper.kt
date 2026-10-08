package com.example.data.supabase

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

class SupabaseHelper(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("rentnear_supabase_prefs", Context.MODE_PRIVATE)

    private val client = OkHttpClient.Builder()
        .connectTimeout(8, TimeUnit.SECONDS)
        .readTimeout(8, TimeUnit.SECONDS)
        .build()

    var supabaseUrl: String
        get() = prefs.getString("supabase_url", "") ?: ""
        set(value) = prefs.edit().putString("supabase_url", value.trim()).apply()

    var supabaseAnonKey: String
        get() = prefs.getString("supabase_anon_key", "") ?: ""
        set(value) = prefs.edit().putString("supabase_anon_key", value.trim()).apply()

    val isConfigured: Boolean
        get() = supabaseUrl.isNotBlank() && supabaseAnonKey.isNotBlank()

    suspend fun testConnection(): Result<String> = withContext(Dispatchers.IO) {
        if (!isConfigured) {
            return@withContext Result.failure(IllegalStateException("Supabase URL or Anon Key is missing"))
        }

        try {
            val cleanUrl = supabaseUrl.removeSuffix("/")
            val request = Request.Builder()
                .url("$cleanUrl/rest/v1/properties?select=count")
                .addHeader("apikey", supabaseAnonKey)
                .addHeader("Authorization", "Bearer $supabaseAnonKey")
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                Result.success("Connected to Supabase PostgreSQL successfully!")
            } else {
                Result.failure(Exception("Supabase response code: ${response.code} - ${response.message}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
