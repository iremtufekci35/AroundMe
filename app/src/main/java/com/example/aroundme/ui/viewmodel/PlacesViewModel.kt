package com.example.aroundme.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aroundme.data.model.Place
import com.example.aroundme.data.model.PlaceRecommendation
import com.example.aroundme.data.remote.RetrofitInstance
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import dagger.hilt.android.lifecycle.HiltViewModel
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
            } catch (e: Exception){
                _error.value = e.message
            } finally {
                _loading.value = false
            }
        }
    }

    fun fetchAiRecommendations(latitude: Double, longitude: Double, category: String) {
        viewModelScope.launch {
            _loading.value = true
            try {
                val recommendationResponse = getRecommendationsAi(latitude, longitude, category)
                _recommendations.value = recommendationResponse

                recommendationResponse.forEach { rec ->
                    loadTouristAttractionsInternal(rec.name)
                }
                showAiRecommendationsOnMap(recommendationResponse)
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _loading.value = false
            }
        }
    }
    fun setLoading(value: Boolean) {
        _loading.value = value
    }
     fun loadTouristAttractionsInternal(searchQuery: String?) {
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
    suspend fun showAiRecommendationsOnMap(recommendations: List<PlaceRecommendation>) {
        val newPlaces = recommendations.mapNotNull { rec ->
            try {
                val query = """
                [out:json];
                node
                  [name~"${rec.name}", i]
                  (38.40,27.10,38.45,27.20);
                out;
            """.trimIndent()

                val response = RetrofitInstance.api.getTouristAttractions(query)
                if (response.isSuccessful) {
                    val elements = response.body()?.getAsJsonArray("elements") ?: return@mapNotNull null
                    val first = elements.firstOrNull() ?: return@mapNotNull null
                    val obj = JSONObject(first.toString())
                    val lat = obj.optDouble("lat")
                    val lon = obj.optDouble("lon")

                    Place.Element(
                        id = obj.optLong("id"),
                        lat = lat,
                        lon = lon,
                        tags = Place.Element.Tags(
                            addrHousenumber = null,
                            addrStreet = null,
                            amenity = null,
                            bench = null,
                            bus = null,
                            cuisine = null,
                            healthcare = null,
                            highway = null,
                            historic = null,
                            name = rec.name,
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

                } else null
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }

        _places.value = _places.value + newPlaces
    }

    fun parseAiResponse(text: String): List<PlaceRecommendation> {
        val lines = text.lines()
        val places = mutableListOf<PlaceRecommendation>()

        var currentName = ""
        var currentDesc = ""
        var currentSignificance = ""
        var currentProximity: String? = null

        val titleRegex = Regex("""\d+\.\s*\*\*(.+?)\*\*:?(\s*\((.+?)\))?""")
        val labelValueRegex = Regex("""\*\*\s*(.+?)\s*:\s*\*\*\s*(.+)""")

        lines.forEach { line ->
            titleRegex.find(line)?.let { match ->
                if (currentName.isNotEmpty()) {
                    places.add(
                        PlaceRecommendation(
                            currentName,
                            currentDesc,
                            currentSignificance,
                            currentProximity
                        )
                    )
                }

                currentName = match.groupValues[1].trim()
                currentDesc = ""
                currentSignificance = ""
                currentProximity = match.groupValues.getOrNull(3)?.trim()
            }

            labelValueRegex.find(line)?.let { match ->
                val (label, value) = match.destructured
                when (label.lowercase()) {
                    "description" -> currentDesc = value
                    "historical significance" -> currentSignificance = value
                    "proximity" -> currentProximity = value
                }
            }
        }

        if (currentName.isNotEmpty()) {
            places.add(
                PlaceRecommendation(
                    currentName,
                    currentDesc,
                    currentSignificance,
                    currentProximity
                )
            )
        }

        println("places: $places")
        return places
    }

    suspend fun getRecommendationsAi(
        latitude: Double,
        longitude: Double,
        category: String? = null
    ): List<PlaceRecommendation> {
        val model = Firebase.ai(backend = GenerativeBackend.googleAI())
            .generativeModel("gemini-2.5-flash")

        val basePrompt =
            "Suggest $category places near latitude: $latitude and longitude: $longitude"
        val prompt = if (!category.isNullOrBlank()) {
            "$basePrompt that belong to the category: $category"
        } else {
            basePrompt
        }

        val response = model.generateContent(prompt)
        println("AI response: ${response.text}")

        return parseAiResponse(response.text.orEmpty())
    }
}