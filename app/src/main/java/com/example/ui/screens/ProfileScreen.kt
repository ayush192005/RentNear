package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.data.model.UserRole
import com.example.ui.i18n.AppLanguage
import com.example.ui.i18n.LanguageManager
import com.example.ui.theme.AppThemeMode
import com.example.ui.theme.ThemeManager
import com.example.ui.viewmodel.RentNearViewModel
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(
    viewModel: RentNearViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val currentUser by viewModel.currentUser.collectAsState()
    val currentLanguage by LanguageManager.currentLanguage.collectAsState()
    val themeMode by ThemeManager.themeMode.collectAsState()

    var showAuthDialog by remember { mutableStateOf(false) }
    var isSignUpMode by remember { mutableStateOf(false) }
    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showSupabaseConfigDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("profile_screen"),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Profile Header Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = currentUser.fullName.firstOrNull()?.uppercase() ?: "U",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = currentUser.fullName,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = currentUser.email,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (currentUser.phone.isNotBlank()) {
                                Text(
                                    text = currentUser.phone,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }

                        IconButton(
                            onClick = { showEditProfileDialog = true },
                            modifier = Modifier.testTag("edit_profile_button")
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit Profile")
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Role pill & Switcher (Updates user's real profile)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = "Role: ${currentUser.role.displayName}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }

                        OutlinedButton(
                            onClick = {
                                val nextRole = if (currentUser.role == UserRole.TENANT) UserRole.OWNER else UserRole.TENANT
                                viewModel.updateProfile(currentUser.fullName, currentUser.phone, nextRole) {
                                    Toast.makeText(context, "Role updated to ${nextRole.displayName}", Toast.LENGTH_SHORT).show()
                                }
                            },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("switch_role_button")
                        ) {
                            Icon(Icons.Default.SwapHoriz, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (currentUser.role == UserRole.TENANT) "Switch to Owner" else "Switch to Tenant")
                        }
                    }
                }
            }
        }

        // 2. Authentication Card (Switch Account / Logout)
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Account & Authentication",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Signed in as ${currentUser.email}. You can switch accounts or sign out.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                isSignUpMode = false
                                showAuthDialog = true
                            },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f).testTag("login_account_button")
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Switch Account")
                        }

                        OutlinedButton(
                            onClick = {
                                viewModel.logout()
                                Toast.makeText(context, "Signed out", Toast.LENGTH_SHORT).show()
                            },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f).testTag("logout_account_button")
                        ) {
                            Icon(Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Log Out")
                        }
                    }
                }
            }
        }

        // 3. Settings: Language & Theme Mode (Dark & White Mode)
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth().testTag("app_settings_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (currentLanguage == AppLanguage.HINDI) "ऐप सेटिंग्स और रूप-रंग" else "Settings & Appearance",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // A. Language Switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Language,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (currentLanguage == AppLanguage.HINDI) "भाषा बदलें" else "App Language",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = if (currentLanguage == AppLanguage.HINDI) "वर्तमान: हिंदी" else "Current: English",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            FilterChip(
                                selected = currentLanguage == AppLanguage.ENGLISH,
                                onClick = { LanguageManager.setLanguage(AppLanguage.ENGLISH) },
                                label = { Text("English") },
                                modifier = Modifier.testTag("lang_english_chip")
                            )
                            FilterChip(
                                selected = currentLanguage == AppLanguage.HINDI,
                                onClick = { LanguageManager.setLanguage(AppLanguage.HINDI) },
                                label = { Text("हिंदी") },
                                modifier = Modifier.testTag("lang_hindi_chip")
                            )
                        }
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 12.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )

                    // B. Dark Mode & White Mode Switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (themeMode == AppThemeMode.DARK) Icons.Default.DarkMode else Icons.Default.LightMode,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (currentLanguage == AppLanguage.HINDI) "डार्क / लाइट मोड" else "Dark & White Theme",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = if (themeMode == AppThemeMode.DARK) "Dark Mode Active" else "White (Light) Mode Active",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = if (themeMode == AppThemeMode.DARK) "Dark" else "White",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Switch(
                                checked = themeMode == AppThemeMode.DARK,
                                onCheckedChange = { isChecked ->
                                    ThemeManager.setThemeMode(if (isChecked) AppThemeMode.DARK else AppThemeMode.LIGHT)
                                },
                                modifier = Modifier.testTag("theme_switch")
                            )
                        }
                    }
                }
            }
        }

        // 3. Supabase PostgreSQL Configuration Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
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
                                imageVector = if (viewModel.supabaseHelper.isConfigured) Icons.Default.CloudDone else Icons.Default.CloudQueue,
                                contentDescription = null,
                                tint = if (viewModel.supabaseHelper.isConfigured) Color(0xFF10B981) else MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Supabase PostgreSQL Database",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (viewModel.supabaseHelper.isConfigured) Color(0xFF10B981).copy(alpha = 0.2f)
                            else MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = if (viewModel.supabaseHelper.isConfigured) "Configured" else "Local Room Mode",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (viewModel.supabaseHelper.isConfigured) Color(0xFF047857)
                                else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Full database SQL script with tables, RLS policies, and indexes is provided in 'supabase_schema.sql'. You can plug in your project URL and Anon Key below.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = { showSupabaseConfigDialog = true },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().testTag("configure_supabase_button")
                    ) {
                        Icon(Icons.Default.Key, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Configure Supabase URL & Key")
                    }
                }
            }
        }

        // 5. Gemini AI Search Information Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "AI Natural-Language Search",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "Extracts structured criteria (e.g. 2 BHK, budget, Kalka, parking) and queries the real database without inventing fake listings.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }
    }

    // Auth Dialog (Login / Signup)
    if (showAuthDialog) {
        AuthDialog(
            isSignUp = isSignUpMode,
            onDismiss = { showAuthDialog = false },
            onLogin = { email, pass ->
                viewModel.login(email, pass) { result ->
                    if (result.isSuccess) {
                        Toast.makeText(context, "Logged in successfully!", Toast.LENGTH_SHORT).show()
                        showAuthDialog = false
                    } else {
                        Toast.makeText(context, result.exceptionOrNull()?.message ?: "Login failed", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            onSignUp = { email, pass, name, phone, role ->
                viewModel.signup(email, pass, name, phone, role) { result ->
                    if (result.isSuccess) {
                        Toast.makeText(context, "Account created successfully!", Toast.LENGTH_SHORT).show()
                        showAuthDialog = false
                    } else {
                        Toast.makeText(context, result.exceptionOrNull()?.message ?: "Signup failed", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        )
    }

    // Edit Profile Dialog
    if (showEditProfileDialog) {
        EditProfileDialog(
            currentUser = currentUser,
            onDismiss = { showEditProfileDialog = false },
            onSave = { name, phone, role ->
                viewModel.updateProfile(name, phone, role) { result ->
                    if (result.isSuccess) {
                        Toast.makeText(context, "Profile updated!", Toast.LENGTH_SHORT).show()
                        showEditProfileDialog = false
                    }
                }
            }
        )
    }

    // Supabase Configuration Dialog
    if (showSupabaseConfigDialog) {
        SupabaseConfigDialog(
            helper = viewModel.supabaseHelper,
            onDismiss = { showSupabaseConfigDialog = false },
            onSaved = {
                showSupabaseConfigDialog = false
                viewModel.refreshRemoteData()
                Toast.makeText(context, "Supabase configured! Syncing listings...", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

@Composable
private fun AuthDialog(
    isSignUp: Boolean,
    onDismiss: () -> Unit,
    onLogin: (String, String) -> Unit,
    onSignUp: (String, String, String, String, UserRole) -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var fullName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var role by remember { mutableStateOf(UserRole.TENANT) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isSignUp) "Create RentNear Account" else "Login to RentNear") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (isSignUp) {
                    OutlinedTextField(
                        value = fullName,
                        onValueChange = { fullName = it },
                        label = { Text("Full Name *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("auth_dialog_name")
                    )
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Phone Number") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("auth_dialog_phone")
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = role == UserRole.TENANT,
                            onClick = { role = UserRole.TENANT },
                            label = { Text("Tenant / Seeker") }
                        )
                        FilterChip(
                            selected = role == UserRole.OWNER,
                            onClick = { role = UserRole.OWNER },
                            label = { Text("Property Owner") }
                        )
                    }
                }

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("auth_dialog_email")
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password *") },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("auth_dialog_password")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (isSignUp) {
                        onSignUp(email, password, fullName, phone, role)
                    } else {
                        onLogin(email, password)
                    }
                },
                modifier = Modifier.testTag("auth_dialog_submit_button")
            ) {
                Text(if (isSignUp) "Sign Up" else "Login")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun EditProfileDialog(
    currentUser: com.example.data.model.UserProfile,
    onDismiss: () -> Unit,
    onSave: (String, String, UserRole) -> Unit
) {
    var fullName by remember { mutableStateOf(currentUser.fullName) }
    var phone by remember { mutableStateOf(currentUser.phone) }
    var role by remember { mutableStateOf(currentUser.role) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Profile") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text("Full Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = role == UserRole.TENANT,
                        onClick = { role = UserRole.TENANT },
                        label = { Text("Tenant") }
                    )
                    FilterChip(
                        selected = role == UserRole.OWNER,
                        onClick = { role = UserRole.OWNER },
                        label = { Text("Owner") }
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = { onSave(fullName, phone, role) }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun SupabaseConfigDialog(
    helper: com.example.data.supabase.SupabaseHelper,
    onDismiss: () -> Unit,
    onSaved: () -> Unit = onDismiss
) {
    val coroutineScope = rememberCoroutineScope()
    var url by remember { mutableStateOf(helper.supabaseUrl) }
    var key by remember { mutableStateOf(helper.supabaseAnonKey) }
    var testStatus by remember { mutableStateOf<String?>(null) }
    var isTesting by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Supabase Configuration") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Configure your Supabase PostgreSQL project URL and anon public key:",
                    style = MaterialTheme.typography.bodySmall
                )

                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text("Supabase URL") },
                    placeholder = { Text("https://xyzcompany.supabase.co") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("supabase_url_input")
                )

                OutlinedTextField(
                    value = key,
                    onValueChange = { key = it },
                    label = { Text("Supabase Anon Key") },
                    placeholder = { Text("eyJhbGciOiJIUzI1NiIsInR5cCI6...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("supabase_key_input")
                )

                OutlinedButton(
                    onClick = {
                        helper.setCredentials(url, key)
                        isTesting = true
                        testStatus = null
                        coroutineScope.launch {
                            val res = helper.testConnection()
                            isTesting = false
                            testStatus = if (res.isSuccess) res.getOrNull() else "Error: ${res.exceptionOrNull()?.message}"
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isTesting) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    Text("Test Connection")
                }

                if (!testStatus.isNullOrBlank()) {
                    Text(
                        text = testStatus!!,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (testStatus!!.startsWith("Connected")) Color(0xFF059669) else MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    helper.setCredentials(url, key)
                    onSaved()
                }
            ) {
                Text("Save & Sync")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}
