package com.example.aroundme.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.aroundme.data.model.Place
import com.example.aroundme.ui.cards.PlaceDetailsCard
import com.example.aroundme.ui.viewmodel.PlacesViewModel
import com.google.firebase.auth.FirebaseAuth
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
    var shouldShowDialog = remember { mutableStateOf(false) }
    val loading by placesViewModel.loading.collectAsState()
    var showBottomBar by remember { mutableStateOf(false) }

    val userId = FirebaseAuth.getInstance().currentUser?.uid
    showBottomBar = userId != null

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

    val places by placesViewModel.places.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var selectedPlace by remember { mutableStateOf<Place.Element?>(null) }
    val categories = listOf("Tarihi", "Doğa", "Müze", "Eğlence, Yemek")
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    val userMarker = remember { Marker(mapView) }

    LaunchedEffect(selectedPlace) {
        onBottomBarVisibleChange(selectedPlace == null)
    }

    Box(modifier = Modifier.fillMaxSize()) {

        AndroidView(
            factory = { mapView },
            modifier = Modifier.fillMaxSize() .padding(bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding())

        ) { map ->
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
                    IconButton(
                        onClick = {
                            if (searchQuery.isBlank()) {
                                mapView.overlays.removeAll { it is Marker && it != userMarker }
                            } else {
                                placesViewModel.setLoading(true)
                                placesViewModel.searchAttractions(searchQuery, selectedCategory)
                                val firstPlace = places.firstOrNull {
                                    it.tags?.name?.contains(searchQuery, true) == true
                                }
                                firstPlace?.let {
                                    mapView.controller.animateTo(GeoPoint(it.lat ?: 0.0, it.lon ?: 0.0))
                                    mapView.controller.setZoom(15.0)
                                }
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
                            // placesViewModel.searchAttractions(searchQuery, selectedCategory)
                        },
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
                    println("fetch ai recommendations")
                    /** too many request and can not show location lat and lon */
                    // placesViewModel.fetchAiRecommendations(latitude, longitude, category)
                    println("fetch ai recommendations end")
                } catch (e: Exception) {
                    println("Recommendation response: $e")
                    shouldShowDialog.value = false
                }
            }
        }

        if (loading) {
            AlertDialogShow(
                message = "Öneriler Alınıyor..."
            )
        }

        selectedPlace?.let { place ->
            PlaceDetailsCard(place = place, onClose = { selectedPlace = null })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertDialogShow(
    message: String
) {
        AlertDialog(
            onDismissRequest = { },
            shape = RoundedCornerShape(24.dp),
            tonalElevation = 8.dp,
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.primary,
            textContentColor = MaterialTheme.colorScheme.onSurface,
            title = {
                Text(
                    text = "Bilgi",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    CircularProgressIndicator(
                        modifier = Modifier.size(32.dp),
                        strokeWidth = 3.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            },
            confirmButton = {},
            dismissButton = {}
        )
}
