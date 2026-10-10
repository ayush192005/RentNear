package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Tune
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Property
import com.example.data.model.PropertyType
import com.example.data.model.UserRole
import com.example.ui.components.PropertyCard
import com.example.ui.i18n.AppLanguage
import com.example.ui.i18n.AppStrings
import com.example.ui.i18n.LanguageManager
import com.example.ui.viewmodel.RentNearViewModel

private val POPULAR_CITIES = listOf("All", "Kalka", "Chandigarh", "Panchkula", "Mohali", "Pinjore", "Delhi")

@Composable
fun HomeScreen(
    viewModel: RentNearViewModel,
    onNavigateToSearch: () -> Unit,
    onNavigateToDetails: (String) -> Unit,
    onNavigateToOwnerDashboard: () -> Unit,
    onNavigateToAddProperty: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentLanguage by LanguageManager.currentLanguage.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val featuredProperties by viewModel.featuredProperties.collectAsState()
    val recentProperties by viewModel.recentProperties.collectAsState()
    val favoriteIds by viewModel.favoriteIds.collectAsState()
    val currentFilter by viewModel.currentFilter.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val syncError by viewModel.syncError.collectAsState()
    val isDatabaseConnected by viewModel.isDatabaseConnected.collectAsState()

    var showConfigDialog by remember { mutableStateOf(false) }

    // Refresh listings from Supabase whenever the home screen becomes active (e.g. after posting a property)
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.refreshRemoteData()
    }

    var searchInputText by remember { mutableStateOf(currentFilter.searchQuery) }

    val hasNoProperties by remember(featuredProperties, recentProperties) {
        derivedStateOf { featuredProperties.isEmpty() && recentProperties.isEmpty() }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("home_screen_scroll"),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        // 1. Top Brand Banner with Language Switcher and Search Bar
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.90f)
                            )
                        )
                    )
                    .padding(horizontal = 20.dp, vertical = 22.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Logo & Title
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Home,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(26.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = AppStrings.appTitle(currentLanguage),
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                            }
                            Text(
                                text = AppStrings.appTagline(currentLanguage),
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Manual Refresh Listings Button
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = Color.White.copy(alpha = 0.22f),
                                modifier = Modifier
                                    .clickable { viewModel.refreshRemoteData() }
                                    .testTag("refresh_listings_button")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = "Refresh",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (isRefreshing) "Syncing..." else "Refresh",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }

                            // Language Switcher Button [ English | हिंदी ]
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = Color.White.copy(alpha = 0.22f),
                                modifier = Modifier
                                    .clickable { LanguageManager.toggleLanguage() }
                                    .testTag("language_toggle_button")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Language,
                                        contentDescription = "Language",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (currentLanguage == AppLanguage.ENGLISH) "हिंदी" else "English",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }

                    // Database Connection Indicator Pill
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = when {
                            isRefreshing -> Color(0xFF3B82F6).copy(alpha = 0.25f)
                            isDatabaseConnected -> Color(0xFF10B981).copy(alpha = 0.25f)
                            else -> Color(0xFFEF4444).copy(alpha = 0.30f)
                        },
                        modifier = Modifier
                            .clickable {
                                if (!viewModel.supabaseHelper.isConfigured) {
                                    showConfigDialog = true
                                } else {
                                    viewModel.refreshRemoteData()
                                }
                            }
                            .testTag("database_connection_status_badge")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when {
                                            isRefreshing -> Color(0xFF60A5FA)
                                            isDatabaseConnected -> Color(0xFF34D399)
                                            else -> Color(0xFFF87171)
                                        }
                                    )
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = when {
                                    isRefreshing -> "Syncing with Database..."
                                    isDatabaseConnected -> "Live Database Connected (Supabase)"
                                    !viewModel.supabaseHelper.isConfigured -> "Database Not Configured (Tap to setup)"
                                    else -> "Database Offline (Tap to retry)"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    if (syncError != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.95f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Sync error: $syncError",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    modifier = Modifier.weight(1f)
                                )
                                TextButton(
                                    onClick = { viewModel.refreshRemoteData() },
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Text("Retry", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Prominent Direct Search Input Bar
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("home_search_bar_container"),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface,
                        shadowElevation = 4.dp
                    ) {
                        OutlinedTextField(
                            value = searchInputText,
                            onValueChange = {
                                searchInputText = it
                                viewModel.updateSearchQuery(it)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("home_search_input_field"),
                            placeholder = {
                                Text(
                                    text = AppStrings.searchPlaceholder(currentLanguage),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Search",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            trailingIcon = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (searchInputText.isNotBlank()) {
                                        IconButton(
                                            onClick = {
                                                searchInputText = ""
                                                viewModel.updateSearchQuery("")
                                            },
                                            modifier = Modifier.testTag("clear_search_button")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Clear,
                                                contentDescription = "Clear search",
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    IconButton(
                                        onClick = onNavigateToSearch,
                                        modifier = Modifier.testTag("open_filters_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Tune,
                                            contentDescription = "Filters",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions(
                                onSearch = {
                                    onNavigateToSearch()
                                }
                            ),
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent,
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent
                            )
                        )
                    }
                }
            }
        }

        // 2. City Selector Chips
        item {
            Column(modifier = Modifier.padding(top = 16.dp, start = 20.dp, end = 20.dp)) {
                Text(
                    text = AppStrings.popularLocations(currentLanguage),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(end = 20.dp),
                    modifier = Modifier.testTag("popular_locations_row")
                ) {
                    items(POPULAR_CITIES, key = { it }) { city ->
                        val isSelected = if (city == "All") currentFilter.city.isNullOrBlank() else currentFilter.city == city
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                viewModel.updateCityFilter(if (city == "All") null else city)
                                onNavigateToSearch()
                            },
                            label = { Text(if (city == "All") (if (currentLanguage == AppLanguage.HINDI) "सभी शहर" else "All") else city) },
                            modifier = Modifier.testTag("city_chip_$city")
                        )
                    }
                }
            }
        }

        // 3. Property Category Horizontal Selection
        item {
            Column(modifier = Modifier.padding(top = 18.dp, start = 20.dp, end = 20.dp)) {
                Text(
                    text = AppStrings.propertyTypes(currentLanguage),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(10.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(end = 20.dp),
                    modifier = Modifier.testTag("property_types_row")
                ) {
                    val types = listOf(
                        PropertyType.HOUSE to (Icons.Default.Home to AppStrings.typeHouse(currentLanguage)),
                        PropertyType.SHOP to (Icons.Default.Store to AppStrings.typeShop(currentLanguage)),
                        PropertyType.FLAT to (Icons.Default.Apartment to AppStrings.typeFlat(currentLanguage)),
                        PropertyType.ROOM to (Icons.Default.MeetingRoom to AppStrings.typeRoom(currentLanguage)),
                        PropertyType.OFFICE to (Icons.Default.Business to AppStrings.typeOffice(currentLanguage)),
                        PropertyType.PG to (Icons.Default.Hotel to AppStrings.typePg(currentLanguage)),
                        PropertyType.OTHER to (Icons.Default.MoreHoriz to AppStrings.typeOther(currentLanguage))
                    )
                    items(types) { (type, pair) ->
                        val (icon, title) = pair
                        PropertyTypeItem(
                            title = title,
                            icon = icon,
                            isSelected = currentFilter.propertyType == type,
                            onClick = {
                                viewModel.updateFilter(currentFilter.copy(propertyType = type))
                                onNavigateToSearch()
                            }
                        )
                    }
                }
            }
        }

        // 4. Horizontal Scrollable List for FEATURED PROPERTIES
        if (featuredProperties.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp, end = 20.dp, top = 22.dp, bottom = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = AppStrings.featuredProperties(currentLanguage),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = AppStrings.verifiedReady(currentLanguage),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    TextButton(
                        onClick = onNavigateToSearch,
                        modifier = Modifier.testTag("see_all_featured_button")
                    ) {
                        Text(AppStrings.seeAll(currentLanguage))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            item {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("featured_properties_horizontal_list"),
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(featuredProperties, key = { it.id }) { property ->
                        PropertyCard(
                            property = property,
                            isFavorite = favoriteIds.contains(property.id),
                            onFavoriteToggle = { viewModel.toggleFavorite(property.id) },
                            onClick = { onNavigateToDetails(property.id) },
                            modifier = Modifier.width(285.dp)
                        )
                    }
                }
            }
        }

        // When database is empty (no listings posted yet), show a single clean empty card
        if (hasNoProperties) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                        .testTag("empty_properties_card"),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                            modifier = Modifier.size(60.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Home,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (currentLanguage == AppLanguage.HINDI) "अभी कोई मकान या दुकान लिस्टेड नहीं है" else "No Properties Listed Yet",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (currentLanguage == AppLanguage.HINDI)
                                "जब आप या कोई मालिक मकान, दुकान या कमरा जोड़ेंगे, तभी वह यहाँ किराये और विवरण के साथ दिखेगा।"
                            else
                                "Only genuine listings posted by owners appear here. Post the first property to get started!",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = onNavigateToAddProperty,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.testTag("post_first_property_button")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (currentLanguage == AppLanguage.HINDI) "पहली प्रॉपर्टी जोड़ें" else "Post First Property")
                            }

                            androidx.compose.material3.OutlinedButton(
                                onClick = { viewModel.refreshRemoteData() },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.testTag("empty_state_refresh_button")
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (isRefreshing) "Syncing..." else "Refresh")
                            }
                        }
                    }
                }
            }
        }

        // 6. Horizontal Scrollable List for RECENTLY ADDED PROPERTIES
        if (recentProperties.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp, end = 20.dp, top = 10.dp, bottom = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = AppStrings.recentProperties(currentLanguage),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (currentLanguage == AppLanguage.HINDI) "आपके क्षेत्र के नए मकान व दुकानें" else "Fresh rentals in your local area",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    TextButton(
                        onClick = onNavigateToSearch,
                        modifier = Modifier.testTag("see_all_recent_button")
                    ) {
                        Text(AppStrings.seeAll(currentLanguage))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            item {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("recent_properties_horizontal_list"),
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(recentProperties, key = { it.id }) { property ->
                        PropertyCard(
                            property = property,
                            isFavorite = favoriteIds.contains(property.id),
                            onFavoriteToggle = { viewModel.toggleFavorite(property.id) },
                            onClick = { onNavigateToDetails(property.id) },
                            modifier = Modifier.width(285.dp)
                        )
                    }
                }
            }
        }
    }

    // Supabase Configuration Dialog (can be opened from connection badge on HomeScreen)
    if (showConfigDialog) {
        SupabaseConfigDialog(
            helper = viewModel.supabaseHelper,
            onDismiss = { showConfigDialog = false },
            onSaved = {
                showConfigDialog = false
                viewModel.refreshRemoteData()
            }
        )
    }
}

@Composable
private fun PropertyTypeItem(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(4.dp)
            .testTag("property_type_item_$title")
    ) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(CircleShape)
                .background(
                    if (isSelected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.surfaceVariant
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (isSelected) MaterialTheme.colorScheme.onPrimary
                else MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
