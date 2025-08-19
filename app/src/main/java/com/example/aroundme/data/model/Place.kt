package com.example.aroundme.data.model

import com.google.gson.annotations.SerializedName

data class Place(
    @SerializedName("name") val name: String,
    @SerializedName("lat") val lat: Double,
    @SerializedName("lon") val lon: Double,
    @SerializedName("addr:street") val street: String? = null,
    @SerializedName("historic") val historic: String? = null,
    @SerializedName("bus") val bus: String? = null,
    @SerializedName("shop") val shop: String? = null,
    @SerializedName("amenity") val amenity: String? = null,
    @SerializedName("phone") val phone: String? = null,
    @SerializedName("tram") val tram: Boolean? = null,
    @SerializedName("railway") val railway: String? = null,
    @SerializedName("public_transport") val publicTransport: String? = null,
    @SerializedName("ferry") val ferry: String? = null,
    @SerializedName("opening_hours") val openingHours: String? = null
)

