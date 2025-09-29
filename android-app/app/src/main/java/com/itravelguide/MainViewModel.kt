package com.itravelguide

import android.content.res.AssetManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.itravelguide.data.Place
import com.itravelguide.data.PlacesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {

    private val repository = PlacesRepository()

    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState

    private var locationClient: FusedLocationProviderClient? = null

    fun bindLocationClient(client: FusedLocationProviderClient) {
        locationClient = client
        requestLastKnownLocation()
    }

    fun loadData(assetManager: AssetManager) {
        if (_uiState.value.places.isNotEmpty()) return
        viewModelScope.launch(Dispatchers.IO) {
            runCatching { repository.loadPlaces(assetManager) }
                .onSuccess { places ->
                    _uiState.value = _uiState.value.copy(places = places)
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(errorMessage = it.localizedMessage ?: "Không thể tải dữ liệu")
                }
        }
    }

    fun onPlaceSelected(place: Place) {
        _uiState.value = _uiState.value.copy(selectedPlace = place)
    }

    fun toggleLanguage() {
        val current = _uiState.value
        _uiState.value = current.copy(isVietnamese = !current.isVietnamese)
    }

    fun onLocationPermissionResult(granted: Boolean) {
        _uiState.value = _uiState.value.copy(isLocationPermissionGranted = granted)
        if (granted) {
            requestLastKnownLocation()
        }
    }

    private fun requestLastKnownLocation() {
        val client = locationClient ?: return
        if (!_uiState.value.isLocationPermissionGranted) return

        viewModelScope.launch {
            val token = CancellationTokenSource()
            runCatching {
                client.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, token.token)
            }.onSuccess { task ->
                task.addOnSuccessListener { location ->
                    if (location != null) {
                        _uiState.value = _uiState.value.copy(
                            userLocation = location,
                        )
                    }
                }
            }.onFailure {
                _uiState.value = _uiState.value.copy(errorMessage = it.localizedMessage ?: "Không thể xác định vị trí")
            }
        }
    }

    fun onErrorMessageShown() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}

data class MapUiState(
    val places: List<Place> = emptyList(),
    val selectedPlace: Place? = null,
    val isVietnamese: Boolean = true,
    val errorMessage: String? = null,
    val isLocationPermissionGranted: Boolean = false,
    val userLocation: android.location.Location? = null,
)
