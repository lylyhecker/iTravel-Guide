package com.example.itravelguide.data

import android.content.Context
import androidx.annotation.VisibleForTesting
import com.example.itravelguide.model.Place
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

class PlaceRepository(private val context: Context) {
    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = false
    }

    suspend fun loadPlaces(): List<Place> = withContext(Dispatchers.IO) {
        context.assets.open(PLACES_FILE).use { inputStream ->
            val content = inputStream.bufferedReader().readText()
            json.decodeFromString(ListSerializer(Place.serializer()), content)
        }
    }

    companion object {
        @VisibleForTesting
        internal const val PLACES_FILE = "places.json"
    }
}
