package com.example.aroundme.data.model


import com.google.gson.annotations.SerializedName

data class Place(
    @SerializedName("elements")
    val elements: List<Element?>?,
    @SerializedName("generator")
    val generator: String?,
    @SerializedName("osm3s")
    val osm3s: Osm3s?,
    @SerializedName("version")
    val version: Double?
) {
    data class Element(
        @SerializedName("id")
        val id: Long?,
        @SerializedName("lat")
        val lat: Double?,
        @SerializedName("lon")
        val lon: Double?,
        @SerializedName("tags")
        val tags: Tags?,
        @SerializedName("type")
        val type: String?
    ) {
        data class Tags(
            @SerializedName("addr:housenumber")
            val addrHousenumber: String?,
            @SerializedName("addr:street")
            val addrStreet: String?,
            @SerializedName("amenity")
            val amenity: String?,
            @SerializedName("bench")
            val bench: String?,
            @SerializedName("bus")
            val bus: String?,
            @SerializedName("cuisine")
            val cuisine: String?,
            @SerializedName("healthcare")
            val healthcare: String?,
            @SerializedName("highway")
            val highway: String?,
            @SerializedName("historic")
            val historic: String?,
            @SerializedName("name")
            val name: String?,
            @SerializedName("opening_hours")
            val openingHours: String?,
            @SerializedName("operator")
            val `operator`: String?,
            @SerializedName("operator:wikidata")
            val operatorWikidata: String?,
            @SerializedName("operator:wikipedia")
            val operatorWikipedia: String?,
            @SerializedName("place")
            val place: String?,
            @SerializedName("public_transport")
            val publicTransport: String?,
            @SerializedName("railway")
            val railway: String?,
            @SerializedName("ref")
            val ref: String?,
            @SerializedName("shelter")
            val shelter: String?,
            @SerializedName("shop")
            val shop: String?,
            @SerializedName("tourism")
            val tourism: String?,
            @SerializedName("train")
            val train: String?,
            @SerializedName("wikidata")
            val wikidata: String?,
            @SerializedName("wikimedia_commons")
            val wikimediaCommons: String?,
            @SerializedName("wikipedia")
            val wikipedia: String?,
            @SerializedName("wikipedia:arz")
            val wikipediaArz: String?,
            @SerializedName("wikipedia:en")
            val wikipediaEn: String?
        )
    }

    data class Osm3s(
        @SerializedName("copyright")
        val copyright: String?,
        @SerializedName("timestamp_osm_base")
        val timestampOsmBase: String?
    )
}