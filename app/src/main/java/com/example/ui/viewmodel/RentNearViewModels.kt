package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ai.GeminiSearchService
import com.example.data.local.AppDatabase
import com.example.data.model.Favorite
import com.example.data.model.Lead
import com.example.data.model.Property
import com.example.data.model.PropertyFilter
import com.example.data.model.PropertyStatus
import com.example.data.model.UserProfile
import com.example.data.model.UserRole
import com.example.data.repository.AuthRepository
import com.example.data.repository.PropertyRepository
import com.example.data.supabase.SupabaseHelper
import com.example.di.DatabaseModule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class RentNearViewModel(
    application: Application
) : AndroidViewModel(application) {
    val db: AppDatabase = DatabaseModule.provideDatabase(application)
    val supabaseHelper = SupabaseHelper(application)
    val propertyRepository = DatabaseModule.providePropertyRepository(
        database = db,
        supabaseHelper = supabaseHelper
    )
    val authRepository = DatabaseModule.provideAuthRepository(db, application)
    private val geminiSearchService = GeminiSearchService()

    // Auth State
    val currentUser: StateFlow<UserProfile> = authRepository.currentUser
    val isUserLoggedIn: StateFlow<Boolean> = authRepository.isUserLoggedIn

    // Filter State
    private val _currentFilter = MutableStateFlow(PropertyFilter())
    val currentFilter: StateFlow<PropertyFilter> = _currentFilter.asStateFlow()

    // AI Search State
    private val _isAiSearching = MutableStateFlow(false)
    val isAiSearching: StateFlow<Boolean> = _isAiSearching.asStateFlow()

    private val _aiInterpretation = MutableStateFlow<String?>(null)
    val aiInterpretation: StateFlow<String?> = _aiInterpretation.asStateFlow()

    // Reactive Properties list filtered
    @OptIn(ExperimentalCoroutinesApi::class)
    val filteredProperties: StateFlow<List<Property>> = _currentFilter
        .flatMapLatest { filter -> propertyRepository.filterPropertiesFlow(filter) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val featuredProperties: StateFlow<List<Property>> = propertyRepository
        .getFeaturedPropertiesFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentProperties: StateFlow<List<Property>> = propertyRepository
        .getRecentPropertiesFlow(10)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val availableCities: StateFlow<List<String>> = propertyRepository
        .getAllCitiesFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Owner's properties
    @OptIn(ExperimentalCoroutinesApi::class)
    val ownerProperties: StateFlow<List<Property>> = currentUser
        .flatMapLatest { user -> propertyRepository.getPropertiesByOwnerFlow(user.id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // User's favorites
    @OptIn(ExperimentalCoroutinesApi::class)
    val favoriteProperties: StateFlow<List<Property>> = currentUser
        .flatMapLatest { user -> propertyRepository.getFavoritePropertiesFlow(user.id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val favoriteIds: StateFlow<Set<String>> = currentUser
        .flatMapLatest { user -> propertyRepository.getFavoritePropertyIdsFlow(user.id) }
        .map { it.toSet() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    init {
        viewModelScope.launch {
            propertyRepository.ensureSeeded()
            // Single source of truth sync: pull latest remote properties to refresh local Room
            propertyRepository.refreshProperties()
        }
    }

    fun refreshRemoteData() {
        viewModelScope.launch {
            propertyRepository.refreshProperties()
        }
    }

    // Filter updates
    fun updateFilter(newFilter: PropertyFilter) {
        _currentFilter.value = newFilter
    }

    fun updateSearchQuery(query: String) {
        _currentFilter.value = _currentFilter.value.copy(searchQuery = query)
    }

    fun updateCityFilter(city: String?) {
        _currentFilter.value = _currentFilter.value.copy(city = city)
    }

    // AI Natural Language Search
    fun triggerAiSearch(query: String) {
        if (query.isBlank()) return
        viewModelScope.launch {
            _isAiSearching.value = true
            try {
                val result = geminiSearchService.parseNaturalLanguageQuery(query)
                _aiInterpretation.value = result.interpretation
                _currentFilter.value = result.filter
            } catch (e: Exception) {
                // Fallback local NLP
                val result = geminiSearchService.parseWithLocalNlp(query)
                _aiInterpretation.value = result.interpretation
                _currentFilter.value = result.filter
            } finally {
                _isAiSearching.value = false
            }
        }
    }

    fun clearAiSearch() {
        _aiInterpretation.value = null
        _currentFilter.value = PropertyFilter()
    }

    // Favorites
    fun toggleFavorite(propertyId: String) {
        viewModelScope.launch {
            val userId = currentUser.value.id
            val isFav = favoriteIds.value.contains(propertyId)
            propertyRepository.toggleFavorite(userId, propertyId, isFav)
        }
    }

    // Owner Operations
    fun saveProperty(property: Property, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            propertyRepository.saveProperty(property)
            onComplete()
        }
    }

    fun updateProperty(property: Property, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            propertyRepository.updateProperty(property)
            onComplete()
        }
    }

    fun updatePropertyStatus(propertyId: String, status: PropertyStatus) {
        viewModelScope.launch {
            propertyRepository.updatePropertyStatus(propertyId, status)
        }
    }

    fun deleteProperty(propertyId: String) {
        viewModelScope.launch {
            propertyRepository.deletePropertyById(propertyId)
        }
    }

    fun deleteProperty(property: Property) {
        viewModelScope.launch {
            propertyRepository.deleteProperty(property)
        }
    }

    // Sample Data Management
    fun resetSampleData(onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            propertyRepository.resetSampleData(currentUser.value.id)
            onComplete()
        }
    }

    fun clearSampleData(onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            propertyRepository.clearSampleData()
            onComplete()
        }
    }

    // Auth
    fun login(email: String, pass: String, onResult: (Result<UserProfile>) -> Unit) {
        viewModelScope.launch {
            val res = authRepository.login(email, pass)
            onResult(res)
        }
    }

    fun signup(
        email: String,
        pass: String,
        fullName: String,
        phone: String,
        role: UserRole,
        onResult: (Result<UserProfile>) -> Unit
    ) {
        viewModelScope.launch {
            val res = authRepository.signup(email, pass, fullName, phone, role)
            onResult(res)
        }
    }

    fun clearAllData(onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            propertyRepository.clearAllProperties()
            onComplete()
        }
    }

    fun updateProfile(fullName: String, phone: String, role: UserRole, onResult: (Result<UserProfile>) -> Unit) {
        viewModelScope.launch {
            val res = authRepository.updateProfile(fullName, phone, role)
            onResult(res)
        }
    }

    fun logout() {
        authRepository.logout()
    }

    // Lead Capture
    fun submitLead(
        propertyId: String,
        propertyTitle: String,
        ownerId: String,
        userName: String,
        userPhone: String,
        message: String,
        contactMethod: String,
        onComplete: () -> Unit = {}
    ) {
        viewModelScope.launch {
            val lead = Lead(
                propertyId = propertyId,
                propertyTitle = propertyTitle,
                ownerId = ownerId,
                userName = userName,
                userPhone = userPhone,
                message = message,
                contactMethod = contactMethod
            )
            propertyRepository.recordLead(lead)
            onComplete()
        }
    }

    fun getOwnerLeadsFlow(ownerId: String) = propertyRepository.getLeadsForOwnerFlow(ownerId)
}
