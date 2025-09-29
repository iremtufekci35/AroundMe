package com.example.aroundme.data.model

data class PlaceRecommendation(
    var name: String,
    val description: String,
    val historicalPeriod: String,
    val distance: String?,
    val latitude: Double? = null,
    val longitude: Double? = null
)


