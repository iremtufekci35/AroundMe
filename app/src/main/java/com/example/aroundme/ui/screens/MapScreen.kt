package com.example.aroundme.ui.screens

import android.view.InputDevice
import android.view.MotionEvent
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
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import com.example.aroundme.data.model.Place
import com.example.aroundme.ui.cards.PlaceDetailsCard
import org.osmdroid.views.CustomZoomButtonsController

@Composable
fun MapScreen(
    latitude: Double,
    longitude: Double,
    placesViewModel: PlacesViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current

    val mapView = remember {
        MapView(context).apply {
            setMultiTouchControls(true)
            zoomController.setVisibility(CustomZoomButtonsController.Visibility.SHOW_AND_FADEOUT)
            controller.setZoom(15.0)
            controller.setCenter(GeoPoint(latitude, longitude))

            setOnGenericMotionListener { _, event ->
                if (event.action == MotionEvent.ACTION_SCROLL &&
                    event.isFromSource(InputDevice.SOURCE_CLASS_POINTER)
                ) {
                    val scrollValue = event.getAxisValue(MotionEvent.AXIS_VSCROLL)
                    val currentZoom = zoomLevelDouble
                    val zoomStep = 1.0

                    if (scrollValue < 0) controller.setZoom(currentZoom - zoomStep)
                    else if (scrollValue > 0) controller.setZoom(currentZoom + zoomStep)
                    true
                } else false
            }
        }
    }

    val places by placesViewModel.places.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var selectedPlace by remember { mutableStateOf<Place.Element?>(null) }

    val userMarker = remember { Marker(mapView) }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(factory = { mapView }, modifier = Modifier.fillMaxSize()) { map ->
            userMarker.position = GeoPoint(latitude, longitude)
            userMarker.title = "Buradasınız"
            if (!map.overlays.contains(userMarker)) map.overlays.add(userMarker)

            map.overlays.removeAll { it is Marker && it != userMarker }

            places.forEach { place ->
                val marker = Marker(map)
                marker.position = GeoPoint(place.lat ?: 0.0, place.lon ?: 0.0)
                marker.title = place.tags?.name ?: "Gezilecek Yer"
                marker.setOnMarkerClickListener { _, _ ->
                    selectedPlace = place
                    true
                }
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
                    focusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                    unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                    cursorColor = MaterialTheme.colorScheme.primary
                ),
                trailingIcon = {
                    IconButton(
                        onClick = {
                            placesViewModel.searchAttractionsByName(searchQuery)
                            val firstPlace = places.firstOrNull {
                                it.tags?.name?.contains(
                                    searchQuery,
                                    true
                                ) == true
                            }
                            firstPlace?.let {
                                mapView.controller.animateTo(GeoPoint(it.lat ?: 0.0, it.lon ?: 0.0))
                                mapView.controller.setZoom(15.0)
                            }
                            keyboardController?.hide()
                        }
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

            selectedPlace?.let { place ->
            PlaceDetailsCard(place = place, onClose = { selectedPlace = null })
        }
    }
}







