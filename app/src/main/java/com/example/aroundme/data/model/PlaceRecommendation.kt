package com.example.aroundme.data.model

data class PlaceRecommendation(
    val name: String,
    val description: String,
    val historicalPeriod: String,
    val distance: String?,
    val travelTime: String? = null
)

