package com.example.aroundme.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aroundme.data.model.Place
import com.example.aroundme.data.model.PlaceRecommendation
import com.example.aroundme.data.remote.RetrofitInstance
import com.example.aroundme.utils.Translations
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import javax.inject.Inject

@HiltViewModel
class PlacesViewModel @Inject constructor() : ViewModel() {

    private val _places = MutableStateFlow<List<Place.Element>>(emptyList())
    val places: StateFlow<List<Place.Element>> = _places

    private val _loading = MutableStateFlow<Boolean>(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>("")

    private val _recommendations = MutableStateFlow<List<PlaceRecommendation>>(emptyList())

    fun searchAttractions(name: String?, category: String?) {
        viewModelScope.launch {
            _loading.value = true
            try {
                loadTouristAttractionsInternal(name)
                val filtered = _places.value.filter { place ->
                    val matchesName =
                        name.isNullOrBlank() || place.tags?.name?.contains(name, true) == true
                    val matchesCategory =
                        category.isNullOrBlank() || place.tags?.tourism == category
                    matchesName && matchesCategory
                }
                _places.value = filtered
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                delay(500)
                _loading.value = false
            }
        }
    }

    fun fetchAiRecommendations(latitude: Double, longitude: Double, category: String, keyword: String?) {
        println("fetch ai 1 category $category")
        viewModelScope.launch {
            _loading.value = true
            try {

                val recommendationList = getRecommendationsAi(latitude, longitude, category, keyword)

                println("rec list: $recommendationList")
                val translatedRecommendations = mutableListOf<PlaceRecommendation>()
                println("rec list 2: $translatedRecommendations")
                recommendationList.forEach { rec ->
                    val cleanedName = rec.name.replace(Regex("\\s*\\([^)]*\\)"), "").trim()

                    Translations.translateText(cleanedName) { translatedName ->
                        translatedRecommendations.add(
                            rec.copy(name = translatedName)
                        )

                        if (translatedRecommendations.size == recommendationList.size) {
                            println("Translated & cleaned recommendations: $translatedRecommendations")
                            _recommendations.value = translatedRecommendations
                            showAiRecommendationsOnMap(translatedRecommendations)
                        }
                    }
                }
            } catch (e: Exception) {
                _error.value = e.message
                println("AI fetch error: $e")
            } finally {
                _loading.value = false
            }
        }
    }

    fun setLoading(value: Boolean) {
        _loading.value = value
    }

    fun loadTouristAttractionsInternal(searchQuery: String?) {
        /** here do not use static lat and lon */
        println("query burada: $searchQuery")
        viewModelScope.launch {
            try {
                val query = searchQuery?.let {
                    """
                    [out:json];
                    node
                      [name~"$it",i]
                      (38.40,27.10,38.45,27.20);
                    out;
                    """.trimIndent()
                } ?: """
                    [out:json];
                    node
                      [tourism=attraction]
                      (38.40,27.10,38.45,27.20);
                    out;
                    """.trimIndent()

                val response = RetrofitInstance.api.getTouristAttractions(query)
                if (response.isSuccessful) {
                    val body = response.body()
                    val elements = body?.getAsJsonArray("elements") ?: return@launch
                    val list = elements.mapNotNull { element ->
                        try {
                            val obj = JSONObject(element.toString())
                            val id = obj.optLong("id")
                            val lat = obj.optDouble("lat")
                            val lon = obj.optDouble("lon")
                            val tagsJson = obj.optJSONObject("tags") ?: JSONObject()
                            val tags = Place.Element.Tags(
                                addrHousenumber = tagsJson.optString("addr:housenumber"),
                                addrStreet = tagsJson.optString("addr:street"),
                                amenity = tagsJson.optString("amenity"),
                                bench = tagsJson.optString("bench"),
                                bus = tagsJson.optString("bus"),
                                cuisine = tagsJson.optString("cuisine"),
                                healthcare = tagsJson.optString("healthcare"),
                                highway = tagsJson.optString("highway"),
                                historic = tagsJson.optString("historic"),
                                name = tagsJson.optString("name"),
                                openingHours = tagsJson.optString("opening_hours"),
                                `operator` = tagsJson.optString("operator"),
                                operatorWikidata = tagsJson.optString("operator:wikidata"),
                                operatorWikipedia = tagsJson.optString("operator:wikipedia"),
                                place = tagsJson.optString("place"),
                                publicTransport = tagsJson.optString("public_transport"),
                                railway = tagsJson.optString("railway"),
                                ref = tagsJson.optString("ref"),
                                shelter = tagsJson.optString("shelter"),
                                shop = tagsJson.optString("shop"),
                                tourism = tagsJson.optString("tourism"),
                                train = tagsJson.optString("train"),
                                wikidata = tagsJson.optString("wikidata"),
                                wikimediaCommons = tagsJson.optString("wikimedia_commons"),
                                wikipedia = tagsJson.optString("wikipedia"),
                                wikipediaArz = tagsJson.optString("wikipedia:arz"),
                                wikipediaEn = tagsJson.optString("wikipedia:en")
                            )
                            Place.Element(
                                id = id,
                                lat = lat,
                                lon = lon,
                                tags = tags,
                                type = obj.optString("type")
                            )
                        } catch (e: Exception) {
                            e.printStackTrace()
                            null
                        }
                    }
                    _places.value = list

                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun showAiRecommendationsOnMap(recommendations: List<PlaceRecommendation>) {
        println("showAiRecommendationsOnMap $recommendations")
        val newPlaces = recommendations.mapNotNull { rec ->
            val lat = rec.latitude
            val lon = rec.longitude

            if (lat != null && lon != null) {
                Place.Element(
                    id = 0L,
                    lat = lat,
                    lon = lon,
                    tags = Place.Element.Tags(
                        name = rec.name,
                        addrHousenumber = null,
                        addrStreet = null,
                        amenity = null,
                        bench = null,
                        bus = null,
                        cuisine = null,
                        healthcare = null,
                        highway = null,
                        historic = null,
                        openingHours = null,
                        `operator` = null,
                        operatorWikidata = null,
                        operatorWikipedia = null,
                        place = null,
                        publicTransport = null,
                        railway = null,
                        ref = null,
                        shelter = null,
                        shop = null,
                        tourism = null,
                        train = null,
                        wikidata = null,
                        wikimediaCommons = null,
                        wikipedia = null,
                        wikipediaArz = null,
                        wikipediaEn = null
                    ),
                    type = "ai"
                )
            } else {
                null
            }
        }

        _places.value = _places.value + newPlaces
    }
    suspend fun getRecommendationsAi(
        latitude: Double,
        longitude: Double,
        category: String? = null,
        keyword: String? = null
    ): List<PlaceRecommendation> {
        val model = Firebase.ai(backend = GenerativeBackend.googleAI())
            .generativeModel("gemini-2.5-flash")

        val basePrompt = buildString {
            append("Suggest up to 5 places near latitude: $latitude and longitude: $longitude")
            if (!category.isNullOrBlank()) append(" in category $category")
            if (!keyword.isNullOrBlank()) append(" matching keyword $keyword")
            append(". Respond ONLY with a JSON array containing: name, latitude, longitude, distance. No extra text or explanation.")
        }

        val response = model.generateContent(basePrompt)
        println("Prompt: $basePrompt")
        println("AI response raw: ${response.text}")

        val cleanedJson = response.text
            ?.trim()
            ?.removePrefix("```json")
            ?.removePrefix("```")
            ?.removeSuffix("```")
            ?.trim()
            ?: "[]"

        println("Cleaned JSON: $cleanedJson")

        val jsonArray = org.json.JSONArray(cleanedJson)
        val list = mutableListOf<PlaceRecommendation>()
        for (i in 0 until jsonArray.length()) {
            val obj = jsonArray.getJSONObject(i)
            list.add(
                PlaceRecommendation(
                    name = obj.optString("name"),
                    description = obj.optString("description"),
                    historicalPeriod = obj.optString("historicalPeriod"),
                    distance = obj.optString("distance"),
                    latitude = obj.optDouble("latitude"),
                    longitude = obj.optDouble("longitude")
                )
            )
        }

        return list
    }
}