package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Star
import androidx.core.content.ContextCompat
import com.example.ui.util.PropertyPhotoManager
import java.io.File
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.FurnishingStatus
import com.example.data.model.ListedBy
import com.example.data.model.Property
import com.example.data.model.PropertyStatus
import com.example.data.model.PropertyType
import com.example.data.model.WaterSupply
import com.example.ui.components.LeafletLocationPickerView
import com.example.ui.viewmodel.RentNearViewModel
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddEditPropertyScreen(
    propertyId: String?,
    viewModel: RentNearViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentUser by viewModel.currentUser.collectAsState()
    val properties by viewModel.filteredProperties.collectAsState()
    val recentProps by viewModel.recentProperties.collectAsState()

    val existingProperty = remember(propertyId, properties, recentProps) {
        if (propertyId != null) {
            properties.find { it.id == propertyId } ?: recentProps.find { it.id == propertyId }
        } else null
    }

    var title by remember { mutableStateOf(existingProperty?.title ?: "") }
    var description by remember { mutableStateOf(existingProperty?.description ?: "") }
    var propertyType by remember { mutableStateOf(existingProperty?.propertyType ?: PropertyType.FLAT) }
    var rentText by remember { mutableStateOf(existingProperty?.rent?.toString() ?: "") }
    var depositText by remember { mutableStateOf(existingProperty?.securityDeposit?.toString() ?: "") }
    var areaText by remember { mutableStateOf(existingProperty?.areaSqft?.toString() ?: "") }
    var bedrooms by remember { mutableStateOf(existingProperty?.bedrooms?.toString() ?: "2") }
    var bathrooms by remember { mutableStateOf(existingProperty?.bathrooms?.toString() ?: "2") }
    var address by remember { mutableStateOf(existingProperty?.address ?: "") }
    var locality by remember { mutableStateOf(existingProperty?.locality ?: "") }
    var city by remember { mutableStateOf(existingProperty?.city ?: "Kalka") }
    var latitude by remember { mutableDoubleStateOf(existingProperty?.latitude ?: 30.8354) }
    var longitude by remember { mutableDoubleStateOf(existingProperty?.longitude ?: 76.9362) }
    var furnishingStatus by remember { mutableStateOf(existingProperty?.furnishingStatus ?: FurnishingStatus.SEMI_FURNISHED) }
    var hasParking by remember { mutableStateOf(existingProperty?.hasParking ?: true) }
    var waterSupply by remember { mutableStateOf(existingProperty?.waterSupply ?: WaterSupply.HOURS_24_7) }
    var hasElectricityBackup by remember { mutableStateOf(existingProperty?.hasElectricityBackup ?: true) }
    var hasBalcony by remember { mutableStateOf(existingProperty?.hasBalcony ?: true) }
    var contactPhone by remember { mutableStateOf(existingProperty?.contactPhone ?: currentUser.phone.ifBlank { "+91 98765 43210" }) }
    var whatsappNumber by remember { mutableStateOf(existingProperty?.whatsappNumber ?: "919876543210") }
    var listedBy by remember { mutableStateOf(existingProperty?.listedBy ?: ListedBy.OWNER) }
    var status by remember { mutableStateOf(existingProperty?.status ?: PropertyStatus.AVAILABLE) }

    val amenities = remember {
        mutableStateListOf<String>().apply {
            if (existingProperty != null) addAll(existingProperty.amenities)
            else addAll(listOf("Car Parking", "Water Supply", "Power Backup"))
        }
    }

    val imageUrls = remember {
        mutableStateListOf<String>().apply {
            if (existingProperty != null && existingProperty.imageUrls.isNotEmpty()) {
                addAll(existingProperty.imageUrls)
            }
        }
    }

    var newImageUrlInput by remember { mutableStateOf("") }
    var typeDropdownExpanded by remember { mutableStateOf(false) }
    var furnishingDropdownExpanded by remember { mutableStateOf(false) }
    var waterDropdownExpanded by remember { mutableStateOf(false) }

    var tempCameraFile by remember { mutableStateOf<File?>(null) }
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }

    // Launcher for Full-Resolution Camera capture with FileProvider
    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempCameraFile != null) {
            val savedUrl = PropertyPhotoManager.promoteTempCameraFile(context, tempCameraFile!!)
            if (savedUrl != null) {
                imageUrls.add(savedUrl)
                Toast.makeText(context, "Photo captured and added!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Fallback Launcher for Camera Preview (Bitmap)
    val takePreviewLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            val savedUrl = PropertyPhotoManager.saveCameraBitmap(context, bitmap)
            if (savedUrl != null) {
                imageUrls.add(savedUrl)
                Toast.makeText(context, "Photo captured and added!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Permission launcher for Camera
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val pair = PropertyPhotoManager.createTempCameraUri(context)
            if (pair != null) {
                tempCameraUri = pair.first
                tempCameraFile = pair.second
                takePictureLauncher.launch(pair.first)
            } else {
                takePreviewLauncher.launch(null)
            }
        } else {
            Toast.makeText(context, "Camera permission needed to take photos", Toast.LENGTH_LONG).show()
        }
    }

    // Gallery Picker Launcher (Zero-permission Android Photo Picker)
    val galleryPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 10)
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            var addedCount = 0
            uris.forEach { uri ->
                val localSaved = PropertyPhotoManager.saveGalleryUriToInternalStorage(context, uri)
                if (localSaved != null && !imageUrls.contains(localSaved)) {
                    imageUrls.add(localSaved)
                    addedCount++
                }
            }
            if (addedCount > 0) {
                Toast.makeText(context, "Added $addedCount photo(s) from gallery", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (propertyId == null) "List New Property" else "Edit Property") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("add_edit_property_screen"),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Section 1: Basic Information
            item {
                FormSectionTitle(title = "Basic Information")
            }

            item {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Property Title *") },
                    placeholder = { Text("e.g. Spacious 2 BHK Flat near Kalka Station") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("property_form_title")
                )
            }

            item {
                ExposedDropdownMenuBox(
                    expanded = typeDropdownExpanded,
                    onExpandedChange = { typeDropdownExpanded = it }
                ) {
                    OutlinedTextField(
                        value = propertyType.displayName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Property Type *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeDropdownExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = typeDropdownExpanded,
                        onDismissRequest = { typeDropdownExpanded = false }
                    ) {
                        PropertyType.entries.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type.displayName) },
                                onClick = {
                                    propertyType = type
                                    typeDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            item {
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    placeholder = { Text("Describe the surroundings, sunlight, modular kitchen, maintenance...") },
                    minLines = 3,
                    maxLines = 5,
                    modifier = Modifier.fillMaxWidth().testTag("property_form_description")
                )
            }

            // Section 2: Pricing & Measurements
            item {
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                FormSectionTitle(title = "Pricing & Measurements")
            }

            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = rentText,
                        onValueChange = { rentText = it.filter { char -> char.isDigit() } },
                        label = { Text("Monthly Rent (₹) *") },
                        placeholder = { Text("12000") },
                        singleLine = true,
                        modifier = Modifier.weight(1f).testTag("property_form_rent")
                    )
                    OutlinedTextField(
                        value = depositText,
                        onValueChange = { depositText = it.filter { char -> char.isDigit() } },
                        label = { Text("Security Deposit (₹)") },
                        placeholder = { Text("24000") },
                        singleLine = true,
                        modifier = Modifier.weight(1f).testTag("property_form_deposit")
                    )
                }
            }

            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = areaText,
                        onValueChange = { areaText = it.filter { char -> char.isDigit() } },
                        label = { Text("Area (sq ft) *") },
                        placeholder = { Text("950") },
                        singleLine = true,
                        modifier = Modifier.weight(1f).testTag("property_form_area")
                    )
                    OutlinedTextField(
                        value = bedrooms,
                        onValueChange = { bedrooms = it.filter { char -> char.isDigit() } },
                        label = { Text("Bedrooms (BHK)") },
                        placeholder = { Text("2") },
                        singleLine = true,
                        modifier = Modifier.weight(1f).testTag("property_form_bedrooms")
                    )
                    OutlinedTextField(
                        value = bathrooms,
                        onValueChange = { bathrooms = it.filter { char -> char.isDigit() } },
                        label = { Text("Baths") },
                        placeholder = { Text("2") },
                        singleLine = true,
                        modifier = Modifier.weight(1f).testTag("property_form_bathrooms")
                    )
                }
            }

            // Section 3: Location & Interactive Map
            item {
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                FormSectionTitle(title = "Location & OpenStreetMap Pin")
            }

            item {
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Complete Address *") },
                    placeholder = { Text("House 302, Green Valley Enclave") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("property_form_address")
                )
            }

            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = locality,
                        onValueChange = { locality = it },
                        label = { Text("Locality / Area") },
                        placeholder = { Text("Railway Colony") },
                        singleLine = true,
                        modifier = Modifier.weight(1f).testTag("property_form_locality")
                    )
                    OutlinedTextField(
                        value = city,
                        onValueChange = { city = it },
                        label = { Text("City *") },
                        placeholder = { Text("Kalka") },
                        singleLine = true,
                        modifier = Modifier.weight(1f).testTag("property_form_city")
                    )
                }
            }

            // Section 3: Location on Map (Feature Locked / Currently Not Available)
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Feature Locked",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Select Location from Map",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.errorContainer
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onErrorContainer,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Not Available",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Map location selection is currently locked and not available in this release. Please enter your Property Address, Locality, and City manually above.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Visual Locked Map Placeholder with Lock Icon
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.surface,
                                    shadowElevation = 2.dp,
                                    modifier = Modifier.size(44.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Lock,
                                            contentDescription = "Feature Locked",
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Map Location Locked (Coming Soon)",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // Section 4: Specifications & Furnishing
            item {
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                FormSectionTitle(title = "Specifications & Amenities")
            }

            item {
                ExposedDropdownMenuBox(
                    expanded = furnishingDropdownExpanded,
                    onExpandedChange = { furnishingDropdownExpanded = it }
                ) {
                    OutlinedTextField(
                        value = furnishingStatus.displayName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Furnishing Status") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = furnishingDropdownExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = furnishingDropdownExpanded,
                        onDismissRequest = { furnishingDropdownExpanded = false }
                    ) {
                        FurnishingStatus.entries.forEach { f ->
                            DropdownMenuItem(
                                text = { Text(f.displayName) },
                                onClick = {
                                    furnishingStatus = f
                                    furnishingDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            item {
                ExposedDropdownMenuBox(
                    expanded = waterDropdownExpanded,
                    onExpandedChange = { waterDropdownExpanded = it }
                ) {
                    OutlinedTextField(
                        value = waterSupply.displayName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Water Supply") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = waterDropdownExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = waterDropdownExpanded,
                        onDismissRequest = { waterDropdownExpanded = false }
                    ) {
                        WaterSupply.entries.forEach { w ->
                            DropdownMenuItem(
                                text = { Text(w.displayName) },
                                onClick = {
                                    waterSupply = w
                                    waterDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Parking Available")
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
                        Text("Electricity Backup")
                        Switch(checked = hasElectricityBackup, onCheckedChange = { hasElectricityBackup = it })
                    }
                }
            }

            // Amenities Tags
            item {
                Text(
                    text = "Select Amenities",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))

                val availableAmenities = listOf(
                    "Car Parking", "Bike Parking", "Lift", "Power Backup", "Air Conditioning",
                    "Geyser", "Modular Kitchen", "Hill View", "Security 24x7", "Wi-Fi", "RO Water",
                    "Gym", "Swimming Pool", "Park Facing"
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    availableAmenities.forEach { amenity ->
                        val isSelected = amenities.contains(amenity)
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                if (isSelected) amenities.remove(amenity)
                                else amenities.add(amenity)
                            },
                            label = { Text(amenity) },
                            leadingIcon = if (isSelected) {
                                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                            } else null
                        )
                    }
                }
            }

            // Section 5: Photos & Images
            item {
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                FormSectionTitle(title = "Property Photos (${imageUrls.size})")
                Text(
                    text = "Add photos using your Camera or Gallery. The first photo will be used as the primary display image.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Camera and Gallery Quick Upload Buttons
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // 1. Camera Button
                    Button(
                        onClick = {
                            val hasCamPermission = ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.CAMERA
                            ) == PackageManager.PERMISSION_GRANTED

                            if (hasCamPermission) {
                                val pair = PropertyPhotoManager.createTempCameraUri(context)
                                if (pair != null) {
                                    tempCameraUri = pair.first
                                    tempCameraFile = pair.second
                                    takePictureLauncher.launch(pair.first)
                                } else {
                                    takePreviewLauncher.launch(null)
                                }
                            } else {
                                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("take_photo_camera_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Take Photo", fontWeight = FontWeight.Bold)
                    }

                    // 2. Gallery Button
                    OutlinedButton(
                        onClick = {
                            galleryPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("pick_from_gallery_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("From Gallery", fontWeight = FontWeight.Bold)
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = newImageUrlInput,
                        onValueChange = { newImageUrlInput = it },
                        placeholder = { Text("Paste image URL...") },
                        singleLine = true,
                        modifier = Modifier.weight(1f).testTag("property_form_new_image_input")
                    )
                    Button(
                        onClick = {
                            if (newImageUrlInput.isNotBlank()) {
                                imageUrls.add(newImageUrlInput.trim())
                                newImageUrlInput = ""
                            }
                        },
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Add")
                    }
                }
            }

            // Image Thumbnails Row (User's Real Uploaded Photos)
            if (imageUrls.isNotEmpty()) {
                item {
                    Text(
                        text = "Uploaded Photos (${imageUrls.size}) - Tap to set primary",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Image Thumbnails Row
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    imageUrls.forEachIndexed { index, url ->
                        Box(
                            modifier = Modifier
                                .size(110.dp)
                                .clip(RoundedCornerShape(12.dp))
                        ) {
                            AsyncImage(
                                model = url,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )

                            // Primary badge
                            if (index == 0) {
                                Surface(
                                    color = MaterialTheme.colorScheme.primary,
                                    shape = RoundedCornerShape(bottomEnd = 8.dp),
                                    modifier = Modifier.align(Alignment.TopStart)
                                ) {
                                    Text(
                                        text = "Primary",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            // Remove Button
                            IconButton(
                                onClick = { imageUrls.removeAt(index) },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .size(28.dp)
                                    .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Remove", tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }

            // Section 6: Contact Details
            item {
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                FormSectionTitle(title = "Contact Information")
            }

            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = contactPhone,
                        onValueChange = { contactPhone = it },
                        label = { Text("Call Phone *") },
                        placeholder = { Text("+91 98765 43210") },
                        singleLine = true,
                        modifier = Modifier.weight(1f).testTag("property_form_phone")
                    )
                    OutlinedTextField(
                        value = whatsappNumber,
                        onValueChange = { whatsappNumber = it },
                        label = { Text("WhatsApp Number *") },
                        placeholder = { Text("919876543210") },
                        singleLine = true,
                        modifier = Modifier.weight(1f).testTag("property_form_whatsapp")
                    )
                }
            }

            // Submit Button
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        val rent = rentText.toIntOrNull() ?: 0
                        val area = areaText.toIntOrNull() ?: 0

                        if (title.isBlank()) {
                            Toast.makeText(context, "Please enter a property title", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (rent <= 0) {
                            Toast.makeText(context, "Please enter a valid monthly rent", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (area <= 0) {
                            Toast.makeText(context, "Please enter a valid area in sq ft", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (address.isBlank() || city.isBlank()) {
                            Toast.makeText(context, "Please enter address and city", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        val propertyToSave = Property(
                            id = existingProperty?.id ?: UUID.randomUUID().toString(),
                            ownerId = currentUser.id,
                            ownerName = currentUser.fullName.ifBlank { "Property Owner" },
                            title = title.trim(),
                            description = description.trim(),
                            propertyType = propertyType,
                            rent = rent,
                            securityDeposit = depositText.toIntOrNull() ?: (rent * 2),
                            areaSqft = area,
                            bedrooms = bedrooms.toIntOrNull() ?: 1,
                            bathrooms = bathrooms.toIntOrNull() ?: 1,
                            address = address.trim(),
                            locality = locality.trim(),
                            city = city.trim(),
                            latitude = latitude,
                            longitude = longitude,
                            furnishingStatus = furnishingStatus,
                            hasParking = hasParking,
                            waterSupply = waterSupply,
                            hasElectricityBackup = hasElectricityBackup,
                            hasBalcony = hasBalcony,
                            amenities = amenities.toList(),
                            contactPhone = contactPhone.trim(),
                            whatsappNumber = whatsappNumber.trim(),
                            listedBy = listedBy,
                            status = status,
                            isFeatured = existingProperty?.isFeatured ?: false,
                            imageUrls = imageUrls.toList(),
                            isSample = false,
                            createdAt = existingProperty?.createdAt ?: System.currentTimeMillis(),
                            updatedAt = System.currentTimeMillis()
                        )

                        if (existingProperty != null) {
                            viewModel.updateProperty(propertyToSave) {
                                Toast.makeText(context, "Property updated successfully!", Toast.LENGTH_SHORT).show()
                                onNavigateBack()
                            }
                        } else {
                            viewModel.saveProperty(propertyToSave) {
                                Toast.makeText(context, "Property published successfully!", Toast.LENGTH_SHORT).show()
                                onNavigateBack()
                            }
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("save_property_button")
                ) {
                    Text(
                        text = if (existingProperty != null) "Update Property" else "Publish Property",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun FormSectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary
    )
}
