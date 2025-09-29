package com.example.itravelguide

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.itravelguide.data.PlaceUiState
import com.example.itravelguide.data.PlaceViewModel
import com.example.itravelguide.model.Place
import com.example.itravelguide.ui.theme.ITravelGuideTheme
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ITravelGuideTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    MapScreen()
                }
            }
        }
    }
}

@Composable
private fun MapScreen(viewModel: PlaceViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val cameraPositionState = rememberCameraPositionState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(id = R.string.app_name)) },
                scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
            )
        }
    ) { paddingValues ->
        when (val state = uiState) {
            is PlaceUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is PlaceUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = state.throwable.localizedMessage ?: "Đã xảy ra lỗi")
                }
            }
            is PlaceUiState.Success -> {
                MapContent(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    cameraPositionState = cameraPositionState,
                    places = state.places,
                    selected = state.selected,
                    onSelect = viewModel::selectPlace
                )
            }
        }
    }
}

@Composable
private fun MapContent(
    modifier: Modifier,
    cameraPositionState: CameraPositionState,
    places: List<Place>,
    selected: Place?,
    onSelect: (Place) -> Unit
) {
    val scope = rememberCoroutineScope()
    val initialPosition = remember(places) {
        selected?.location ?: places.firstOrNull()?.location ?: LatLng(16.4637, 107.5909)
    }
    val lastTappedPosition = remember { mutableStateOf(initialPosition) }
    val cameraInitialized = remember { mutableStateOf(false) }

    LaunchedEffect(initialPosition, cameraInitialized.value) {
        if (!cameraInitialized.value) {
            cameraPositionState.move(CameraUpdateFactory.newLatLngZoom(initialPosition, 12f))
            cameraInitialized.value = true
        }
    }

    Box(modifier = modifier) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(isMyLocationEnabled = false),
            uiSettings = MapUiSettings(zoomControlsEnabled = false),
            onMapClick = { latLng ->
                lastTappedPosition.value = latLng
                scope.launch {
                    cameraPositionState.animate(CameraUpdateFactory.newLatLng(latLng))
                }
            }
        ) {
            places.forEach { place ->
                val markerState = remember(place.id) { MarkerState(position = place.location) }
                Marker(
                    state = markerState,
                    title = place.name,
                    snippet = place.intro,
                    onClick = {
                        onSelect(place)
                        scope.launch {
                            cameraPositionState.animate(
                                CameraUpdateFactory.newLatLngZoom(place.location, 14f)
                            )
                        }
                        true
                    }
                )
            }

            val moveMarkerState = remember { MarkerState(position = lastTappedPosition.value) }
            moveMarkerState.position = lastTappedPosition.value
            Marker(
                state = moveMarkerState,
                title = "Vị trí đã chọn",
                snippet = "Nhấn vào bản đồ để di chuyển"
            )
        }

        selected?.let { place ->
            PlaceInfoCard(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                place = place,
                onFocusOnPlace = {
                    onSelect(place)
                    scope.launch {
                        cameraPositionState.animate(
                            CameraUpdateFactory.newLatLngZoom(place.location, 15f)
                        )
                    }
                }
            )
        }
    }
}

@Composable
private fun PlaceInfoCard(
    modifier: Modifier = Modifier,
    place: Place,
    onFocusOnPlace: () -> Unit
) {
    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = place.name,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                FilledIconButton(onClick = onFocusOnPlace, modifier = Modifier.size(40.dp)) {
                    Icon(
                        imageVector = Icons.Filled.Place,
                        contentDescription = null
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = place.intro,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
