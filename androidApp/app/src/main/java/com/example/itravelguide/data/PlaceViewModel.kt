package com.example.itravelguide.data

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.itravelguide.model.Place
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed interface PlaceUiState {
    object Loading : PlaceUiState
    data class Success(val places: List<Place>, val selected: Place?) : PlaceUiState
    data class Error(val throwable: Throwable) : PlaceUiState
}

class PlaceViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = PlaceRepository(application.applicationContext)

    private val _uiState: MutableStateFlow<PlaceUiState> = MutableStateFlow(PlaceUiState.Loading)
    val uiState: StateFlow<PlaceUiState> = _uiState

    init {
        loadPlaces()
    }

    fun loadPlaces() {
        viewModelScope.launch {
            _uiState.value = PlaceUiState.Loading
            runCatching { repository.loadPlaces() }
                .onSuccess { places ->
                    _uiState.value = PlaceUiState.Success(places, places.firstOrNull())
                }
                .onFailure { throwable ->
                    _uiState.value = PlaceUiState.Error(throwable)
                }
        }
    }

    fun selectPlace(place: Place) {
        val current = _uiState.value
        if (current is PlaceUiState.Success) {
            _uiState.value = current.copy(selected = place)
        }
    }
}
