package com.itravelguide

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.itravelguide.data.Place
import com.itravelguide.ui.PlaceDetailsSheet
import com.itravelguide.ui.PlaceSummaryCard
import com.itravelguide.ui.theme.ITravelGuideTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    private val locationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
            val granted = result.values.any { it }
            viewModel.onLocationPermissionResult(granted)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        viewModel.bindLocationClient(fusedLocationClient)

        setContent {
            ITravelGuideTheme {
                MapScreen(viewModel = viewModel)
            }
        }

        locationPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(viewModel: MainViewModel = viewModel()) {
    val uiState by viewModel.uiState
    val context = LocalContext.current
    val cameraPositionState = rememberCameraPositionState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    var selectedPlace by remember { mutableStateOf<Place?>(null) }
    var isBottomSheetVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadData(context.assets)
    }

    LaunchedEffect(uiState.places) {
        if (uiState.places.isNotEmpty() && selectedPlace == null) {
            val firstPlace = uiState.places.first()
            selectedPlace = firstPlace
            viewModel.onPlaceSelected(firstPlace)
            cameraPositionState.animateTo(firstPlace.latitude, firstPlace.longitude)
        }
    }

    LaunchedEffect(uiState.selectedPlace) {
        uiState.selectedPlace?.let { place ->
            selectedPlace = place
            coroutineScope.launch {
                cameraPositionState.animateTo(place.latitude, place.longitude)
            }
        }
    }

    LaunchedEffect(uiState.userLocation) {
        uiState.userLocation?.let { location ->
            coroutineScope.launch {
                cameraPositionState.animateTo(location.latitude, location.longitude, zoom = 16f)
            }
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            coroutineScope.launch { snackbarHostState.showSnackbar(message) }
            viewModel.onErrorMessageShown()
        }
    }

    MapScaffold(
        cameraPositionState = cameraPositionState,
        snackbarHostState = snackbarHostState,
        properties = MapProperties(mapType = MapType.NORMAL, isMyLocationEnabled = uiState.isLocationPermissionGranted),
        places = uiState.places,
        onPlaceSelected = { place ->
            selectedPlace = place
            viewModel.onPlaceSelected(place)
            coroutineScope.launch {
                cameraPositionState.animateTo(place.latitude, place.longitude)
            }
        },
        onLanguageToggle = viewModel::toggleLanguage,
        isVietnamese = uiState.isVietnamese,
        selectedPlace = selectedPlace,
        onShowDetails = {
            if (selectedPlace != null) {
                isBottomSheetVisible = true
            }
        },
        onDismissDetails = {
            isBottomSheetVisible = false
        }
    )

    if (isBottomSheetVisible && selectedPlace != null) {
        PlaceDetailsSheet(
            place = selectedPlace!!,
            isVietnamese = uiState.isVietnamese,
            onDismiss = {
                isBottomSheetVisible = false
            },
            assetManager = context.assets
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MapScaffold(
    cameraPositionState: CameraPositionState,
    snackbarHostState: SnackbarHostState,
    properties: MapProperties,
    places: List<Place>,
    onPlaceSelected: (Place) -> Unit,
    onLanguageToggle: () -> Unit,
    isVietnamese: Boolean,
    selectedPlace: Place?,
    onShowDetails: () -> Unit,
    onDismissDetails: () -> Unit,
) {
    androidx.compose.material3.Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = properties,
                onMapClick = { onDismissDetails() }
            ) {
                places.forEach { place ->
                    Marker(
                        state = MarkerState(position = place.latLng),
                        title = place.displayName(isVietnamese),
                        snippet = place.intro(isVietnamese),
                        onClick = {
                            onPlaceSelected(place)
                            true
                        }
                    )
                }
            }
            PlaceSummaryCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter),
                place = selectedPlace,
                isVietnamese = isVietnamese,
                onLanguageToggle = onLanguageToggle,
                onDetailsClick = onShowDetails
            )
        }
    }
}

private suspend fun CameraPositionState.animateTo(lat: Double, lng: Double, zoom: Float = 14f) {
    animate(CameraUpdateFactory.newLatLngZoom(LatLng(lat, lng), zoom))
}
