package com.itravelguide.data

import android.content.res.AssetManager
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

class PlacesRepository {
    private val json = Json { ignoreUnknownKeys = true }

    fun loadPlaces(assetManager: AssetManager): List<Place> {
        assetManager.open(PLACES_FILE).use { inputStream ->
            val content = inputStream.bufferedReader().readText()
            return json.decodeFromString(ListSerializer(Place.serializer()), content)
        }
    }

    fun loadDescription(assetManager: AssetManager, path: String): String {
        val sanitizedPath = path.removePrefix("/")
        return assetManager.open(sanitizedPath).use { it.bufferedReader().readText() }
    }

    companion object {
        private const val PLACES_FILE = "places.json"
    }
}
