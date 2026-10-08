package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Call
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.net.URLEncoder

object ContactUtils {
    fun callOwner(context: Context, phoneNumber: String) {
        val cleanPhone = phoneNumber.replace(Regex("[^0-9+]"), "")
        if (cleanPhone.isBlank()) {
            Toast.makeText(context, "Phone number is not available", Toast.LENGTH_SHORT).show()
            return
        }
        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:$cleanPhone")
        }
        try {
            context.startActivity(intent)
        } catch (_: Exception) {
            Toast.makeText(context, "No phone app found to make calls", Toast.LENGTH_SHORT).show()
        }
    }

    fun openWhatsApp(context: Context, whatsappNumber: String, propertyTitle: String) {
        var clean = whatsappNumber.replace(Regex("[^0-9]"), "")
        if (clean.isBlank()) {
            Toast.makeText(context, "WhatsApp number is not available", Toast.LENGTH_SHORT).show()
            return
        }
        // If number doesn't have country code, prepend India's 91 as default
        if (clean.length == 10) {
            clean = "91$clean"
        }

        val message = "Hello, I found your property '$propertyTitle' on RentNear and would like more details."
        val encodedMessage = try {
            URLEncoder.encode(message, "UTF-8")
        } catch (_: Exception) {
            message
        }

        val url = "https://wa.me/$clean?text=$encodedMessage"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        try {
            context.startActivity(intent)
        } catch (_: Exception) {
            Toast.makeText(context, "Unable to open WhatsApp", Toast.LENGTH_SHORT).show()
        }
    }
}

@Composable
fun ContactBar(
    phoneNumber: String,
    whatsappNumber: String,
    propertyTitle: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Row(
        modifier = modifier.fillMaxWidth()
    ) {
        // Call Owner Button
        Button(
            onClick = { ContactUtils.callOwner(context, phoneNumber) },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .weight(1f)
                .height(48.dp)
                .testTag("call_owner_button")
        ) {
            Icon(
                imageVector = Icons.Default.Call,
                contentDescription = "Call Owner",
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Call Owner", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.width(12.dp))

        // WhatsApp Button
        Button(
            onClick = { ContactUtils.openWhatsApp(context, whatsappNumber, propertyTitle) },
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF25D366),
                contentColor = Color.White
            ),
            modifier = Modifier
                .weight(1f)
                .height(48.dp)
                .testTag("whatsapp_owner_button")
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Chat,
                contentDescription = "WhatsApp Owner",
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "WhatsApp", fontWeight = FontWeight.Bold)
        }
    }
}
