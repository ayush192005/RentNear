package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.example.ui.i18n.AppLanguage

enum class ContactActionType {
    CALL,
    WHATSAPP
}

/**
 * Lead Capture Dialog
 *
 * Prompts the user to enter their Name, Phone number, and an optional message
 * before unlocking access to the owner's phone or WhatsApp.
 * Logs and verifies initial contact to keep the platform secure and spam-free.
 */
@Composable
fun LeadCaptureDialog(
    propertyTitle: String,
    initialUserName: String = "",
    initialUserPhone: String = "",
    actionType: ContactActionType = ContactActionType.CALL,
    currentLanguage: AppLanguage = AppLanguage.ENGLISH,
    onDismiss: () -> Unit,
    onSubmit: (name: String, phone: String, message: String) -> Unit
) {
    var name by remember { mutableStateOf(initialUserName) }
    var phone by remember { mutableStateOf(initialUserPhone) }
    var message by remember {
        mutableStateOf(
            if (currentLanguage == AppLanguage.HINDI)
                "नमस्ते, मैं आपकी इस प्रॉपर्टी में रुचि रखता हूँ।"
            else
                "Hi, I am interested in this rental property and would like to arrange a visit."
        )
    }

    var nameError by remember { mutableStateOf(false) }
    var phoneError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier
            .fillMaxWidth(0.92f)
            .testTag("lead_capture_dialog"),
        shape = RoundedCornerShape(24.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(42.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (actionType == ContactActionType.CALL) Icons.Default.Call else Icons.AutoMirrored.Filled.Chat,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = if (currentLanguage == AppLanguage.HINDI)
                                "मालिक से संपर्क करें"
                            else
                                "Contact Property Owner",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = if (actionType == ContactActionType.CALL)
                                (if (currentLanguage == AppLanguage.HINDI) "कॉल विवरण अनलॉक करें" else "Unlock Phone Number")
                            else
                                (if (currentLanguage == AppLanguage.HINDI) "व्हाट्सएप चैट शुरू करें" else "Start WhatsApp Chat"),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("lead_capture_close_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Verification & Privacy Notice Badge
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (currentLanguage == AppLanguage.HINDI)
                                "मालिक का नंबर देखने के लिए कृपया अपना नाम और फोन नंबर सत्यापित करें।"
                            else
                                "Please provide your details below to log this inquiry and access direct owner contact.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Property Reference
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = if (currentLanguage == AppLanguage.HINDI) "प्रॉपर्टी:" else "Inquiring about:",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = propertyTitle,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 2
                        )
                    }
                }

                // User Full Name Input
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        if (it.isNotBlank()) nameError = false
                    },
                    label = {
                        Text(if (currentLanguage == AppLanguage.HINDI) "आपका नाम *" else "Your Full Name *")
                    },
                    leadingIcon = {
                        Icon(Icons.Default.Person, contentDescription = null)
                    },
                    isError = nameError,
                    supportingText = {
                        if (nameError) {
                            Text(
                                if (currentLanguage == AppLanguage.HINDI) "कृपया अपना नाम दर्ज करें" else "Please enter your name",
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("lead_capture_name_input")
                )

                // User Phone Number Input
                OutlinedTextField(
                    value = phone,
                    onValueChange = {
                        phone = it
                        if (it.length >= 7) phoneError = false
                    },
                    label = {
                        Text(if (currentLanguage == AppLanguage.HINDI) "आपका फोन नंबर *" else "Your Phone Number *")
                    },
                    leadingIcon = {
                        Icon(Icons.Default.Phone, contentDescription = null)
                    },
                    isError = phoneError,
                    supportingText = {
                        if (phoneError) {
                            Text(
                                if (currentLanguage == AppLanguage.HINDI) "मान्य 10 अंकों का फोन नंबर दर्ज करें" else "Enter a valid phone number (at least 10 digits)",
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("lead_capture_phone_input")
                )

                // Message to Owner Input
                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    label = {
                        Text(if (currentLanguage == AppLanguage.HINDI) "संदेश (वैकल्पिक)" else "Message to Owner (Optional)")
                    },
                    leadingIcon = {
                        Icon(Icons.Default.Message, contentDescription = null)
                    },
                    maxLines = 3,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("lead_capture_message_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val cleanName = name.trim()
                    val cleanPhone = phone.trim()
                    val hasValidName = cleanName.isNotBlank()
                    val hasValidPhone = cleanPhone.replace(Regex("[^0-9]"), "").length >= 7

                    nameError = !hasValidName
                    phoneError = !hasValidPhone

                    if (hasValidName && hasValidPhone) {
                        onSubmit(cleanName, cleanPhone, message.trim())
                    }
                },
                shape = RoundedCornerShape(12.dp),
                colors = if (actionType == ContactActionType.WHATSAPP) {
                    ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366), contentColor = Color.White)
                } else {
                    ButtonDefaults.buttonColors()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("lead_capture_submit_button")
            ) {
                Icon(
                    imageVector = if (actionType == ContactActionType.CALL) Icons.Default.Call else Icons.AutoMirrored.Filled.Chat,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (actionType == ContactActionType.CALL) {
                        if (currentLanguage == AppLanguage.HINDI) "सत्यापित करें और कॉल करें" else "Verify & Call Owner"
                    } else {
                        if (currentLanguage == AppLanguage.HINDI) "सत्यापित करें और चैट करें" else "Verify & WhatsApp Owner"
                    },
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("lead_capture_cancel_button")
            ) {
                Text(if (currentLanguage == AppLanguage.HINDI) "रद्द करें" else "Cancel")
            }
        }
    )
}
