package com.example.data.ai

import android.util.Log
import com.example.BuildConfig
import com.example.data.model.FurnishingStatus
import com.example.data.model.PropertyFilter
import com.example.data.model.PropertyType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

data class AiSearchResult(
    val filter: PropertyFilter,
    val interpretation: String,
    val isAiPowered: Boolean = true
)

class GeminiSearchService(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()
) {
    companion object {
        private const val TAG = "GeminiSearchService"
        // Use gemini-2.5-flash as prescribed in skill
        private const val GEMINI_MODEL = "gemini-2.5-flash"
    }

    suspend fun parseNaturalLanguageQuery(query: String): AiSearchResult = withContext(Dispatchers.IO) {
        val trimmed = query.trim()
        if (trimmed.isBlank()) {
            return@withContext AiSearchResult(
                filter = PropertyFilter(),
                interpretation = "All available properties",
                isAiPowered = false
            )
        }

        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (_: Exception) {
            ""
        }

        // If API key is present and valid, attempt Gemini call
        if (!apiKey.isNullOrBlank() && !apiKey.startsWith("MY_") && apiKey.length > 10) {
            try {
                val geminiResult = callGeminiApi(trimmed, apiKey)
                if (geminiResult != null) {
                    return@withContext geminiResult
                }
            } catch (e: Exception) {
                Log.w(TAG, "Gemini API call failed, falling back to local NLP engine: ${e.message}")
            }
        }

        // Fallback: Intelligent on-device NLP parser
        parseWithLocalNlp(trimmed)
    }

    private fun callGeminiApi(userPrompt: String, apiKey: String): AiSearchResult? {
        val url = "https://generativelanguage.googleapis.com/v1beta/models/$GEMINI_MODEL:generateContent?key=$apiKey"

        val systemInstruction = """
            You are a real-estate search query parser for RentNear.
            Extract structured search filters from the user's natural language request.
            Respond strictly with valid JSON only, without markdown formatting or codeblocks.
            Schema:
            {
               "property_type": "FLAT" | "HOUSE" | "ROOM" | "SHOP" | "OFFICE" | "PG" | null,
               "bedrooms": number | null,
               "max_price": number | null,
               "min_price": number | null,
               "location": string | null,
               "parking": boolean | null,
               "furnishing": "FURNISHED" | "SEMI_FURNISHED" | "UNFURNISHED" | null,
               "explanation": string
            }
        """.trimIndent()

        val jsonBody = JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "user")
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", "Parse this rental property search: \"$userPrompt\"")
                        })
                    })
                })
            })
            put("systemInstruction", JSONObject().apply {
                put("parts", JSONArray().apply {
                    put(JSONObject().apply {
                        put("text", systemInstruction)
                    })
                })
            })
            put("generationConfig", JSONObject().apply {
                put("responseMimeType", "application/json")
                put("temperature", 0.1)
            })
        }

        val request = Request.Builder()
            .url(url)
            .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
            .build()

        val response = client.newCall(request).execute()
        if (!response.isSuccessful) {
            Log.e(TAG, "Gemini response error code: ${response.code}")
            return null
        }

        val responseBody = response.body?.string() ?: return null
        val root = JSONObject(responseBody)
        val candidates = root.optJSONArray("candidates") ?: return null
        if (candidates.length() == 0) return null
        val text = candidates.getJSONObject(0)
            .getJSONObject("content")
            .getJSONArray("parts")
            .getJSONObject(0)
            .getString("text")

        // Parse extracted JSON
        val cleanJson = text.trim().removePrefix("```json").removePrefix("```").removeSuffix("```").trim()
        val parsed = JSONObject(cleanJson)

        val propertyTypeStr = parsed.optString("property_type", "")
        val propType = if (propertyTypeStr.isNotBlank() && propertyTypeStr != "null") {
            try { PropertyType.valueOf(propertyTypeStr.uppercase()) } catch (_: Exception) { null }
        } else null

        val bedrooms = if (parsed.has("bedrooms") && !parsed.isNull("bedrooms")) parsed.getInt("bedrooms") else null
        val maxPrice = if (parsed.has("max_price") && !parsed.isNull("max_price")) parsed.getInt("max_price") else null
        val minPrice = if (parsed.has("min_price") && !parsed.isNull("min_price")) parsed.getInt("min_price") else null
        val location = if (parsed.has("location") && !parsed.isNull("location")) parsed.getString("location").takeIf { it != "null" && it.isNotBlank() } else null
        val parking = if (parsed.has("parking") && !parsed.isNull("parking")) parsed.getBoolean("parking") else null
        val furnishingStr = parsed.optString("furnishing", "")
        val furnishing = if (furnishingStr.isNotBlank() && furnishingStr != "null") {
            try { FurnishingStatus.valueOf(furnishingStr.uppercase()) } catch (_: Exception) { null }
        } else null
        val explanation = parsed.optString("explanation", "Searched based on AI criteria")

        val filter = PropertyFilter(
            searchQuery = "",
            city = location,
            propertyType = propType,
            bedrooms = bedrooms,
            maxRent = maxPrice,
            minRent = minPrice,
            hasParking = parking,
            furnishingStatus = furnishing,
            aiSearchPrompt = userPrompt
        )

        return AiSearchResult(filter = filter, interpretation = explanation, isAiPowered = true)
    }

    fun parseWithLocalNlp(query: String): AiSearchResult {
        val lower = query.lowercase()

        // 1. Bedrooms extraction: "2 BHK", "2 bedroom", "1 bed"
        var bedrooms: Int? = null
        val bhkPattern = Pattern.compile("(\\d+)\\s*(?:bhk|bed(?:room)?s?|room)", Pattern.CASE_INSENSITIVE)
        val bhkMatcher = bhkPattern.matcher(lower)
        if (bhkMatcher.find()) {
            bedrooms = bhkMatcher.group(1)?.toIntOrNull()
        }

        // 2. Max Price / Budget extraction: "under 12000", "below 15k", "max 20000", "under 12k"
        var maxPrice: Int? = null
        var minPrice: Int? = null
        val pricePattern = Pattern.compile("(?:under|below|less than|max(?:imum)?|within|budget of)\\s*(?:rs\\.?|inr|₹)?\\s*(\\d+)(k)?", Pattern.CASE_INSENSITIVE)
        val priceMatcher = pricePattern.matcher(lower)
        if (priceMatcher.find()) {
            val amountStr = priceMatcher.group(1) ?: "0"
            val isK = priceMatcher.group(2) != null
            val base = amountStr.toIntOrNull() ?: 0
            maxPrice = if (isK) base * 1000 else base
        }

        // 3. Location extraction: "near Kalka", "in Chandigarh", "at Sector 20"
        var location: String? = null
        val locPattern = Pattern.compile("(?:near|in|at|around)\\s+([a-zA-Z0-9\\s]{3,20}?)(?=\\s+(?:with|under|below|for|having|and|$))", Pattern.CASE_INSENSITIVE)
        val locMatcher = locPattern.matcher(lower)
        if (locMatcher.find()) {
            location = locMatcher.group(1)?.trim()?.replaceFirstChar { it.uppercase() }
        } else {
            // Direct city keyword check
            val commonCities = listOf("Kalka", "Chandigarh", "Panchkula", "Mohali", "Delhi", "Bengaluru", "Mumbai")
            for (c in commonCities) {
                if (lower.contains(c.lowercase())) {
                    location = c
                    break
                }
            }
        }

        // 4. Property type
        var propType: PropertyType? = null
        when {
            lower.contains("flat") || lower.contains("apartment") -> propType = PropertyType.FLAT
            lower.contains("house") || lower.contains("villa") || lower.contains("kothi") -> propType = PropertyType.HOUSE
            lower.contains("shop") || lower.contains("commercial") || lower.contains("retail") -> propType = PropertyType.SHOP
            lower.contains("office") -> propType = PropertyType.OFFICE
            lower.contains("pg") || lower.contains("paying guest") || lower.contains("hostel") -> propType = PropertyType.PG
            lower.contains("room") && !lower.contains("bedroom") -> propType = PropertyType.ROOM
        }

        // If bedrooms specified but no type, default to Flat/House
        if (propType == null && bedrooms != null) {
            propType = PropertyType.FLAT
        }

        // 5. Parking
        var hasParking: Boolean? = null
        if (lower.contains("parking") || lower.contains("garage")) {
            hasParking = true
        }

        // 6. Furnishing
        var furnishing: FurnishingStatus? = null
        when {
            lower.contains("fully furnished") || lower.contains("furnished") && !lower.contains("semi") && !lower.contains("unfurnished") -> furnishing = FurnishingStatus.FURNISHED
            lower.contains("semi-furnished") || lower.contains("semi furnished") -> furnishing = FurnishingStatus.SEMI_FURNISHED
            lower.contains("unfurnished") -> furnishing = FurnishingStatus.UNFURNISHED
        }

        // Build explanation string
        val parts = mutableListOf<String>()
        if (bedrooms != null) parts.add("$bedrooms BHK")
        if (propType != null) parts.add(propType.displayName)
        if (maxPrice != null) parts.add("Under ₹$maxPrice")
        if (!location.isNullOrBlank()) parts.add("Near $location")
        if (hasParking == true) parts.add("With Parking")
        if (furnishing != null) parts.add(furnishing.displayName)

        val explanation = if (parts.isNotEmpty()) parts.joinToString(" • ") else "Custom search"

        val filter = PropertyFilter(
            searchQuery = "",
            city = location,
            propertyType = propType,
            bedrooms = bedrooms,
            maxRent = maxPrice,
            minRent = minPrice,
            hasParking = hasParking,
            furnishingStatus = furnishing,
            aiSearchPrompt = query
        )

        return AiSearchResult(filter = filter, interpretation = explanation, isAiPowered = false)
    }
}
