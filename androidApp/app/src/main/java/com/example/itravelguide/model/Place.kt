package com.example.itravelguide.model

import com.google.android.gms.maps.model.LatLng
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Place(
    val id: String,
    val name: String,
    @SerialName("coords")
    val coordinates: List<Double>,
    val intro: String,
    @SerialName("intro_en")
    val introEn: String? = null,
    @SerialName("read_url")
    val readUrl: String? = null,
    @SerialName("read_url_en")
    val readUrlEn: String? = null,
    @SerialName("quiz_url")
    val quizUrl: String? = null
) {
    val location: LatLng
        get() = LatLng(coordinates.getOrNull(0) ?: 0.0, coordinates.getOrNull(1) ?: 0.0)
}
