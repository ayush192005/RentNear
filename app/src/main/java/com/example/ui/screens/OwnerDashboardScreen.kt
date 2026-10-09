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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.Property
import com.example.data.model.PropertyStatus
import com.example.ui.i18n.AppLanguage
import com.example.ui.i18n.AppStrings
import com.example.ui.i18n.LanguageManager
import com.example.ui.theme.StatusAvailable
import com.example.ui.theme.StatusAvailableBg
import com.example.ui.theme.StatusRented
import com.example.ui.theme.StatusRentedBg
import com.example.ui.util.CurrencyUtils
import com.example.ui.viewmodel.RentNearViewModel

@Composable
fun OwnerDashboardScreen(
    viewModel: RentNearViewModel,
    onNavigateToAddProperty: () -> Unit,
    onNavigateToEditProperty: (String) -> Unit,
    onNavigateToDetails: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val currentLanguage by LanguageManager.currentLanguage.collectAsState()
    val ownerProperties by viewModel.ownerProperties.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current

    androidx.lifecycle.compose.LifecycleEventEffect(androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
        viewModel.refreshRemoteData()
    }

    var propertyToDelete by remember { mutableStateOf<Property?>(null) }

    val availableCount by remember(ownerProperties) {
        derivedStateOf { ownerProperties.count { it.status == PropertyStatus.AVAILABLE } }
    }
    val rentedCount by remember(ownerProperties) {
        derivedStateOf { ownerProperties.count { it.status == PropertyStatus.RENTED } }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAddProperty,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("add_property_fab")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Property")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (currentLanguage == AppLanguage.HINDI) "मकान / दुकान जोड़ें" else "Post House/Shop",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("owner_dashboard_list"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header & Language Switcher
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (currentLanguage == AppLanguage.HINDI) "मालिक डैशबोर्ड" else "Owner Dashboard",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = if (currentLanguage == AppLanguage.HINDI)
                                "अपनी किराये की संपत्तियां प्रबंधित करें"
                            else
                                "Manage your rental listings & tenant inquiries",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.clickable { LanguageManager.toggleLanguage() }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Language, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (currentLanguage == AppLanguage.ENGLISH) "हिंदी" else "EN",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Overview Metric Cards - Redesigned with modern cards & status indicators
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ModernMetricCard(
                            title = if (currentLanguage == AppLanguage.HINDI) "कुल प्रॉपर्टी" else "Total Listed",
                            count = ownerProperties.size.toString(),
                            subtitle = if (currentLanguage == AppLanguage.HINDI) "सभी यूनिट्स" else "All Units",
                            icon = Icons.Default.Apartment,
                            badgeColor = MaterialTheme.colorScheme.primaryContainer,
                            iconTint = MaterialTheme.colorScheme.primary,
                            countColor = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f)
                        )
                        ModernMetricCard(
                            title = if (currentLanguage == AppLanguage.HINDI) "उपलब्ध" else "Available",
                            count = availableCount.toString(),
                            subtitle = if (currentLanguage == AppLanguage.HINDI) "खाली / तैयार" else "Vacant",
                            icon = Icons.Default.CheckCircle,
                            badgeColor = Color(0xFFD1FAE5),
                            iconTint = Color(0xFF059669),
                            countColor = Color(0xFF047857),
                            isAvailableIndicator = true,
                            modifier = Modifier.weight(1f)
                        )
                        ModernMetricCard(
                            title = if (currentLanguage == AppLanguage.HINDI) "उठ चुकी है" else "Rented Out",
                            count = rentedCount.toString(),
                            subtitle = if (currentLanguage == AppLanguage.HINDI) "किराये पर" else "Occupied",
                            icon = Icons.Default.Key,
                            badgeColor = Color(0xFFFEF3C7),
                            iconTint = Color(0xFFD97706),
                            countColor = Color(0xFFB45309),
                            isRentedIndicator = true,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Occupancy Health & Progress Meter
                    val totalProps = ownerProperties.size
                    val occupancyPercent = if (totalProps > 0) (rentedCount.toFloat() / totalProps.toFloat() * 100).toInt() else 0

                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.TrendingUp,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (currentLanguage == AppLanguage.HINDI) "ऑक्यूपेंसी दर (Occupancy)" else "Occupancy Status",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (occupancyPercent >= 70) Color(0xFFD1FAE5) else MaterialTheme.colorScheme.primaryContainer
                                ) {
                                    Text(
                                        text = "$occupancyPercent%",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (occupancyPercent >= 70) Color(0xFF047857) else MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            LinearProgressIndicator(
                                progress = { if (totalProps > 0) rentedCount.toFloat() / totalProps.toFloat() else 0f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = if (occupancyPercent >= 70) Color(0xFF10B981) else MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = if (currentLanguage == AppLanguage.HINDI)
                                        "$availableCount खाली यूनिट्स उपलब्ध"
                                    else
                                        "$availableCount vacant units ready",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = if (currentLanguage == AppLanguage.HINDI)
                                        "$rentedCount किराये पर उठी"
                                    else
                                        "$rentedCount generating rent",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // Listings Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (currentLanguage == AppLanguage.HINDI) "आपकी लिस्टिंग्स" else "Your Properties",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${ownerProperties.size} ${if (currentLanguage == AppLanguage.HINDI) "प्रॉपर्टीज" else "properties"}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Empty State
            if (ownerProperties.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Home,
                                contentDescription = null,
                                modifier = Modifier.size(56.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = if (currentLanguage == AppLanguage.HINDI) "कोई प्रॉपर्टी नहीं जोड़ी गई" else "No properties posted yet",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = if (currentLanguage == AppLanguage.HINDI)
                                    "अपना मकान, दुकान या कमरा पोस्ट करें और किरायेदारों को दिखाएं"
                                else
                                    "Post your house, shop or room to receive direct tenant inquiries.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = onNavigateToAddProperty,
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (currentLanguage == AppLanguage.HINDI) "प्रॉपर्टी जोड़ें" else "Post Property")
                            }
                        }
                    }
                }
            }

            // List of Properties with 1-Tap Status Switch
            items(ownerProperties, key = { it.id }) { property ->
                OwnerPropertyCard(
                    property = property,
                    currentLanguage = currentLanguage,
                    onToggleStatus = {
                        val newStatus = if (property.status == PropertyStatus.AVAILABLE) {
                            PropertyStatus.RENTED
                        } else {
                            PropertyStatus.AVAILABLE
                        }
                        viewModel.updatePropertyStatus(property.id, newStatus)
                    },
                    onEdit = { onNavigateToEditProperty(property.id) },
                    onDelete = { propertyToDelete = property },
                    onView = { onNavigateToDetails(property.id) }
                )
            }
        }
    }

    // Delete Confirmation Dialog
    propertyToDelete?.let { property ->
        AlertDialog(
            onDismissRequest = { propertyToDelete = null },
            title = {
                Text(if (currentLanguage == AppLanguage.HINDI) "प्रॉपर्टी हटाएं?" else "Delete Property?")
            },
            text = {
                Text(
                    if (currentLanguage == AppLanguage.HINDI)
                        "क्या आप वाकई '${property.title}' को हटाना चाहते हैं? यह डेटा हमेशा के लिए हट जाएगा।"
                    else
                        "Are you sure you want to delete '${property.title}'? This action cannot be undone."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteProperty(property) { result ->
                            if (result.isSuccess) {
                                android.widget.Toast.makeText(context, "Property deleted from database", android.widget.Toast.LENGTH_SHORT).show()
                            } else {
                                val msg = result.exceptionOrNull()?.localizedMessage ?: "Failed to delete"
                                android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_LONG).show()
                            }
                        }
                        propertyToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(if (currentLanguage == AppLanguage.HINDI) "हाँ, हटाएं" else "Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { propertyToDelete = null }) {
                    Text(if (currentLanguage == AppLanguage.HINDI) "रद्द करें" else "Cancel")
                }
            }
        )
    }
}

@Composable
private fun OwnerPropertyCard(
    property: Property,
    currentLanguage: AppLanguage,
    onToggleStatus: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onView: () -> Unit
) {
    val formattedRent = remember(property.rent) { CurrencyUtils.formatRent(property.rent) }
    val isAvailable = remember(property.status) { property.status == PropertyStatus.AVAILABLE }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("owner_property_card_${property.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (!property.primaryImageUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = property.primaryImageUrl,
                        contentDescription = property.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(86.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(86.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = property.propertyType.displayName,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        // Distinct Status Badge
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isAvailable) StatusAvailableBg else StatusRentedBg
                        ) {
                            Text(
                                text = if (isAvailable) {
                                    if (currentLanguage == AppLanguage.HINDI) "● उपलब्ध" else "● AVAILABLE"
                                } else {
                                    if (currentLanguage == AppLanguage.HINDI) "● उठ चुका है" else "● RENTED OUT"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isAvailable) StatusAvailable else StatusRented,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = property.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = "$formattedRent / mo • ${property.city}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )

                    Text(
                        text = "${property.bedrooms} BHK • ${property.areaSqft} sq ft",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 1-Tap Status Switcher: MARK AS RENTED / MARK AS AVAILABLE
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (isAvailable) StatusRentedBg else StatusAvailableBg,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onToggleStatus)
                    .testTag("toggle_status_button_${property.id}")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = if (isAvailable) StatusRented else StatusAvailable,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isAvailable) {
                            if (currentLanguage == AppLanguage.HINDI) "किराये पर उठ गया? (Rented मार्क करें)" else "Mark as Rented Out"
                        } else {
                            if (currentLanguage == AppLanguage.HINDI) "फिर से उपलब्ध करें (Mark Available)" else "Mark as Available"
                        },
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isAvailable) StatusRented else StatusAvailable
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // View, Edit, Delete Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onView,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (currentLanguage == AppLanguage.HINDI) "देखें" else "View", fontSize = 12.sp)
                }

                Button(
                    onClick = onEdit,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (currentLanguage == AppLanguage.HINDI) "एडिट" else "Edit", fontSize = 12.sp)
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.testTag("delete_property_button_${property.id}")
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
private fun ModernMetricCard(
    title: String,
    count: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    badgeColor: Color,
    iconTint: Color,
    countColor: Color,
    isAvailableIndicator: Boolean = false,
    isRentedIndicator: Boolean = false,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(badgeColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(18.dp)
                    )
                }

                if (isAvailableIndicator) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF10B981))
                    )
                } else if (isRentedIndicator) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF59E0B))
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = count,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = countColor
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
