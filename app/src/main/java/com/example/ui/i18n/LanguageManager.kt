package com.example.ui.i18n

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class AppLanguage(val code: String, val displayName: String, val shortLabel: String) {
    ENGLISH("en", "English", "EN"),
    HINDI("hi", "हिंदी", "HI")
}

object LanguageManager {
    private const val PREFS_NAME = "rentnear_lang_prefs"
    private const val KEY_LANG = "selected_language"

    private var prefs: SharedPreferences? = null
    private val _currentLanguage = MutableStateFlow(AppLanguage.ENGLISH)
    val currentLanguage: StateFlow<AppLanguage> = _currentLanguage.asStateFlow()

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedCode = prefs?.getString(KEY_LANG, AppLanguage.ENGLISH.code) ?: AppLanguage.ENGLISH.code
        val lang = AppLanguage.entries.find { it.code == savedCode } ?: AppLanguage.ENGLISH
        _currentLanguage.value = lang
    }

    fun setLanguage(language: AppLanguage) {
        _currentLanguage.value = language
        prefs?.edit()?.putString(KEY_LANG, language.code)?.apply()
    }

    fun toggleLanguage() {
        val next = if (_currentLanguage.value == AppLanguage.ENGLISH) AppLanguage.HINDI else AppLanguage.ENGLISH
        setLanguage(next)
    }
}

/**
 * Clean bilingual strings lookup helper for UI composables.
 */
object AppStrings {
    fun appTitle(lang: AppLanguage) = if (lang == AppLanguage.HINDI) "रेंट नियर" else "RentNear"

    fun appTagline(lang: AppLanguage) = if (lang == AppLanguage.HINDI)
        "मकान, दुकान और कमरा आसानी से किराये पर पाएं"
    else
        "Find houses, shops & rooms for rent near you"

    fun searchPlaceholder(lang: AppLanguage) = if (lang == AppLanguage.HINDI)
        "मकान, दुकान, शहर या इलाका खोजें..."
    else
        "Search house, shop, room, city or locality..."

    fun popularLocations(lang: AppLanguage) = if (lang == AppLanguage.HINDI) "प्रमुख शहर" else "Popular Locations"
    fun propertyTypes(lang: AppLanguage) = if (lang == AppLanguage.HINDI) "प्रॉपर्टी का प्रकार" else "Property Type"
    fun featuredProperties(lang: AppLanguage) = if (lang == AppLanguage.HINDI) "खास मकान व दुकानें" else "Featured Properties"
    fun recentProperties(lang: AppLanguage) = if (lang == AppLanguage.HINDI) "हाल ही में जोड़ी गई प्रॉपर्टी" else "Recently Added Properties"
    fun verifiedReady(lang: AppLanguage) = if (lang == AppLanguage.HINDI) "जांची-परखी, रहने/खोलने के लिए तैयार" else "Verified properties ready to move in"
    fun seeAll(lang: AppLanguage) = if (lang == AppLanguage.HINDI) "सभी देखें" else "See All"

    // Property Types
    fun typeHouse(lang: AppLanguage) = if (lang == AppLanguage.HINDI) "मकान / घर" else "House"
    fun typeFlat(lang: AppLanguage) = if (lang == AppLanguage.HINDI) "फ्लैट" else "Flat"
    fun typeShop(lang: AppLanguage) = if (lang == AppLanguage.HINDI) "दुकान" else "Shop"
    fun typeRoom(lang: AppLanguage) = if (lang == AppLanguage.HINDI) "कमरा" else "Room"
    fun typeOffice(lang: AppLanguage) = if (lang == AppLanguage.HINDI) "ऑफिस" else "Office"
    fun typePg(lang: AppLanguage) = if (lang == AppLanguage.HINDI) "पीजी / हॉस्टल" else "PG / Hostel"
    fun typeOther(lang: AppLanguage) = if (lang == AppLanguage.HINDI) "अन्य" else "Other"

    // Statuses
    fun statusAvailable(lang: AppLanguage) = if (lang == AppLanguage.HINDI) "किराये पर उपलब्ध" else "AVAILABLE"
    fun statusRented(lang: AppLanguage) = if (lang == AppLanguage.HINDI) "किराये पर उठ चुका है" else "RENTED OUT"
    fun statusSold(lang: AppLanguage) = if (lang == AppLanguage.HINDI) "बिक चुका है" else "SOLD"

    // Actions & Buttons
    fun postProperty(lang: AppLanguage) = if (lang == AppLanguage.HINDI) "मकान / दुकान जोड़ें" else "Post House / Shop"
    fun ownerCtaTitle(lang: AppLanguage) = if (lang == AppLanguage.HINDI) "क्या आप मकान मालिक या दुकानदार हैं?" else "Are you a Property Owner?"
    fun ownerCtaDesc(lang: AppLanguage) = if (lang == AppLanguage.HINDI)
        "अपना मकान, दुकान, फ्लैट या कमरा मुफ्त में डालें और किरायेदारों से सीधे संपर्क पाएं।"
    else
        "Post your house, shop, flat or room for free and connect directly with tenants."

    fun callOwner(lang: AppLanguage) = if (lang == AppLanguage.HINDI) "मालिक को कॉल करें" else "Call Owner"
    fun whatsappOwner(lang: AppLanguage) = if (lang == AppLanguage.HINDI) "व्हाट्सएप करें" else "WhatsApp"
    fun viewDetails(lang: AppLanguage) = if (lang == AppLanguage.HINDI) "पूरी जानकारी देखें" else "View Details"
    fun editProperty(lang: AppLanguage) = if (lang == AppLanguage.HINDI) "बदलाव करें" else "Edit"
    fun deleteProperty(lang: AppLanguage) = if (lang == AppLanguage.HINDI) "हटाएं" else "Delete"
    fun markRented(lang: AppLanguage) = if (lang == AppLanguage.HINDI) "किराये पर उठ गया" else "Mark as Rented"
    fun markAvailable(lang: AppLanguage) = if (lang == AppLanguage.HINDI) "उपलब्ध मार्क करें" else "Mark as Available"

    // Labels & Details
    fun rentPerMonth(lang: AppLanguage) = if (lang == AppLanguage.HINDI) "/ माह" else "/ month"
    fun securityDeposit(lang: AppLanguage) = if (lang == AppLanguage.HINDI) "सिक्योरिटी डिपॉजिट" else "Security Deposit"
    fun area(lang: AppLanguage) = if (lang == AppLanguage.HINDI) "क्षेत्रफल" else "Area"
    fun address(lang: AppLanguage) = if (lang == AppLanguage.HINDI) "पता" else "Address"
    fun locality(lang: AppLanguage) = if (lang == AppLanguage.HINDI) "इलाका / कॉलोनी" else "Locality"
    fun city(lang: AppLanguage) = if (lang == AppLanguage.HINDI) "शहर" else "City"
    fun contactNumber(lang: AppLanguage) = if (lang == AppLanguage.HINDI) "फोन नंबर" else "Phone Number"
    fun whatsappNumber(lang: AppLanguage) = if (lang == AppLanguage.HINDI) "व्हाट्सएप नंबर" else "WhatsApp Number"
    fun description(lang: AppLanguage) = if (lang == AppLanguage.HINDI) "विवरण" else "Description"
    fun amenities(lang: AppLanguage) = if (lang == AppLanguage.HINDI) "सुविधाएं" else "Amenities"
    fun parking(lang: AppLanguage) = if (lang == AppLanguage.HINDI) "पार्किंग उपलब्ध" else "Parking Available"
    fun waterSupply(lang: AppLanguage) = if (lang == AppLanguage.HINDI) "पानी की व्यवस्था" else "Water Supply"
    fun saveListing(lang: AppLanguage) = if (lang == AppLanguage.HINDI) "प्रॉपर्टी प्रकाशित करें" else "Publish Property"
    fun updateListing(lang: AppLanguage) = if (lang == AppLanguage.HINDI) "बदलाव सेव करें" else "Save Changes"

    // Navigation Tabs
    fun tabExplore(lang: AppLanguage) = if (lang == AppLanguage.HINDI) "खोजें" else "Explore"
    fun tabSearch(lang: AppLanguage) = if (lang == AppLanguage.HINDI) "सर्च" else "Search"
    fun tabSaved(lang: AppLanguage) = if (lang == AppLanguage.HINDI) "पसंदीदा" else "Saved"
    fun tabOwner(lang: AppLanguage) = if (lang == AppLanguage.HINDI) "मालिक हब" else "Owner Hub"
    fun tabProfile(lang: AppLanguage) = if (lang == AppLanguage.HINDI) "प्रोफ़ाइल" else "Profile"
}
