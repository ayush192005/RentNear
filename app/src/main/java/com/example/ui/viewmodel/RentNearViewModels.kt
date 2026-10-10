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
    val authRepository = DatabaseModule.provideAuthRepository(
        database = db,
        context = application,
        supabaseHelper = supabaseHelper
    )
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

    // Remote sync state
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _syncError = MutableStateFlow<String?>(null)
    val syncError: StateFlow<String?> = _syncError.asStateFlow()

    val isDatabaseConnected: StateFlow<Boolean> = _syncError.map { err ->
        supabaseHelper.isConfigured && err == null
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), supabaseHelper.isConfigured)

    init {
        viewModelScope.launch {
            propertyRepository.ensureSeeded()
            refreshRemoteData()
            // Realtime polling loop (every 10 seconds) for immediate multi-user synchronization
            while (kotlin.coroutines.coroutineContext[kotlinx.coroutines.Job]?.isActive != false) {
                kotlinx.coroutines.delay(10_000)
                if (supabaseHelper.isConfigured) {
                    val result = propertyRepository.refreshProperties()
                    if (result.isSuccess) {
                        _syncError.value = null
                    }
                }
            }
        }
    }

    fun refreshRemoteData(onComplete: (Result<List<Property>>) -> Unit = {}) {
        viewModelScope.launch {
            _isRefreshing.value = true
            val result = propertyRepository.refreshProperties()
            if (result.isSuccess) {
                _syncError.value = null
            } else {
                val err = result.exceptionOrNull()?.message ?: "Sync failed"
                _syncError.value = err
            }
            _isRefreshing.value = false
            onComplete(result)
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
    fun saveProperty(property: Property, onResult: (Result<Property>) -> Unit = {}) {
        viewModelScope.launch {
            val result = propertyRepository.saveProperty(property)
            if (result.isSuccess) {
                // Refresh listings cache to keep all views synchronized
                propertyRepository.refreshProperties()
            }
            onResult(result)
        }
    }

    fun updateProperty(property: Property, onResult: (Result<Property>) -> Unit = {}) {
        viewModelScope.launch {
            val result = propertyRepository.updateProperty(property)
            if (result.isSuccess) {
                propertyRepository.refreshProperties()
            }
            onResult(result)
        }
    }

    fun updatePropertyStatus(propertyId: String, status: PropertyStatus, onResult: (Result<Unit>) -> Unit = {}) {
        viewModelScope.launch {
            val result = propertyRepository.updatePropertyStatus(propertyId, status, currentUser.value.id)
            onResult(result)
        }
    }

    fun deleteProperty(propertyId: String, onResult: (Result<Unit>) -> Unit = {}) {
        viewModelScope.launch {
            val result = propertyRepository.deletePropertyById(propertyId, currentUser.value.id)
            if (result.isSuccess) {
                propertyRepository.refreshProperties()
            }
            onResult(result)
        }
    }

    fun deleteProperty(property: Property, onResult: (Result<Unit>) -> Unit = {}) {
        viewModelScope.launch {
            val result = propertyRepository.deleteProperty(property)
            if (result.isSuccess) {
                propertyRepository.refreshProperties()
            }
            onResult(result)
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
