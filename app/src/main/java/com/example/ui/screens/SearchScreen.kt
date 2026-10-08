package com.example.ui.screens

import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.PropertyFilter
import com.example.data.model.SortOption
import com.example.ui.components.AiSearchBar
import com.example.ui.components.FilterBottomSheet
import com.example.ui.components.PropertyCard
import com.example.ui.viewmodel.RentNearViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    viewModel: RentNearViewModel,
    onNavigateToDetails: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val currentFilter by viewModel.currentFilter.collectAsState()
    val properties by viewModel.filteredProperties.collectAsState()
    val favoriteIds by viewModel.favoriteIds.collectAsState()
    val availableCities by viewModel.availableCities.collectAsState()
    val isAiSearching by viewModel.isAiSearching.collectAsState()
    val aiInterpretation by viewModel.aiInterpretation.collectAsState()

    var showFilterSheet by remember { mutableStateOf(false) }

    // Calculate active filter count
    val activeFilterCount = remember(currentFilter) {
        var count = 0
        if (!currentFilter.city.isNullOrBlank()) count++
        if (currentFilter.propertyType != null) count++
        if (currentFilter.minRent != null || currentFilter.maxRent != null) count++
        if (currentFilter.minArea != null || currentFilter.maxArea != null) count++
        if (currentFilter.bedrooms != null) count++
        if (currentFilter.bathrooms != null) count++
        if (currentFilter.furnishingStatus != null) count++
        if (currentFilter.hasParking == true) count++
        if (currentFilter.hasBalcony == true) count++
        if (currentFilter.hasElectricityBackup == true) count++
        if (currentFilter.listedBy != null) count++
        count
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("search_screen")
    ) {
        // Top Search Bar & Filter trigger
        Surface(
            tonalElevation = 2.dp,
            shadowElevation = 1.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = currentFilter.searchQuery,
                        onValueChange = { viewModel.updateSearchQuery(it) },
                        placeholder = { Text("Search location, flat, shop...") },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = "Search")
                        },
                        trailingIcon = {
                            if (currentFilter.searchQuery.isNotBlank()) {
                                IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("search_query_input")
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // Filter Button with Badge
                    BadgedBox(
                        badge = {
                            if (activeFilterCount > 0) {
                                Badge { Text("$activeFilterCount") }
                            }
                        }
                    ) {
                        OutlinedButton(
                            onClick = { showFilterSheet = true },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .height(56.dp)
                                .testTag("open_filters_sheet_button")
                        ) {
                            Icon(Icons.Default.FilterList, contentDescription = "Filter")
                        }
                    }
                }

                // AI Search prompt bar
                Spacer(modifier = Modifier.height(10.dp))
                AiSearchBar(
                    queryText = currentFilter.aiSearchPrompt ?: "",
                    onQueryChange = {},
                    onSearchTriggered = { viewModel.triggerAiSearch(it) },
                    isLoading = isAiSearching,
                    activeAiInterpretation = aiInterpretation,
                    onClearAiFilter = { viewModel.clearAiSearch() }
                )

                // Active Filters Chips
                if (activeFilterCount > 0) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (!currentFilter.city.isNullOrBlank()) {
                            InputChip(
                                selected = true,
                                onClick = { viewModel.updateFilter(currentFilter.copy(city = null)) },
                                label = { Text("City: ${currentFilter.city}") },
                                trailingIcon = { Icon(Icons.Default.Clear, contentDescription = null, modifier = Modifier.size(14.dp)) }
                            )
                        }
                        if (currentFilter.propertyType != null) {
                            InputChip(
                                selected = true,
                                onClick = { viewModel.updateFilter(currentFilter.copy(propertyType = null)) },
                                label = { Text(currentFilter.propertyType!!.displayName) },
                                trailingIcon = { Icon(Icons.Default.Clear, contentDescription = null, modifier = Modifier.size(14.dp)) }
                            )
                        }
                        if (currentFilter.bedrooms != null) {
                            InputChip(
                                selected = true,
                                onClick = { viewModel.updateFilter(currentFilter.copy(bedrooms = null)) },
                                label = { Text("${currentFilter.bedrooms} BHK") },
                                trailingIcon = { Icon(Icons.Default.Clear, contentDescription = null, modifier = Modifier.size(14.dp)) }
                            )
                        }
                        if (currentFilter.maxRent != null) {
                            InputChip(
                                selected = true,
                                onClick = { viewModel.updateFilter(currentFilter.copy(maxRent = null)) },
                                label = { Text("Under ₹${currentFilter.maxRent}") },
                                trailingIcon = { Icon(Icons.Default.Clear, contentDescription = null, modifier = Modifier.size(14.dp)) }
                            )
                        }
                        if (currentFilter.hasParking == true) {
                            InputChip(
                                selected = true,
                                onClick = { viewModel.updateFilter(currentFilter.copy(hasParking = null)) },
                                label = { Text("Parking") },
                                trailingIcon = { Icon(Icons.Default.Clear, contentDescription = null, modifier = Modifier.size(14.dp)) }
                            )
                        }
                        if (currentFilter.furnishingStatus != null) {
                            InputChip(
                                selected = true,
                                onClick = { viewModel.updateFilter(currentFilter.copy(furnishingStatus = null)) },
                                label = { Text(currentFilter.furnishingStatus!!.displayName) },
                                trailingIcon = { Icon(Icons.Default.Clear, contentDescription = null, modifier = Modifier.size(14.dp)) }
                            )
                        }
                    }
                }
            }
        }

        // Result Count & Sort row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${properties.size} properties available",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = "Sorted by: ${currentFilter.sortOption.displayName}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }

        // Properties List or Empty State
        if (properties.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(64.dp)
                    )
                    Text(
                        text = "No properties found",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Try clearing some filters or searching in a different city or locality.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { viewModel.updateFilter(PropertyFilter()) },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("reset_all_search_filters_button")
                    ) {
                        Text("Reset All Filters")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("search_results_list"),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 90.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(properties, key = { it.id }) { property ->
                    PropertyCard(
                        property = property,
                        isFavorite = favoriteIds.contains(property.id),
                        onFavoriteToggle = { viewModel.toggleFavorite(property.id) },
                        onClick = { onNavigateToDetails(property.id) }
                    )
                }
            }
        }
    }

    if (showFilterSheet) {
        FilterBottomSheet(
            currentFilter = currentFilter,
            availableCities = availableCities,
            onApplyFilter = { newFilter ->
                viewModel.updateFilter(newFilter)
                showFilterSheet = false
            },
            onDismiss = { showFilterSheet = false }
        )
    }
}
