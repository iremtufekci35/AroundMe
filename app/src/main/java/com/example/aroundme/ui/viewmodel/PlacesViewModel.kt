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

    private val _places = MutableStateFlow<List<Place>>(emptyList())
    val places: StateFlow<List<Place>> = _places

    fun loadTouristAttractions() {
        loadTouristAttractionsInternal(null)
    }

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
                            val lat = obj.getDouble("lat")
                            val lon = obj.getDouble("lon")
                            val name = obj.getJSONObject("tags").optString("name") ?: "Gezilecek Yer"
                            Place(name, lat, lon)
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