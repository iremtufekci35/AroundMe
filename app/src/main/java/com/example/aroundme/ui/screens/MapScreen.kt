package com.example.aroundme.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.aroundme.ui.viewmodel.PlacesViewModel
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import androidx.compose.material3.TextFieldDefaults

@Composable
fun MapScreen(
    latitude: Double,
    longitude: Double,
    placesViewModel: PlacesViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val mapView = remember {
        MapView(context).apply {
            setMultiTouchControls(true)
            controller.setZoom(15.0)
        }
    }

    val places by placesViewModel.places.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    val userMarker = remember { Marker(mapView) }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(factory = { mapView }, modifier = Modifier.fillMaxSize()) { map ->
            userMarker.position = GeoPoint(latitude, longitude)
            userMarker.title = "Buradasınız"
            map.controller.setCenter(GeoPoint(latitude, longitude))
            if (!map.overlays.contains(userMarker)) {
                map.overlays.add(userMarker)
            }

            map.overlays.removeAll { it is Marker && it != userMarker }
            places.forEach { place ->
                val marker = Marker(map)
                marker.position = GeoPoint(place.lat, place.lon)
                marker.title = place.name
                map.overlays.add(marker)
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .align(Alignment.TopCenter)
        ) {
            TextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Gezilecek yer ara…") },
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                    focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                    unfocusedIndicatorColor = MaterialTheme.colorScheme.surface
                ),
                trailingIcon = {
                    IconButton(
                        onClick = { placesViewModel.searchAttractionsByName(searchQuery) }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Ara",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        }
    }
}
