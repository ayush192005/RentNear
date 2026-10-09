package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Bathtub
import androidx.compose.material.icons.filled.Bed
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalParking
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SquareFoot
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.Lead
import com.example.data.model.Property
import com.example.data.model.PropertyStatus
import com.example.data.repository.PropertyRepository
import com.example.ui.components.ContactActionType
import com.example.ui.components.ContactBar
import com.example.ui.components.ContactUtils
import com.example.ui.components.LeadCaptureDialog
import com.example.ui.components.LeafletPropertyMapView
import com.example.ui.util.CurrencyUtils
import com.example.ui.i18n.AppLanguage
import com.example.ui.i18n.AppStrings
import com.example.ui.i18n.LanguageManager
import com.example.ui.theme.StatusAvailable
import com.example.ui.theme.StatusRented
import com.example.ui.theme.StatusRentedBg
import com.example.ui.viewmodel.RentNearViewModel
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * PropertyDetailScreen Composable:
 * Displays full property information (images, specifications, description, amenities,
 * location, and owner contact) fetched reactively from the PropertyRepository.
 * Includes direct 'Call' and 'Message' / 'WhatsApp' buttons to contact the property owner.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun PropertyDetailScreen(
    propertyId: String,
    propertyRepository: PropertyRepository,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    isFavorite: Boolean = false,
    onFavoriteToggle: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val currentLanguage by LanguageManager.currentLanguage.collectAsState()

    // Fetch property directly from PropertyRepository stream
    val propertyState by propertyRepository.getPropertyByIdFlow(propertyId).collectAsState(initial = null)
    val property = propertyState

    androidx.compose.runtime.LaunchedEffect(propertyId) {
        propertyRepository.refreshPropertyById(propertyId)
    }

    if (property == null) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(if (currentLanguage == AppLanguage.HINDI) "प्रॉपर्टी विवरण" else "Property Details") },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("detail_back_button")) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(modifier = Modifier.size(36.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (currentLanguage == AppLanguage.HINDI) "लोड हो रहा है..." else "Loading property details...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        return
    }

    val formattedRent = remember(property.rent) { CurrencyUtils.formatRent(property.rent) }
    val formattedDeposit = remember(property.securityDeposit) { CurrencyUtils.formatRent(property.securityDeposit) }

    val images = remember(property.imageUrls) { property.imageUrls.filter { it.isNotBlank() } }
    val pagerState = rememberPagerState(pageCount = { if (images.isNotEmpty()) images.size else 1 })

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(text = property.title, maxLines = 1) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("detail_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Share Button
                    IconButton(
                        onClick = {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(
                                    Intent.EXTRA_TEXT,
                                    "Check out this rental property on RentNear: ${property.title} in ${property.city} for $formattedRent/month!\nAddress: ${property.address}\nContact: ${property.contactPhone}"
                                )
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share Property"))
                        },
                        modifier = Modifier.testTag("share_property_button")
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Share")
                    }

                    // Favorite Button
                    if (onFavoriteToggle != null) {
                        IconButton(
                            onClick = onFavoriteToggle,
                            modifier = Modifier.testTag("favorite_toggle_button")
                        ) {
                            Icon(
                                imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Favorite",
                                tint = if (isFavorite) Color(0xFFEF4444) else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            // Direct Owner Contact Bar
            Surface(
                tonalElevation = 8.dp,
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(modifier = Modifier.padding(16.dp)) {
                    if (property.status == PropertyStatus.AVAILABLE) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Direct Call Button
                            Button(
                                onClick = {
                                    val callIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${property.contactPhone}"))
                                    context.startActivity(callIntent)
                                },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(50.dp)
                                    .testTag("detail_call_owner_button")
                            ) {
                                Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (currentLanguage == AppLanguage.HINDI) "कॉल करें" else "Call Owner",
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            // Direct WhatsApp / Message Button
                            Button(
                                onClick = {
                                    val message = "Hi, I am interested in '${property.title}' in ${property.city} listed on RentNear."
                                    val cleanNum = property.whatsappNumber.replace("+", "").replace(" ", "").replace("-", "")
                                    val uri = Uri.parse("https://wa.me/$cleanNum?text=${Uri.encode(message)}")
                                    val waIntent = Intent(Intent.ACTION_VIEW, uri)
                                    try {
                                        context.startActivity(waIntent)
                                    } catch (_: Exception) {
                                        // Fallback to SMS Message
                                        val smsIntent = Intent(Intent.ACTION_VIEW, Uri.parse("sms:${property.contactPhone}")).apply {
                                            putExtra("sms_body", message)
                                        }
                                        try {
                                            context.startActivity(smsIntent)
                                        } catch (_: Exception) {
                                            Toast.makeText(context, "Could not open messaging app", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF25D366),
                                    contentColor = Color.White
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(50.dp)
                                    .testTag("detail_message_owner_button")
                            ) {
                                Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (currentLanguage == AppLanguage.HINDI) "व्हाट्सएप / मैसेज" else "Message / Chat",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    } else {
                        // Already Rented Out Banner
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = StatusRentedBg,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (currentLanguage == AppLanguage.HINDI)
                                        "यह प्रॉपर्टी किराये पर उठ चुकी है (RENTED OUT)"
                                    else
                                        "This property is ALREADY RENTED OUT",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = StatusRented
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("detail_screen_scroll"),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // 1. Image Gallery Pager
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                ) {
                    if (images.isNotEmpty()) {
                        HorizontalPager(
                            state = pagerState,
                            modifier = Modifier.fillMaxSize()
                        ) { page ->
                            AsyncImage(
                                model = images[page],
                                contentDescription = "Property photo ${page + 1}",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
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
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                                    modifier = Modifier.size(64.dp)
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
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = property.propertyType.displayName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = if (currentLanguage == AppLanguage.HINDI) "मालिक द्वारा फोटो नहीं डाली गई (No Photos)" else "No Photos Uploaded by Owner",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // Gradient Scrim
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp)
                            .align(Alignment.BottomCenter)
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color.Transparent, Color.Black.copy(alpha = 0.6f))
                                )
                            )
                    )

                    // Page Indicator Pill
                    if (images.size > 1) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color.Black.copy(alpha = 0.6f),
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "${pagerState.currentPage + 1} / ${images.size}",
                                color = Color.White,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    // Status Badge (Available vs Rented)
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (property.status == PropertyStatus.AVAILABLE) StatusAvailable else StatusRented,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(16.dp)
                    ) {
                        Text(
                            text = if (property.status == PropertyStatus.AVAILABLE)
                                if (currentLanguage == AppLanguage.HINDI) "किराये पर उपलब्ध" else "AVAILABLE"
                            else
                                if (currentLanguage == AppLanguage.HINDI) "उठ चुका है" else "RENTED OUT",
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }
            }

            // 2. Title, Type, Rent and Deposit
            item {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = property.title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${property.locality}, ${property.city}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Pricing Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = formattedRent,
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = if (currentLanguage == AppLanguage.HINDI) " / माह" else " / month",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(bottom = 3.dp, start = 2.dp)
                                )
                            }
                            Text(
                                text = "${if (currentLanguage == AppLanguage.HINDI) "सिक्योरिटी डिपॉजिट" else "Security Deposit"}: $formattedDeposit",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Property Type Pill
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Text(
                                text = property.propertyType.displayName,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }

            // 3. Core Specs Overview (Bedrooms, Bathrooms, Area, Furnishing)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        DetailSpecTile(
                            icon = Icons.Default.Bed,
                            label = if (currentLanguage == AppLanguage.HINDI) "कमरे" else "Bedrooms",
                            value = "${property.bedrooms} BHK"
                        )
                        DetailSpecTile(
                            icon = Icons.Default.Bathtub,
                            label = if (currentLanguage == AppLanguage.HINDI) "बाथरूम" else "Bathrooms",
                            value = "${property.bathrooms}"
                        )
                        DetailSpecTile(
                            icon = Icons.Default.SquareFoot,
                            label = if (currentLanguage == AppLanguage.HINDI) "क्षेत्रफल" else "Area",
                            value = "${property.areaSqft} sq.ft"
                        )
                    }
                }
            }

            // 4. Description
            item {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = if (currentLanguage == AppLanguage.HINDI) "विवरण (Description)" else "Description",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = property.description,
                        style = MaterialTheme.typography.bodyMedium,
                        lineHeight = 22.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
                    )
                }
            }

            // 5. Amenities & Facilities
            item {
                Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                    Text(
                        text = if (currentLanguage == AppLanguage.HINDI) "सुविधाएं (Amenities)" else "Amenities & Facilities",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (property.hasParking) {
                            AmenityChip(icon = Icons.Default.LocalParking, text = if (currentLanguage == AppLanguage.HINDI) "पार्किंग" else "Parking")
                        }
                        if (property.hasBalcony) {
                            AmenityChip(icon = Icons.Default.CheckCircle, text = if (currentLanguage == AppLanguage.HINDI) "बालकनी" else "Balcony")
                        }
                        if (property.hasElectricityBackup) {
                            AmenityChip(icon = Icons.Default.ElectricBolt, text = if (currentLanguage == AppLanguage.HINDI) "बिजली बैकअप" else "Power Backup")
                        }
                        AmenityChip(icon = Icons.Default.WaterDrop, text = "${property.waterSupply.name.lowercase().replaceFirstChar { it.uppercase() }} Water")

                        property.amenities.forEach { amenity ->
                            AmenityChip(icon = Icons.Default.CheckCircle, text = amenity)
                        }
                    }
                }
            }

            // 6. Complete Location & Address
            item {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = if (currentLanguage == AppLanguage.HINDI) "स्थान व पता (Location)" else "Location & Address",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "${property.address}, ${property.locality}, ${property.city}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Interactive OpenStreetMap Leaflet View
                    LeafletPropertyMapView(
                        latitude = property.latitude,
                        longitude = property.longitude,
                        title = property.title,
                        address = "${property.address}, ${property.locality}, ${property.city}",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(14.dp))
                    )
                }
            }

            // 7. Owner Profile & Contact Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = property.ownerName.firstOrNull()?.uppercase() ?: "O",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = property.ownerName,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(
                                        imageVector = Icons.Default.VerifiedUser,
                                        contentDescription = "Verified",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Text(
                                    text = if (currentLanguage == AppLanguage.HINDI) "प्रॉपर्टी मालिक • तुरंत जवाब" else "Property Owner • Direct Contact",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                        // Phone and WhatsApp details displayed clearly
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = if (currentLanguage == AppLanguage.HINDI) "फोन नंबर" else "Phone",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = property.contactPhone,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Column {
                                Text(
                                    text = "WhatsApp",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = property.whatsappNumber,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Convenience overload accepting RentNearViewModel directly
 */
@Composable
fun PropertyDetailScreen(
    propertyId: String,
    viewModel: RentNearViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val favoriteIds by viewModel.favoriteIds.collectAsState()
    val isFav = favoriteIds.contains(propertyId)

    PropertyDetailScreen(
        propertyId = propertyId,
        propertyRepository = viewModel.propertyRepository,
        onNavigateBack = onNavigateBack,
        modifier = modifier,
        isFavorite = isFav,
        onFavoriteToggle = { viewModel.toggleFavorite(propertyId) }
    )
}

@Composable
private fun DetailSpecTile(
    icon: ImageVector,
    label: String,
    value: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun AmenityChip(
    icon: ImageVector,
    text: String
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 1.dp
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
