package com.example.aroundme.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import com.example.aroundme.data.model.Place
import com.example.aroundme.ui.cards.PlaceDetailsCard
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavHostController
import com.example.aroundme.ui.viewmodel.UserViewModel
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import java.io.File

@Composable
fun MapScreen(
    latitude: Double,
    longitude: Double,
    placesViewModel: PlacesViewModel = hiltViewModel(),
    navController: NavHostController
) {
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val userViewModel: UserViewModel = hiltViewModel()

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

    Box(modifier = Modifier.fillMaxSize()) {

        AndroidView(
            factory = { mapView },
            modifier = Modifier.fillMaxSize()
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
//                            placesViewModel.searchAttractions(searchQuery, selectedCategory)
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
                    val recommendationResponse = placesViewModel.getRecommendationsAi(
                        latitude,
                        longitude,
                        selectedCategory
                    )
                    println("response burada: $recommendationResponse")
                    for (place in recommendationResponse) {
                        placesViewModel.searchAttractions(place.name, category)
                        /** when you can get the recommendation than take this place names and show them on the map */
                    }
                } catch (e: Exception) {
                    println("Recommendation response: $e")
                }
            }
        }
        selectedPlace?.let { place ->
            PlaceDetailsCard(place = place, onClose = { selectedPlace = null })
        }

        if (selectedPlace == null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .background(Color.White)
                    .align(Alignment.BottomCenter),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        IconButton(onClick = { /* Hesap */ }) {
                            Icon(Icons.Default.Person, contentDescription = "Hesap")
                        }
                        Text("Hesap", style = MaterialTheme.typography.labelSmall)
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        IconButton(onClick = { /* Favoriler */ }) {
                            Icon(Icons.Default.Favorite, contentDescription = "Favoriler")
                        }
                        Text("Favoriler", style = MaterialTheme.typography.labelSmall)
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        IconButton(onClick = {
                            userViewModel.logoutUser()
                            navController.navigate("login") {
                                popUpTo(0) { inclusive = true }
                            }
                        }) {
                            Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Çıkış")
                        }
                        Text("Çıkış", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}











