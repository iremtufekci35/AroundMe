package com.example.aroundme.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aroundme.data.model.Place
import com.example.aroundme.data.remote.RetrofitInstance
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

    fun searchAttractionsByName(name: String) {
        loadTouristAttractionsInternal(name)
    }

    private fun loadTouristAttractionsInternal(searchQuery: String?) {
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
                            Place.Element(id = id, lat = lat, lon = lon, tags = tags, type = obj.optString("type"))
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
}