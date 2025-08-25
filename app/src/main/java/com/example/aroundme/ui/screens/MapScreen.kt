package com.example.aroundme.ui.screens

import android.view.InputDevice
import android.view.MotionEvent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import java.io.File

@Composable
fun MapScreen(
    latitude: Double,
    longitude: Double,
    placesViewModel: PlacesViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    LaunchedEffect(Unit) {
        Configuration.getInstance().apply {
            osmdroidBasePath = File(context.cacheDir, "osmdroid")
            osmdroidTileCache = File(context.cacheDir, "osmdroid/tiles")
            userAgentValue = context.packageName
        }
    }

    val mapView = remember {
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setUseDataConnection(true)
            setMultiTouchControls(true)
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
    val categories = listOf("Tarihi", "Doğa", "Müze", "Eğlence, Yemek")
    var selectedCategory by remember { mutableStateOf<String?>(null) }

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

        Column(
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
                    .fillMaxWidth()
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
                            placesViewModel.searchAttractions(searchQuery, selectedCategory)
                            val firstPlace = places.firstOrNull {
                                it.tags?.name?.contains(searchQuery, true) == true
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
                },
                keyboardActions = KeyboardActions(
                    onSearch = {
                        placesViewModel.searchAttractions(searchQuery, selectedCategory)
                        val firstPlace = places.firstOrNull {
                            it.tags?.name?.contains(searchQuery, true) == true
                        }
                        firstPlace?.let {
                            mapView.controller.animateTo(GeoPoint(it.lat ?: 0.0, it.lon ?: 0.0))
                            mapView.controller.setZoom(15.0)
                        }
                        keyboardController?.hide()
                    }
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { category ->
                    val isSelected = selectedCategory == category
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            selectedCategory = if (isSelected) null else category
                            placesViewModel.searchAttractions(searchQuery, selectedCategory)
                        },
                        label = { Text(category) },
                        enabled = true,
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = if (isSelected)
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                            else
                                MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                            labelColor = if (isSelected)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurface
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            selected = isSelected,
                            enabled = true,
                            borderColor = if (isSelected)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.height(36.dp)
                    )
                }
            }
        }

        selectedPlace?.let { place ->
            PlaceDetailsCard(place = place, onClose = { selectedPlace = null })
        }
    }
}







