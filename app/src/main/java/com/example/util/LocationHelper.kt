package com.example.util

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Build
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.util.Locale

class LocationHelper(private val context: Context) {
  private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

  // Default coordinate: Bandra, Mumbai (19.0596, 72.8295) or dynamic user spot
  private val _currentLocation = MutableStateFlow(Pair(19.0596, 72.8295))
  val currentLocation: StateFlow<Pair<Double, Double>> = _currentLocation.asStateFlow()

  private val _hasLocationPermission = MutableStateFlow(false)
  val hasLocationPermission: StateFlow<Boolean> = _hasLocationPermission.asStateFlow()

  init {
    checkPermissionAndFetch()
  }

  fun checkPermissionAndFetch() {
    val fine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    val coarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
    val granted = fine || coarse
    _hasLocationPermission.value = granted
    if (granted) {
      fetchRealTimeLocation()
    }
  }

  @SuppressLint("MissingPermission")
  fun fetchRealTimeLocation(onLocationReceived: ((Double, Double) -> Unit)? = null) {
    val fine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    val coarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
    if (!fine && !coarse) return

    val cancellationSource = CancellationTokenSource()
    fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cancellationSource.token)
      .addOnSuccessListener { location: Location? ->
        if (location != null) {
          val coords = Pair(location.latitude, location.longitude)
          _currentLocation.value = coords
          onLocationReceived?.invoke(location.latitude, location.longitude)
        } else {
          // Fallback to last known location
          fusedLocationClient.lastLocation.addOnSuccessListener { lastLoc: Location? ->
            if (lastLoc != null) {
              val coords = Pair(lastLoc.latitude, lastLoc.longitude)
              _currentLocation.value = coords
              onLocationReceived?.invoke(lastLoc.latitude, lastLoc.longitude)
            }
          }
        }
      }
      .addOnFailureListener {
        // Keep fallback default
      }
  }

  suspend fun getAddressFromCoordinates(lat: Double, lng: Double): String = withContext(Dispatchers.IO) {
    try {
      val geocoder = Geocoder(context, Locale.getDefault())
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val addresses = geocoder.getFromLocation(lat, lng, 1)
        if (!addresses.isNullOrEmpty()) {
          val addr = addresses[0]
          val street = addr.thoroughfare ?: addr.subLocality ?: ""
          val locality = addr.locality ?: addr.subAdminArea ?: ""
          if (street.isNotBlank() && locality.isNotBlank()) {
            return@withContext "$street, $locality"
          } else if (addr.featureName != null) {
            return@withContext "${addr.featureName}, ${locality.ifBlank { addr.adminArea ?: "Area" }}"
          }
        }
      } else {
        @Suppress("DEPRECATION")
        val addresses = geocoder.getFromLocation(lat, lng, 1)
        if (!addresses.isNullOrEmpty()) {
          val addr = addresses[0]
          val street = addr.thoroughfare ?: addr.subLocality ?: ""
          val locality = addr.locality ?: addr.subAdminArea ?: ""
          if (street.isNotBlank() && locality.isNotBlank()) {
            return@withContext "$street, $locality"
          }
        }
      }
    } catch (e: Exception) {
      // Ignore geocoder error and format coords
    }
    return@withContext String.format(Locale.US, "Spot (%.4f, %.4f)", lat, lng)
  }
}
