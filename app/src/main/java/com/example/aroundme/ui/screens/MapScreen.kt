package com.example.aroundme.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.aroundme.data.model.Place
import com.example.aroundme.ui.cards.PlaceDetailsBottomSheet
import com.example.aroundme.ui.components.InfoDialog
import com.example.aroundme.ui.components.LoadingDialog
import com.example.aroundme.ui.viewmodel.PlacesViewModel
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import java.io.File

@Composable
fun MapScreen(
    latitude: Double,
    longitude: Double,
    onBottomBarVisibleChange: (Boolean) -> Unit,
    placesViewModel: PlacesViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val loading by placesViewModel.loading.collectAsState()
    var showLoading by remember { mutableStateOf(false) }
    var selectedPlace by remember { mutableStateOf<Place.Element?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var hasSearched by remember { mutableStateOf(false) }
    val categories = listOf("Tarihi", "Doğa", "Müze", "Eğlence, Yemek")
    val scope = rememberCoroutineScope()

    LaunchedEffect(selectedPlace) { onBottomBarVisibleChange(selectedPlace == null) }

    LaunchedEffect(loading) {
        showLoading = loading
    }

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
        }
    }

    val userMarker = remember {
        Marker(mapView).apply {
            position = GeoPoint(latitude, longitude)
            title = "Buradasınız"
            mapView.overlays.add(this)
        }
    }

    val places by placesViewModel.places.collectAsState()

    LaunchedEffect(places) {
        mapView.overlays.removeAll { it is Marker && it != userMarker }

        places.forEach { place ->
            val marker = Marker(mapView).apply {
                position = GeoPoint(place.lat ?: 0.0, place.lon ?: 0.0)
                title = place.tags?.name ?: "Gezilecek Yer"
                setOnMarkerClickListener { _, _ ->
                    selectedPlace = place
                    true
                }
            }
            mapView.overlays.add(marker)
        }
        mapView.invalidate()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clipToBounds()
    ) {
        AndroidView(
            factory = { mapView },
            modifier = Modifier.fillMaxSize()
        )

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
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                    disabledTextColor = Color.Gray,
                    cursorColor = MaterialTheme.colorScheme.primary,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    disabledContainerColor = Color.White,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                trailingIcon = {
                    IconButton(onClick = {
                        performSearch(
                            searchQuery,
                            selectedCategory,
                            placesViewModel,
                            mapView,
                            userMarker,
                            places
                        )
                        hasSearched = true
                        keyboardController?.hide()
                    }) {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = "Ara",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Search
                ),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        hasSearched = true
                        keyboardController?.hide()

                        scope.launch {
//                            kotlinx.coroutines.delay(100)
                            performSearch(
                                searchQuery,
                                selectedCategory,
                                placesViewModel,
                                mapView,
                                userMarker,
                                places
                            )
                        }
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
                        onClick = { selectedCategory = if (isSelected) null else category },
                        label = { Text(category, color = Color.Black) },
                        shape = RoundedCornerShape(16.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = Color.White,
                            selectedContainerColor = Color(0xFFE0E0E0),
                            labelColor = Color.Black,
                            selectedLabelColor = Color.Black
                        )
                    )
                }
            }
        }

        LaunchedEffect(selectedCategory) {
            selectedCategory?.let { category ->
                try {
                    placesViewModel.fetchAiRecommendations(
                        latitude,
                        longitude,
                        category,
                        searchQuery
                    )
                } catch (e: Exception) {
                    Log.d("MapScreen","Recommendation response exception: $e")
                }
            }
        }
        if (showLoading) {
            LoadingDialog(
                message = if (!selectedCategory.isNullOrBlank()) "Öneriler Alınıyor..." else "Arama Yapılıyor...",
                icon = if (!selectedCategory.isNullOrBlank()) Icons.Default.Star else Icons.Default.Search
            )
        }

        selectedPlace?.let { place ->
            PlaceDetailsBottomSheet(
                place = place,
                onClose = { selectedPlace = null }
            )
        }
        var showInfo by remember { mutableStateOf(false) }

        LaunchedEffect(loading) {
            if (!loading && hasSearched) {
                kotlinx.coroutines.delay(300)
                if (places.isEmpty()) {
                    showInfo = true
                }
            }
        }

        if (showInfo) {
            InfoDialog(
                message = "Sonuç bulunamadı 😕",
                onDismiss = { showInfo = false }
            )
        }
    }
}

private fun performSearch(
    query: String,
    selectedCategory: String?,
    placesViewModel: PlacesViewModel,
    mapView: MapView,
    userMarker: Marker,
    places: List<Place.Element>
) {
    if (query.isBlank()) {
        mapView.overlays.removeAll { it is Marker && it != userMarker }
    } else {
        placesViewModel.setLoading(true)
        val center = mapView.mapCenter as GeoPoint
        val lat = center.latitude
        val lon = center.longitude

        val delta = 0.05
        val minLat = lat - delta
        val minLon = lon - delta
        val maxLat = lat + delta
        val maxLon = lon + delta

        placesViewModel.searchAttractions(query, selectedCategory, minLat, minLon, maxLat, maxLon)

        val firstPlace = places.firstOrNull { it.tags?.name?.contains(query, true) == true }
        firstPlace?.let {
            val target = GeoPoint(it.lat ?: 0.0, it.lon ?: 0.0)
            mapView.controller.animateTo(target)
            mapView.controller.setZoom(16.0)
        }
    }
}


