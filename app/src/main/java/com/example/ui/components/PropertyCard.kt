package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bathtub
import androidx.compose.material.icons.filled.Bed
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.SquareFoot
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.FurnishingStatus
import com.example.data.model.Property
import com.example.data.model.PropertyStatus
import com.example.data.model.PropertyType
import com.example.ui.i18n.AppLanguage
import com.example.ui.i18n.AppStrings
import com.example.ui.i18n.LanguageManager
import com.example.ui.theme.StatusAvailable
import com.example.ui.theme.StatusAvailableBg
import com.example.ui.theme.StatusRented
import com.example.ui.theme.StatusRentedBg
import com.example.ui.util.CurrencyUtils

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PropertyCard(
    property: Property,
    isFavorite: Boolean,
    onFavoriteToggle: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    showContactButtons: Boolean = false
) {
    val context = LocalContext.current
    val currentLanguage by LanguageManager.currentLanguage.collectAsState()

    val formattedRent = remember(property.rent) { CurrencyUtils.formatRent(property.rent) }
    val isAvailable = remember(property.status) { property.status == PropertyStatus.AVAILABLE }
    val isRented = remember(property.status) { property.status == PropertyStatus.RENTED }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("property_card_${property.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column {
            // Image Header with overlay badges
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(190.dp)
            ) {
                if (!property.primaryImageUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = property.primaryImageUrl,
                        contentDescription = property.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(190.dp)
                    )

                    // Top Gradient for contrast
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(75.dp)
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Black.copy(alpha = 0.6f), Color.Transparent)
                                )
                            )
                    )
                } else {
                    // Clean Illustrated Banner when no photo is uploaded
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(190.dp)
                            .background(
                                Brush.linearGradient(
                                    listOf(
                                        MaterialTheme.colorScheme.primaryContainer,
                                        MaterialTheme.colorScheme.secondaryContainer
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                                modifier = Modifier.size(52.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Home,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(26.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = property.propertyType.displayName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = if (currentLanguage == AppLanguage.HINDI) "फोटो उपलब्ध नहीं है (No Photo)" else "No Photo Uploaded",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Status Watermark if RENTED
                if (isRented) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(190.dp)
                            .background(Color.Black.copy(alpha = 0.35f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = StatusRented.copy(alpha = 0.95f),
                            shadowElevation = 4.dp
                        ) {
                            Text(
                                text = if (currentLanguage == AppLanguage.HINDI) "किराये पर उठ चुका है" else "RENTED OUT",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                // Top Row: Property Type Badge & Favorite Button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        // Property Type Badge
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shadowElevation = 2.dp
                        ) {
                            val typeLabel = when (property.propertyType) {
                                PropertyType.FLAT -> AppStrings.typeFlat(currentLanguage)
                                PropertyType.HOUSE -> AppStrings.typeHouse(currentLanguage)
                                PropertyType.SHOP -> AppStrings.typeShop(currentLanguage)
                                PropertyType.ROOM -> AppStrings.typeRoom(currentLanguage)
                                PropertyType.OFFICE -> AppStrings.typeOffice(currentLanguage)
                                PropertyType.PG -> AppStrings.typePg(currentLanguage)
                                PropertyType.OTHER -> AppStrings.typeOther(currentLanguage)
                            }
                            Text(
                                text = typeLabel,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        // Availability Badge
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isAvailable) StatusAvailableBg else StatusRentedBg,
                            shadowElevation = 2.dp
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(if (isAvailable) StatusAvailable else StatusRented)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isAvailable) {
                                        if (currentLanguage == AppLanguage.HINDI) "उपलब्ध" else "AVAILABLE"
                                    } else {
                                        if (currentLanguage == AppLanguage.HINDI) "उठ चुका है" else "RENTED"
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (isAvailable) StatusAvailable else StatusRented
                                )
                            }
                        }
                    }

                    // Favorite Button
                    IconButton(
                        onClick = onFavoriteToggle,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.92f))
                            .testTag("favorite_button_${property.id}")
                    ) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = if (isFavorite) "Remove favorite" else "Add favorite",
                            tint = if (isFavorite) Color(0xFFEF4444) else Color(0xFF64748B),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Bottom Overlay: Rent Price pill
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(10.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color.Black.copy(alpha = 0.8f)
                    ) {
                        Row(
                            verticalAlignment = Alignment.Bottom,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = formattedRent,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                            Text(
                                text = AppStrings.rentPerMonth(currentLanguage),
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.8f),
                                modifier = Modifier.padding(bottom = 1.dp, start = 2.dp)
                            )
                        }
                    }
                }
            }

            // Body Details
            Column(modifier = Modifier.padding(14.dp)) {
                // Title
                Text(
                    text = property.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Location row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Location",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(15.dp)
                    )
                    Text(
                        text = if (property.locality.isNotBlank()) "${property.locality}, ${property.city}" else "${property.address}, ${property.city}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Key Specs: Bedrooms, Bathrooms, Area
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            RoundedCornerShape(10.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (property.bedrooms > 0) {
                        SpecItem(icon = Icons.Default.Bed, label = "${property.bedrooms} BHK")
                    } else {
                        SpecItem(icon = Icons.Default.Home, label = property.propertyType.displayName)
                    }

                    SpecItem(icon = Icons.Default.Bathtub, label = "${property.bathrooms} Bath")
                    SpecItem(icon = Icons.Default.SquareFoot, label = "${property.areaSqft} sq ft")
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Facilities
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    maxItemsInEachRow = 3
                ) {
                    if (property.hasParking) {
                        AmenityBadge(text = if (currentLanguage == AppLanguage.HINDI) "पार्किंग" else "Parking")
                    }
                    if (property.furnishingStatus != FurnishingStatus.UNFURNISHED) {
                        AmenityBadge(text = property.furnishingStatus.displayName)
                    }
                    AmenityBadge(text = if (property.securityDeposit > 0) "Dep: ₹${property.securityDeposit}" else "No Deposit")
                }

                // Optional Quick Direct Contact Actions on card
                if (showContactButtons && isAvailable) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                val callIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${property.contactPhone}"))
                                context.startActivity(callIntent)
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = ButtonDefaults.ButtonWithIconContentPadding
                        ) {
                            Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (currentLanguage == AppLanguage.HINDI) "कॉल" else "Call", fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = {
                                val message = "Hi, I am interested in '${property.title}' listed on RentNear."
                                val cleanNum = property.whatsappNumber.replace("+", "").replace(" ", "").replace("-", "")
                                val uri = Uri.parse("https://wa.me/$cleanNum?text=${Uri.encode(message)}")
                                val waIntent = Intent(Intent.ACTION_VIEW, uri)
                                try {
                                    context.startActivity(waIntent)
                                } catch (_: Exception) {}
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = ButtonDefaults.ButtonWithIconContentPadding
                        ) {
                            Text(if (currentLanguage == AppLanguage.HINDI) "व्हाट्सएप" else "WhatsApp", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SpecItem(
    icon: ImageVector,
    label: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(14.dp)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun AmenityBadge(text: String) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.padding(0.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}
