package com.example.ui.components

import android.annotation.SuppressLint
import android.content.Context
import android.webkit.JavascriptInterface
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.model.Party
import com.example.model.PartyType
import com.example.ui.theme.CartoonBorder
import com.example.ui.theme.PartyCyan
import com.example.ui.theme.PartyGreen
import com.example.ui.theme.PartyPink
import com.example.ui.theme.PartyPurple
import com.example.ui.theme.PartyPurpleDark
import com.example.ui.theme.PartyYellow
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun RealTimeMapView(
  parties: List<Party>,
  userLocation: Pair<Double, Double>,
  selectedParty: Party?,
  onPartySelected: (Party) -> Unit,
  isPickLocationMode: Boolean = false,
  pickedLocation: Pair<Double, Double> = userLocation,
  onLocationPicked: ((Double, Double) -> Unit)? = null,
  onCenterOnMe: () -> Unit = {},
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  var webViewRef by remember { mutableStateOf<WebView?>(null) }
  var isMapReady by remember { mutableStateOf(false) }
  var mapStyleIndex by remember { mutableStateOf(0) } // 0: CartoDB Pastel, 1: OpenStreetMap, 2: Dark Night

  val mapStyles = listOf(
    Pair("🎨 Pastel City", "https://{s}.basemaps.cartocdn.com/rastertiles/voyager/{z}/{x}/{y}{r}.png"),
    Pair("🗺️ Street Map", "https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"),
    Pair("🌙 Dark Rave", "https://{s}.basemaps.cartocdn.com/dark_all/{z}/{x}/{y}{r}.png")
  )

  // JavaScript bridge interface to receive clicks from real-time map
  val jsBridge = remember(parties, onPartySelected, onLocationPicked) {
    object {
      @JavascriptInterface
      fun onPartyClick(partyId: String) {
        val found = parties.find { it.id == partyId }
        if (found != null) {
          scope.launch {
            onPartySelected(found)
          }
        }
      }

      @JavascriptInterface
      fun onMapClick(lat: Double, lng: Double) {
        if (isPickLocationMode && onLocationPicked != null) {
          scope.launch {
            onLocationPicked(lat, lng)
          }
        }
      }

      @JavascriptInterface
      fun onMapLoaded() {
        isMapReady = true
      }
    }
  }

  // Update markers when parties list or selection changes
  LaunchedEffect(parties, isMapReady, selectedParty, isPickLocationMode, pickedLocation) {
    if (isMapReady && webViewRef != null) {
      val partiesArray = JSONArray()
      for (p in parties) {
        val obj = JSONObject().apply {
          put("id", p.id)
          put("name", p.name)
          put("lat", p.latitude)
          put("lng", p.longitude)
          put("emoji", p.emoji)
          put("type", p.type.name)
          put("isLiveNow", p.isLiveNow)
          put("attendees", p.attendeeCount)
          put("isSelected", selectedParty?.id == p.id)
        }
        partiesArray.put(obj)
      }

      val script = """
        updateParties(${partiesArray});
        updatePickMode($isPickLocationMode, ${pickedLocation.first}, ${pickedLocation.second});
      """.trimIndent()
      webViewRef?.evaluateJavascript(script, null)
    }
  }

  // Update user real-time location on the map
  LaunchedEffect(userLocation, isMapReady) {
    if (isMapReady && webViewRef != null) {
      val script = "updateUserLocation(${userLocation.first}, ${userLocation.second});"
      webViewRef?.evaluateJavascript(script, null)
    }
  }

  Box(modifier = modifier.fillMaxSize()) {
    // 1. Hardware accelerated Real-Time Map WebView
    AndroidView(
      factory = { ctx ->
        WebView(ctx).apply {
          settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            cacheMode = WebSettings.LOAD_DEFAULT
            loadsImagesAutomatically = true
            useWideViewPort = true
            loadWithOverviewMode = true
          }
          addJavascriptInterface(jsBridge, "AndroidBridge")
          webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
              super.onPageFinished(view, url)
              isMapReady = true
            }
          }
          val html = generateLeafletMapHtml(
            initialLat = userLocation.first,
            initialLng = userLocation.second,
            tileUrl = mapStyles[mapStyleIndex].second
          )
          loadDataWithBaseURL("https://openstreetmap.org", html, "text/html", "UTF-8", null)
          webViewRef = this
        }
      },
      update = { webView ->
        webViewRef = webView
      },
      modifier = Modifier
        .fillMaxSize()
        .testTag("real_time_map_webview")
    )

    // 2. Real-Time Map Controls (Zoom, Recenter on GPS, Style Toggle)
    Column(
      modifier = Modifier
        .align(Alignment.CenterEnd)
        .padding(end = 16.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      // Style toggle
      FloatingActionButton(
        onClick = {
          mapStyleIndex = (mapStyleIndex + 1) % mapStyles.size
          val nextTile = mapStyles[mapStyleIndex].second
          webViewRef?.evaluateJavascript("changeTileLayer('$nextTile');", null)
        },
        shape = CircleShape,
        containerColor = Color.White,
        contentColor = PartyPurple,
        elevation = FloatingActionButtonDefaults.elevation(4.dp),
        modifier = Modifier
          .size(44.dp)
          .testTag("map_style_toggle_button")
      ) {
        Icon(Icons.Default.Layers, contentDescription = "Change Map Style")
      }

      // Zoom in
      FloatingActionButton(
        onClick = {
          webViewRef?.evaluateJavascript("map.zoomIn();", null)
        },
        shape = CircleShape,
        containerColor = Color.White,
        contentColor = PartyPurple,
        elevation = FloatingActionButtonDefaults.elevation(4.dp),
        modifier = Modifier
          .size(44.dp)
          .testTag("real_map_zoom_in")
      ) {
        Icon(Icons.Default.Add, contentDescription = "Zoom In")
      }

      // Zoom out
      FloatingActionButton(
        onClick = {
          webViewRef?.evaluateJavascript("map.zoomOut();", null)
        },
        shape = CircleShape,
        containerColor = Color.White,
        contentColor = PartyPurple,
        elevation = FloatingActionButtonDefaults.elevation(4.dp),
        modifier = Modifier
          .size(44.dp)
          .testTag("real_map_zoom_out")
      ) {
        Icon(Icons.Default.Remove, contentDescription = "Zoom Out")
      }

      // Recenter on user's real GPS position
      FloatingActionButton(
        onClick = {
          onCenterOnMe()
          webViewRef?.evaluateJavascript(
            "map.flyTo([${userLocation.first}, ${userLocation.second}], 15, { duration: 1.2 });",
            null
          )
        },
        shape = CircleShape,
        containerColor = PartyPurple,
        contentColor = Color.White,
        elevation = FloatingActionButtonDefaults.elevation(6.dp),
        modifier = Modifier
          .size(48.dp)
          .testTag("real_map_center_me")
      ) {
        Icon(Icons.Default.MyLocation, contentDescription = "Center on Real GPS Location")
      }
    }

    // 3. Top Real-Time Map Legend Pill
    Surface(
      shape = RoundedCornerShape(20.dp),
      color = Color.White.copy(alpha = 0.95f),
      border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFE2E8F0)),
      shadowElevation = 4.dp,
      modifier = Modifier
        .align(Alignment.TopCenter)
        .padding(top = 10.dp)
    ) {
      Row(
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(10.dp)
              .clip(CircleShape)
              .background(PartyGreen)
          )
          Spacer(Modifier.width(4.dp))
          Text("Public", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CartoonBorder)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(10.dp)
              .clip(CircleShape)
              .background(PartyPurple)
          )
          Spacer(Modifier.width(4.dp))
          Text("Private 🔒", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CartoonBorder)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text("⚡", fontSize = 11.sp)
          Spacer(Modifier.width(2.dp))
          Text("Live GPS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PartyCyan)
        }
      }
    }
  }
}

// Generate self-contained high-performance Leaflet HTML with CartoDB/OpenStreetMap
private fun generateLeafletMapHtml(
  initialLat: Double,
  initialLng: Double,
  tileUrl: String
): String {
  return """
<!DOCTYPE html>
<html>
<head>
  <meta charset="utf-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no" />
  <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css" />
  <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>
  <style>
    html, body, #map {
      height: 100%;
      width: 100%;
      margin: 0;
      padding: 0;
      background: #E8F4F8;
      overflow: hidden;
    }
    .leaflet-control-zoom, .leaflet-control-attribution {
      display: none !important;
    }
    
    /* Cartoon Party Pin Styling */
    .party-marker-container {
      position: relative;
      cursor: pointer;
      display: flex;
      flex-direction: column;
      align-items: center;
      transition: transform 0.2s cubic-bezier(0.34, 1.56, 0.64, 1);
    }
    .party-marker-container:active {
      transform: scale(0.92);
    }
    .party-pin-bubble {
      width: 44px;
      height: 44px;
      border-radius: 22px;
      background: white;
      border: 3px solid #05C46B;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 22px;
      box-shadow: 0 6px 16px rgba(0,0,0,0.22);
      position: relative;
      z-index: 2;
    }
    .party-pin-bubble.private {
      border-color: #6C5CE7;
    }
    .party-pin-bubble.selected {
      border-color: #FF2A7A;
      transform: scale(1.15);
      box-shadow: 0 8px 20px rgba(255, 42, 122, 0.4);
    }
    .party-pin-pointer {
      width: 0;
      height: 0;
      border-left: 6px solid transparent;
      border-right: 6px solid transparent;
      border-top: 7px solid #05C46B;
      margin-top: -1px;
      z-index: 1;
    }
    .party-pin-pointer.private {
      border-top-color: #6C5CE7;
    }
    .party-pin-pointer.selected {
      border-top-color: #FF2A7A;
    }
    
    /* Live Pulse Ring */
    .pulse-ring {
      position: absolute;
      top: -6px;
      left: -6px;
      width: 56px;
      height: 56px;
      border-radius: 50%;
      background: rgba(255, 42, 122, 0.35);
      animation: pulseAnim 1.8s infinite ease-out;
      z-index: 0;
      pointer-events: none;
    }
    @keyframes pulseAnim {
      0% { transform: scale(0.8); opacity: 0.8; }
      100% { transform: scale(1.8); opacity: 0; }
    }
    
    .live-badge {
      position: absolute;
      top: -8px;
      background: #FF2A7A;
      color: white;
      font-weight: 800;
      font-size: 8px;
      padding: 1px 5px;
      border-radius: 8px;
      border: 1px solid white;
      z-index: 3;
      box-shadow: 0 2px 4px rgba(0,0,0,0.2);
    }
    .party-name-pill {
      margin-top: 2px;
      background: rgba(255, 255, 255, 0.95);
      color: #191635;
      font-weight: 700;
      font-size: 10px;
      padding: 2px 6px;
      border-radius: 10px;
      white-space: nowrap;
      box-shadow: 0 2px 6px rgba(0,0,0,0.15);
      border: 1px solid #E2E8F0;
    }

    /* User GPS Blue Halo */
    .user-gps-marker {
      width: 18px;
      height: 18px;
      background: #00D2D3;
      border: 3px solid white;
      border-radius: 50%;
      box-shadow: 0 0 10px rgba(0, 210, 211, 0.8);
      position: relative;
    }
    .user-gps-pulse {
      position: absolute;
      top: -12px;
      left: -12px;
      width: 42px;
      height: 42px;
      border-radius: 50%;
      background: rgba(0, 210, 211, 0.3);
      animation: userGpsPulse 2s infinite ease-out;
    }
    @keyframes userGpsPulse {
      0% { transform: scale(0.6); opacity: 0.9; }
      100% { transform: scale(2.2); opacity: 0; }
    }
  </style>
</head>
<body>
  <div id="map"></div>
  <script>
    var map = L.map('map', {
      center: [$initialLat, $initialLng],
      zoom: 14,
      zoomControl: false
    });

    var currentTileLayer = L.tileLayer('$tileUrl', {
      maxZoom: 19
    }).addTo(map);

    function changeTileLayer(newUrl) {
      if (currentTileLayer) {
        map.removeLayer(currentTileLayer);
      }
      currentTileLayer = L.tileLayer(newUrl, { maxZoom: 19 }).addTo(map);
    }

    var markersGroup = L.layerGroup().addTo(map);
    var userMarker = null;
    var hostPickMarker = null;
    var isPickMode = false;

    // User Location Pin
    function updateUserLocation(lat, lng) {
      if (userMarker) {
        userMarker.setLatLng([lat, lng]);
      } else {
        var userIcon = L.divIcon({
          className: 'user-icon',
          html: '<div style="position:relative"><div class="user-gps-pulse"></div><div class="user-gps-marker"></div></div>',
          iconSize: [18, 18],
          iconAnchor: [9, 9]
        });
        userMarker = L.marker([lat, lng], { icon: userIcon, zIndexOffset: 1000 }).addTo(map);
      }
    }
    updateUserLocation($initialLat, $initialLng);

    // Map Click Handler for Pick Location Mode
    map.on('click', function(e) {
      if (isPickMode && window.AndroidBridge) {
        window.AndroidBridge.onMapClick(e.latlng.lat, e.latlng.lng);
      }
    });

    // Update Party Markers
    function updateParties(partiesList) {
      markersGroup.clearLayers();
      if (!partiesList) return;

      partiesList.forEach(function(party) {
        var isPublic = party.type === 'PUBLIC';
        var borderClass = isPublic ? '' : 'private';
        var selectedClass = party.isSelected ? 'selected' : '';
        var liveHtml = party.isLiveNow ? '<div class="live-badge">LIVE</div><div class="pulse-ring"></div>' : '';

        var html = 
          '<div class="party-marker-container" onclick="onPartyClick(\'' + party.id + '\')">' +
            liveHtml +
            '<div class="party-pin-bubble ' + borderClass + ' ' + selectedClass + '">' +
              party.emoji +
            '</div>' +
            '<div class="party-pin-pointer ' + borderClass + ' ' + selectedClass + '"></div>' +
            '<div class="party-name-pill">' + (party.name.length > 15 ? party.name.substring(0,14) + '..' : party.name) + '</div>' +
          '</div>';

        var icon = L.divIcon({
          className: 'custom-party-icon',
          html: html,
          iconSize: [60, 70],
          iconAnchor: [30, 52]
        });

        var marker = L.marker([party.lat, party.lng], { icon: icon });
        marker.on('click', function(e) {
          L.DomEvent.stopPropagation(e);
          if (window.AndroidBridge) {
            window.AndroidBridge.onPartyClick(party.id);
          }
        });
        markersGroup.addLayer(marker);
      });
    }

    function onPartyClick(partyId) {
      if (window.AndroidBridge) {
        window.AndroidBridge.onPartyClick(partyId);
      }
    }

    // Update Pick Location Marker
    function updatePickMode(enabled, lat, lng) {
      isPickMode = enabled;
      if (enabled) {
        if (!hostPickMarker) {
          var pickIcon = L.divIcon({
            className: 'pick-host-icon',
            html: '<div style="display:flex;flex-direction:column;align-items:center;">' +
                  '<div style="background:#FF2A7A;color:white;font-weight:bold;font-size:11px;padding:3px 8px;border-radius:12px;border:2px solid white;box-shadow:0 4px 10px rgba(0,0,0,0.3);">📍 Host Here!</div>' +
                  '<div style="font-size:32px;margin-top:-2px;">📍</div>' +
                  '</div>',
            iconSize: [80, 60],
            iconAnchor: [40, 50]
          });
          hostPickMarker = L.marker([lat, lng], { icon: pickIcon, draggable: true }).addTo(map);
          hostPickMarker.on('dragend', function(e) {
            var pos = e.target.getLatLng();
            if (window.AndroidBridge) {
              window.AndroidBridge.onMapClick(pos.lat, pos.lng);
            }
          });
        } else {
          hostPickMarker.setLatLng([lat, lng]);
        }
      } else {
        if (hostPickMarker) {
          map.removeLayer(hostPickMarker);
          hostPickMarker = null;
        }
      }
    }

    if (window.AndroidBridge) {
      window.AndroidBridge.onMapLoaded();
    }
  </script>
</body>
</html>
""".trimIndent()
}
