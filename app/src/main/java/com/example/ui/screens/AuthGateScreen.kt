package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserRole
import com.example.ui.viewmodel.RentNearViewModel

@Composable
fun AuthGateScreen(
    viewModel: RentNearViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedTabIndex by remember { mutableIntStateOf(0) } // 0 = Sign In, 1 = Sign Up

    var fullName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var selectedRole by remember { mutableStateOf(UserRole.TENANT) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val scrollState = rememberScrollState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                        MaterialTheme.colorScheme.surface
                    )
                )
            )
            .imePadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // App Logo & Branding Hero
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = "RentNear",
                    tint = Color.White,
                    modifier = Modifier.size(42.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "RentNear",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = "Find, Rent & Manage Properties Near You",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Main Auth Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("auth_gate_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Sign In / Sign Up Tabs
                    TabRow(
                        selectedTabIndex = selectedTabIndex,
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .fillMaxWidth()
                    ) {
                        Tab(
                            selected = selectedTabIndex == 0,
                            onClick = {
                                selectedTabIndex = 0
                                errorMessage = null
                            },
                            text = {
                                Text(
                                    text = "Sign In",
                                    fontWeight = if (selectedTabIndex == 0) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            modifier = Modifier.testTag("tab_sign_in")
                        )
                        Tab(
                            selected = selectedTabIndex == 1,
                            onClick = {
                                selectedTabIndex = 1
                                errorMessage = null
                            },
                            text = {
                                Text(
                                    text = "Sign Up",
                                    fontWeight = if (selectedTabIndex == 1) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            modifier = Modifier.testTag("tab_sign_up")
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Sign Up Specific Fields: Full Name & Phone
                    if (selectedTabIndex == 1) {
                        OutlinedTextField(
                            value = fullName,
                            onValueChange = { fullName = it },
                            label = { Text("Full Name *") },
                            leadingIcon = {
                                Icon(Icons.Default.Person, contentDescription = null)
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("auth_gate_name_input")
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            label = { Text("Phone Number *") },
                            leadingIcon = {
                                Icon(Icons.Default.Phone, contentDescription = null)
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("auth_gate_phone_input")
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Role Selector
                        Text(
                            text = "I want to use RentNear as:",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.align(Alignment.Start)
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            FilterChip(
                                selected = selectedRole == UserRole.TENANT,
                                onClick = { selectedRole = UserRole.TENANT },
                                label = { Text("Tenant / Seeker") },
                                leadingIcon = {
                                    Icon(Icons.Default.Home, contentDescription = null, modifier = Modifier.size(16.dp))
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("role_tenant_chip"),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                                )
                            )

                            FilterChip(
                                selected = selectedRole == UserRole.OWNER,
                                onClick = { selectedRole = UserRole.OWNER },
                                label = { Text("Property Owner") },
                                leadingIcon = {
                                    Icon(Icons.Default.Business, contentDescription = null, modifier = Modifier.size(16.dp))
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("role_owner_chip"),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // Common Email Field
                    OutlinedTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            errorMessage = null
                        },
                        label = { Text("Email Address *") },
                        leadingIcon = {
                            Icon(Icons.Default.Email, contentDescription = null)
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("auth_gate_email_input")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Common Password Field
                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            errorMessage = null
                        },
                        label = { Text("Password * (min. 6 characters)") },
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = null)
                        },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = if (passwordVisible) "Hide password" else "Show password"
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                performAuth(
                                    isSignUp = selectedTabIndex == 1,
                                    email = email,
                                    password = password,
                                    fullName = fullName,
                                    phone = phone,
                                    role = selectedRole,
                                    viewModel = viewModel,
                                    setLoading = { isLoading = it },
                                    onError = { errorMessage = it },
                                    onSuccess = {
                                        Toast.makeText(context, "Welcome to RentNear!", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("auth_gate_password_input")
                    )

                    // Error Message Banner
                    AnimatedVisibility(visible = errorMessage != null) {
                        errorMessage?.let {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = it,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Primary Submit Button
                    Button(
                        onClick = {
                            performAuth(
                                isSignUp = selectedTabIndex == 1,
                                email = email,
                                password = password,
                                fullName = fullName,
                                phone = phone,
                                role = selectedRole,
                                viewModel = viewModel,
                                setLoading = { isLoading = it },
                                onError = { errorMessage = it },
                                onSuccess = {
                                    Toast.makeText(context, "Welcome to RentNear!", Toast.LENGTH_SHORT).show()
                                }
                            )
                        },
                        enabled = !isLoading,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("auth_gate_submit_button")
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = if (selectedTabIndex == 0) "Sign In to RentNear" else "Create Account & Enter",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "By signing in, you agree to discover genuine, verified rental houses and shops with direct owner contacts.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}

private fun performAuth(
    isSignUp: Boolean,
    email: String,
    password: String,
    fullName: String,
    phone: String,
    role: UserRole,
    viewModel: RentNearViewModel,
    setLoading: (Boolean) -> Unit,
    onError: (String) -> Unit,
    onSuccess: () -> Unit
) {
    val trimmedEmail = email.trim()
    val trimmedPass = password.trim()

    if (trimmedEmail.isBlank()) {
        onError("Please enter your email address")
        return
    }
    if (!android.util.Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches()) {
        onError("Please enter a valid email address (e.g., name@gmail.com)")
        return
    }
    if (trimmedPass.length < 6) {
        onError("Password must be at least 6 characters")
        return
    }

    if (isSignUp) {
        if (fullName.trim().isBlank()) {
            onError("Please enter your full name")
            return
        }
        if (phone.trim().isBlank()) {
            onError("Please enter your contact phone number")
            return
        }

        setLoading(true)
        viewModel.signup(trimmedEmail, trimmedPass, fullName.trim(), phone.trim(), role) { result ->
            setLoading(false)
            if (result.isSuccess) {
                onSuccess()
            } else {
                onError(result.exceptionOrNull()?.message ?: "Signup failed. Please try again.")
            }
        }
    } else {
        setLoading(true)
        viewModel.login(trimmedEmail, trimmedPass) { result ->
            setLoading(false)
            if (result.isSuccess) {
                onSuccess()
            } else {
                onError(result.exceptionOrNull()?.message ?: "Login failed. Please check your credentials.")
            }
        }
    }
}
