package com.example.ui.components

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.View
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.RenderProcessGoneDetail
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

/**
 * Crash-resilient WebViewClient that handles Chromium renderer process termination
 * without letting Android kill the host application.
 */
class SafeWebViewClient(
    private val onProcessGone: () -> Unit
) : WebViewClient() {
    override fun onRenderProcessGone(view: WebView?, detail: RenderProcessGoneDetail?): Boolean {
        try {
            view?.let {
                (it.parent as? ViewGroup)?.removeView(it)
                it.destroy()
            }
        } catch (_: Exception) {}
        onProcessGone()
        // Returning true informs Android the host app handled the situation, preventing crash code -1
        return true
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun LeafletPropertyMapView(
    latitude: Double,
    longitude: Double,
    title: String,
    address: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lat = if (latitude != 0.0) latitude else 30.8354
    val lng = if (longitude != 0.0) longitude else 76.9362

    var hasRendererCrashed by remember { mutableStateOf(false) }
    var webViewInstance by remember { mutableStateOf<WebView?>(null) }

    DisposableEffect(Unit) {
        onDispose {
            try {
                webViewInstance?.let {
                    (it.parent as? ViewGroup)?.removeView(it)
                    it.destroy()
                }
            } catch (_: Exception) {}
        }
    }

    val safeTitle = remember(title) {
        title.replace("'", "\\'").replace("\"", "\\\"").replace("\n", " ")
    }
    val safeAddress = remember(address) {
        address.replace("'", "\\'").replace("\"", "\\\"").replace("\n", " ")
    }

    val htmlContent = remember(lat, lng, safeTitle, safeAddress) {
        """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="utf-8" />
            <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no" />
            <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css" />
            <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>
            <style>
                html, body, #map { height: 100%; width: 100%; margin: 0; padding: 0; background: #e2e8f0; }
                .leaflet-popup-content-wrapper { border-radius: 12px; box-shadow: 0 4px 12px rgba(0,0,0,0.15); font-family: sans-serif; }
                .popup-title { font-weight: bold; color: #0F766E; font-size: 13px; margin-bottom: 2px; }
                .popup-address { color: #475569; font-size: 11px; }
            </style>
        </head>
        <body>
            <div id="map"></div>
            <script>
                try {
                    var map = L.map('map', { zoomControl: true, attributionControl: false }).setView([$lat, $lng], 15);
                    L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png', {
                        maxZoom: 18
                    }).addTo(map);

                    var marker = L.marker([$lat, $lng]).addTo(map);
                    marker.bindPopup('<div class="popup-title">$safeTitle</div><div class="popup-address">$safeAddress</div>').openPopup();

                    setTimeout(function() { map.invalidateSize(); }, 350);
                } catch(e) {
                    console.error(e);
                }
            </script>
        </body>
        </html>
        """.trimIndent()
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .testTag("leaflet_property_map_container")
    ) {
        if (!hasRendererCrashed) {
            AndroidView(
                factory = { ctx ->
                    WebView(ctx).apply {
                        webViewInstance = this
                        // Crucial for virtualized emulators: use software layer to prevent GPU driver crash code -1
                        setLayerType(View.LAYER_TYPE_SOFTWARE, null)

                        settings.apply {
                            javaScriptEnabled = true
                            domStorageEnabled = true
                            useWideViewPort = true
                            loadWithOverviewMode = true
                            cacheMode = WebSettings.LOAD_DEFAULT
                        }
                        webChromeClient = WebChromeClient()
                        webViewClient = SafeWebViewClient(
                            onProcessGone = {
                                hasRendererCrashed = true
                            }
                        )
                        loadDataWithBaseURL("https://rentnear.app", htmlContent, "text/html", "UTF-8", null)
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // Graceful Fallback if emulator renderer process crashes
            MapFallbackView(
                latitude = lat,
                longitude = lng,
                title = title,
                address = address,
                onRetry = {
                    hasRendererCrashed = false
                }
            )
        }
    }
}

class LocationPickerInterface(
    private val onLocationChanged: (Double, Double) -> Unit
) {
    @JavascriptInterface
    fun onLocationSelected(lat: Double, lng: Double) {
        onLocationChanged(lat, lng)
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun LeafletLocationPickerView(
    initialLatitude: Double,
    initialLongitude: Double,
    onLocationSelected: (Double, Double) -> Unit,
    modifier: Modifier = Modifier
) {
    val lat = if (initialLatitude != 0.0) initialLatitude else 30.8354
    val lng = if (initialLongitude != 0.0) initialLongitude else 76.9362

    var hasRendererCrashed by remember { mutableStateOf(false) }
    var webViewInstance by remember { mutableStateOf<WebView?>(null) }
    val jsInterface = remember { LocationPickerInterface(onLocationSelected) }

    DisposableEffect(Unit) {
        onDispose {
            try {
                webViewInstance?.let {
                    (it.parent as? ViewGroup)?.removeView(it)
                    it.destroy()
                }
            } catch (_: Exception) {}
        }
    }

    val htmlContent = remember(lat, lng) {
        """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="utf-8" />
            <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no" />
            <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css" />
            <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>
            <style>
                html, body, #map { height: 100%; width: 100%; margin: 0; padding: 0; background: #e2e8f0; }
                .hint-box {
                    position: absolute; top: 10px; left: 50%; transform: translateX(-50%);
                    z-index: 1000; background: rgba(15, 118, 110, 0.9); color: white;
                    padding: 6px 14px; border-radius: 20px; font-size: 12px; font-family: sans-serif;
                    box-shadow: 0 2px 8px rgba(0,0,0,0.2); pointer-events: none;
                }
            </style>
        </head>
        <body>
            <div class="hint-box">Tap or drag pin to select location</div>
            <div id="map"></div>
            <script>
                try {
                    var map = L.map('map', { zoomControl: true, attributionControl: false }).setView([$lat, $lng], 14);
                    L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png', {
                        maxZoom: 18
                    }).addTo(map);

                    var marker = L.marker([$lat, $lng], { draggable: true }).addTo(map);

                    function notifyKotlin(lat, lng) {
                        if (window.LocationPickerBridge) {
                            window.LocationPickerBridge.onLocationSelected(lat, lng);
                        }
                    }

                    marker.on('dragend', function(e) {
                        var position = marker.getLatLng();
                        notifyKotlin(position.lat, position.lng);
                    });

                    map.on('click', function(e) {
                        marker.setLatLng(e.latlng);
                        notifyKotlin(e.latlng.lat, e.latlng.lng);
                    });

                    setTimeout(function() { map.invalidateSize(); }, 350);
                } catch(e) {
                    console.error(e);
                }
            </script>
        </body>
        </html>
        """.trimIndent()
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .testTag("leaflet_location_picker_container")
    ) {
        if (!hasRendererCrashed) {
            AndroidView(
                factory = { ctx ->
                    WebView(ctx).apply {
                        webViewInstance = this
                        // Crucial for virtualized emulators: use software layer to prevent GPU driver crash code -1
                        setLayerType(View.LAYER_TYPE_SOFTWARE, null)

                        settings.apply {
                            javaScriptEnabled = true
                            domStorageEnabled = true
                            useWideViewPort = true
                            loadWithOverviewMode = true
                            cacheMode = WebSettings.LOAD_DEFAULT
                        }
                        webChromeClient = WebChromeClient()
                        webViewClient = SafeWebViewClient(
                            onProcessGone = {
                                hasRendererCrashed = true
                            }
                        )
                        addJavascriptInterface(jsInterface, "LocationPickerBridge")
                        loadDataWithBaseURL("https://rentnear.app", htmlContent, "text/html", "UTF-8", null)
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            MapFallbackView(
                latitude = lat,
                longitude = lng,
                title = "Selected Location",
                address = "Coordinates: ${"%.4f".format(lat)}, ${"%.4f".format(lng)}",
                onRetry = {
                    hasRendererCrashed = false
                }
            )
        }
    }
}

@Composable
private fun MapFallbackView(
    latitude: Double,
    longitude: Double,
    title: String,
    address: String,
    onRetry: () -> Unit
) {
    val context = LocalContext.current

    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(36.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = address,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "GPS: ${"%.4f".format(latitude)}, ${"%.4f".format(longitude)}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline
            )
            Spacer(modifier = Modifier.height(10.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onRetry) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Reload Map")
                }

                Button(
                    onClick = {
                        val gmmIntentUri = Uri.parse("geo:$latitude,$longitude?q=$latitude,$longitude($title)")
                        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                        try {
                            context.startActivity(mapIntent)
                        } catch (_: Exception) {
                            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.openstreetmap.org/?mlat=$latitude&mlon=$longitude#map=16/$latitude/$longitude"))
                            context.startActivity(browserIntent)
                        }
                    }
                ) {
                    Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Open Map")
                }
            }
        }
    }
}
