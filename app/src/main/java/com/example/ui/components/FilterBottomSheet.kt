package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.SheetState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.FurnishingStatus
import com.example.data.model.ListedBy
import com.example.data.model.PropertyFilter
import com.example.data.model.PropertyStatus
import com.example.data.model.PropertyType
import com.example.data.model.SortOption
import com.example.data.model.WaterSupply
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun FilterBottomSheet(
    currentFilter: PropertyFilter,
    availableCities: List<String>,
    onApplyFilter: (PropertyFilter) -> Unit,
    onDismiss: () -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
) {
    var selectedCity by remember { mutableStateOf(currentFilter.city ?: "") }
    var selectedType by remember { mutableStateOf(currentFilter.propertyType) }
    var minRent by remember { mutableStateOf(currentFilter.minRent?.toString() ?: "") }
    var maxRent by remember { mutableStateOf(currentFilter.maxRent?.toString() ?: "") }
    var minArea by remember { mutableStateOf(currentFilter.minArea?.toString() ?: "") }
    var maxArea by remember { mutableStateOf(currentFilter.maxArea?.toString() ?: "") }
    var bedrooms by remember { mutableStateOf(currentFilter.bedrooms) }
    var bathrooms by remember { mutableStateOf(currentFilter.bathrooms) }
    var furnishing by remember { mutableStateOf(currentFilter.furnishingStatus) }
    var hasParking by remember { mutableStateOf(currentFilter.hasParking ?: false) }
    var waterSupply by remember { mutableStateOf(currentFilter.waterSupply) }
    var hasBalcony by remember { mutableStateOf(currentFilter.hasBalcony ?: false) }
    var hasElectricity by remember { mutableStateOf(currentFilter.hasElectricityBackup ?: false) }
    var listedBy by remember { mutableStateOf(currentFilter.listedBy) }
    var sortOption by remember { mutableStateOf(currentFilter.sortOption) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Filters & Sort",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedButton(
                        onClick = {
                            selectedCity = ""
                            selectedType = null
                            minRent = ""
                            maxRent = ""
                            minArea = ""
                            maxArea = ""
                            bedrooms = null
                            bathrooms = null
                            furnishing = null
                            hasParking = false
                            waterSupply = null
                            hasBalcony = false
                            hasElectricity = false
                            listedBy = null
                            sortOption = SortOption.NEWEST
                        },
                        modifier = Modifier.testTag("reset_filters_button")
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Reset")
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Sort Section
            FilterSectionTitle(title = "Sort By")
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                SortOption.entries.forEach { option ->
                    FilterChip(
                        selected = sortOption == option,
                        onClick = { sortOption = option },
                        label = { Text(option.displayName) }
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            // City Filter
            FilterSectionTitle(title = "City / Location")
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                FilterChip(
                    selected = selectedCity.isBlank(),
                    onClick = { selectedCity = "" },
                    label = { Text("All Cities") }
                )
                val allCities = (listOf("Kalka", "Chandigarh", "Panchkula", "Mohali", "Delhi") + availableCities).distinct()
                allCities.forEach { city ->
                    FilterChip(
                        selected = selectedCity.equals(city, ignoreCase = true),
                        onClick = {
                            selectedCity = if (selectedCity.equals(city, ignoreCase = true)) "" else city
                        },
                        label = { Text(city) }
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            // Property Type
            FilterSectionTitle(title = "Property Type")
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                FilterChip(
                    selected = selectedType == null,
                    onClick = { selectedType = null },
                    label = { Text("All Types") }
                )
                PropertyType.entries.forEach { type ->
                    FilterChip(
                        selected = selectedType == type,
                        onClick = {
                            selectedType = if (selectedType == type) null else type
                        },
                        label = { Text(type.displayName) }
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            // Price / Rent Range
            FilterSectionTitle(title = "Monthly Rent Range (₹)")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = minRent,
                    onValueChange = { minRent = it.filter { char -> char.isDigit() } },
                    label = { Text("Min Rent") },
                    placeholder = { Text("₹ 5,000") },
                    singleLine = true,
                    modifier = Modifier.weight(1f).testTag("min_rent_input")
                )
                OutlinedTextField(
                    value = maxRent,
                    onValueChange = { maxRent = it.filter { char -> char.isDigit() } },
                    label = { Text("Max Rent") },
                    placeholder = { Text("₹ 30,000") },
                    singleLine = true,
                    modifier = Modifier.weight(1f).testTag("max_rent_input")
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            // Bedrooms (BHK)
            FilterSectionTitle(title = "Bedrooms")
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                FilterChip(
                    selected = bedrooms == null,
                    onClick = { bedrooms = null },
                    label = { Text("Any") }
                )
                listOf(1 to "1 BHK", 2 to "2 BHK", 3 to "3 BHK", 4 to "4+ BHK").forEach { (num, label) ->
                    FilterChip(
                        selected = bedrooms == num,
                        onClick = { bedrooms = if (bedrooms == num) null else num },
                        label = { Text(label) }
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            // Furnishing Status
            FilterSectionTitle(title = "Furnishing")
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                FilterChip(
                    selected = furnishing == null,
                    onClick = { furnishing = null },
                    label = { Text("Any") }
                )
                FurnishingStatus.entries.forEach { f ->
                    FilterChip(
                        selected = furnishing == f,
                        onClick = { furnishing = if (furnishing == f) null else f },
                        label = { Text(f.displayName) }
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            // Amenities & Facilities Switches
            FilterSectionTitle(title = "Amenities & Facilities")
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Car / Bike Parking Required")
                    Switch(checked = hasParking, onCheckedChange = { hasParking = it })
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Balcony Included")
                    Switch(checked = hasBalcony, onCheckedChange = { hasBalcony = it })
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Power / Electricity Backup")
                    Switch(checked = hasElectricity, onCheckedChange = { hasElectricity = it })
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            // Listed By
            FilterSectionTitle(title = "Listed By")
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                FilterChip(
                    selected = listedBy == null,
                    onClick = { listedBy = null },
                    label = { Text("Any") }
                )
                ListedBy.entries.forEach { l ->
                    FilterChip(
                        selected = listedBy == l,
                        onClick = { listedBy = if (listedBy == l) null else l },
                        label = { Text(l.displayName) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Apply Button
            Button(
                onClick = {
                    val updated = currentFilter.copy(
                        city = selectedCity.takeIf { it.isNotBlank() },
                        propertyType = selectedType,
                        minRent = minRent.toIntOrNull(),
                        maxRent = maxRent.toIntOrNull(),
                        minArea = minArea.toIntOrNull(),
                        maxArea = maxArea.toIntOrNull(),
                        bedrooms = bedrooms,
                        bathrooms = bathrooms,
                        furnishingStatus = furnishing,
                        hasParking = if (hasParking) true else null,
                        waterSupply = waterSupply,
                        hasBalcony = if (hasBalcony) true else null,
                        hasElectricityBackup = if (hasElectricity) true else null,
                        listedBy = listedBy,
                        sortOption = sortOption
                    )
                    onApplyFilter(updated)
                },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("apply_filters_button")
            ) {
                Text(text = "Apply Filters", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun FilterSectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(bottom = 6.dp)
    )
}
