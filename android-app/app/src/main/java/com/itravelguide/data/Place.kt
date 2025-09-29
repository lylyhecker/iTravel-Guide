package com.itravelguide.data

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
    val introEn: String,
    @SerialName("read_url")
    val readUrl: String,
    @SerialName("read_url_en")
    val readUrlEn: String,
    @SerialName("quiz_url")
    val quizUrl: String,
) {
    val latitude: Double get() = coordinates.getOrNull(0) ?: 0.0
    val longitude: Double get() = coordinates.getOrNull(1) ?: 0.0
    val latLng: LatLng get() = LatLng(latitude, longitude)

    fun displayName(isVietnamese: Boolean) = name

    fun intro(isVietnamese: Boolean): String = if (isVietnamese) intro else introEn

    fun readAssetPath(isVietnamese: Boolean) = if (isVietnamese) readUrl else readUrlEn
}
