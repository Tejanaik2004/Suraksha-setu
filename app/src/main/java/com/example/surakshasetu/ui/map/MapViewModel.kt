package com.example.surakshasetu.ui.map

import android.annotation.SuppressLint
import android.location.Location
import android.os.Looper
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

data class MapState(
    val userLocation: LatLng? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val nearbyPoints: List<EmergencyPoint> = emptyList()
)

data class EmergencyPoint(
    val name: String,
    val location: LatLng,
    val type: PointType
)

enum class PointType {
    SAFE_ZONE, POLICE, HOSPITAL, HELP_POINT
}

@HiltViewModel
class MapViewModel @Inject constructor(
    private val fusedLocationClient: FusedLocationProviderClient
) : ViewModel() {

    private val _state = MutableStateFlow(MapState())
    val state: StateFlow<MapState> = _state.asStateFlow()

    private val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000)
        .setMinUpdateIntervalMillis(2000)
        .build()

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            locationResult.lastLocation?.let { location ->
                updateUserLocation(location)
            }
        }
    }

    init {
        loadNearbyEmergencyServices()
    }

    private fun updateUserLocation(location: Location) {
        val latLng = LatLng(location.latitude, location.longitude)
        _state.value = _state.value.copy(
            userLocation = latLng,
            isLoading = false,
            error = null
        )
        Log.d("MapViewModel", "Live location updated: $latLng")
    }

    @SuppressLint("MissingPermission")
    fun fetchInitialLocation() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            try {
                // Try to get last known location first for instant UI response
                val lastLocation = fusedLocationClient.lastLocation.await()
                if (lastLocation != null) {
                    updateUserLocation(lastLocation)
                }
            } catch (e: Exception) {
                Log.e("MapViewModel", "Error fetching initial location", e)
            } finally {
                // Always start continuous updates
                startLocationUpdates()
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun startLocationUpdates() {
        Log.d("MapViewModel", "Starting real-time location tracking")
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        ).addOnFailureListener { e ->
            Log.e("MapViewModel", "Location updates failed", e)
            _state.value = _state.value.copy(
                isLoading = false,
                error = "Location tracking failed. Please ensure GPS is enabled."
            )
        }
    }

    fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    private fun loadNearbyEmergencyServices() {
        // Production-ready mock data. In a real app, this would be fetched from a backend.
        val mockPoints = listOf(
            EmergencyPoint("City North Police Station", LatLng(12.9160, 74.8580), PointType.POLICE),
            EmergencyPoint("Mangalore General Hospital", LatLng(12.9120, 74.8540), PointType.HOSPITAL),
            EmergencyPoint("Unity Medical Center", LatLng(12.9100, 74.8590), PointType.HOSPITAL),
            EmergencyPoint("Safe Zone - Mall Center", LatLng(12.9141, 74.8560), PointType.SAFE_ZONE),
            EmergencyPoint("SOS Help Point - Park Plaza", LatLng(12.9180, 74.8520), PointType.HELP_POINT)
        )
        _state.value = _state.value.copy(nearbyPoints = mockPoints)
    }

    override fun onCleared() {
        super.onCleared()
        stopLocationUpdates()
    }
}
