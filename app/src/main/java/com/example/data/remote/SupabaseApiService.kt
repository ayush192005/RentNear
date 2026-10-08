package com.example.data.remote

import com.example.data.remote.dto.FavoriteDto
import com.example.data.remote.dto.PropertyDto
import com.example.data.remote.dto.UserProfileDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * Retrofit API Service interface for Supabase PostgREST endpoints.
 */
interface SupabaseApiService {

    // ==========================================
    // PROPERTIES ENDPOINTS
    // ==========================================

    @GET("rest/v1/properties")
    suspend fun getProperties(
        @Header("apikey") apiKey: String,
        @Header("Authorization") authHeader: String,
        @Query("select") select: String = "*",
        @Query("order") order: String = "created_at.desc",
        @Query("limit") limit: Int? = null
    ): List<PropertyDto>

    @GET("rest/v1/properties")
    suspend fun getPropertyById(
        @Header("apikey") apiKey: String,
        @Header("Authorization") authHeader: String,
        @Query("id") idFilter: String, // e.g. "eq.123-uuid"
        @Query("select") select: String = "*"
    ): List<PropertyDto>

    @GET("rest/v1/properties")
    suspend fun getPropertiesByOwner(
        @Header("apikey") apiKey: String,
        @Header("Authorization") authHeader: String,
        @Query("owner_id") ownerIdFilter: String, // e.g. "eq.owner-uuid"
        @Query("select") select: String = "*",
        @Query("order") order: String = "created_at.desc"
    ): List<PropertyDto>

    @POST("rest/v1/properties")
    suspend fun createProperty(
        @Header("apikey") apiKey: String,
        @Header("Authorization") authHeader: String,
        @Header("Prefer") prefer: String = "return=representation",
        @Body property: PropertyDto
    ): List<PropertyDto>

    @PATCH("rest/v1/properties")
    suspend fun updateProperty(
        @Header("apikey") apiKey: String,
        @Header("Authorization") authHeader: String,
        @Query("id") idFilter: String, // e.g. "eq.123-uuid"
        @Header("Prefer") prefer: String = "return=representation",
        @Body propertyUpdates: Map<String, @JvmSuppressWildcards Any?>
    ): List<PropertyDto>

    @DELETE("rest/v1/properties")
    suspend fun deleteProperty(
        @Header("apikey") apiKey: String,
        @Header("Authorization") authHeader: String,
        @Query("id") idFilter: String // e.g. "eq.123-uuid"
    ): Response<Unit>

    // ==========================================
    // FAVORITES ENDPOINTS
    // ==========================================

    @GET("rest/v1/favorites")
    suspend fun getFavoritesByUser(
        @Header("apikey") apiKey: String,
        @Header("Authorization") authHeader: String,
        @Query("user_id") userIdFilter: String, // e.g. "eq.user-uuid"
        @Query("select") select: String = "*"
    ): List<FavoriteDto>

    @POST("rest/v1/favorites")
    suspend fun addFavorite(
        @Header("apikey") apiKey: String,
        @Header("Authorization") authHeader: String,
        @Header("Prefer") prefer: String = "return=representation",
        @Body favorite: FavoriteDto
    ): List<FavoriteDto>

    @DELETE("rest/v1/favorites")
    suspend fun removeFavorite(
        @Header("apikey") apiKey: String,
        @Header("Authorization") authHeader: String,
        @Query("user_id") userIdFilter: String, // e.g. "eq.user-uuid"
        @Query("property_id") propertyIdFilter: String // e.g. "eq.prop-uuid"
    ): Response<Unit>

    // ==========================================
    // USER PROFILES ENDPOINTS
    // ==========================================

    @GET("rest/v1/profiles")
    suspend fun getProfile(
        @Header("apikey") apiKey: String,
        @Header("Authorization") authHeader: String,
        @Query("id") idFilter: String, // e.g. "eq.user-uuid"
        @Query("select") select: String = "*"
    ): List<UserProfileDto>

    @POST("rest/v1/profiles")
    suspend fun upsertProfile(
        @Header("apikey") apiKey: String,
        @Header("Authorization") authHeader: String,
        @Header("Prefer") prefer: String = "resolution=merge-duplicates,return=representation",
        @Body profile: UserProfileDto
    ): List<UserProfileDto>
}
