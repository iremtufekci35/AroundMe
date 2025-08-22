package com.example.aroundme.ui.cards

import android.app.Fragment
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.aroundme.data.model.Place
import com.example.aroundme.utils.Translations

@Composable
fun PlaceDetailsCard(place: Place.Element, onClose: () -> Unit) {
    val tags = place.tags
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            ),
            elevation = CardDefaults.cardElevation(12.dp),
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 20.dp, vertical = 16.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier.width(50.dp).height(5.dp).padding(bottom = 12.dp)
                        .background(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(50)
                        )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = tags?.name ?: "Bilinmiyor",
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onClose) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Kapat",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                val streetInfo = tags?.addrHousenumber ?: tags?.addrStreet
                streetInfo?.takeUnless { it.isBlank() }?.let { value ->
                    InfoRow(icon = Icons.Default.Place, label = "Sokak / Ev", value = value)
                }

                tags?.historic?.takeUnless { it.isBlank() }?.let {
                    val translated = Translations.translate(Translations.historicMap, it)
                    InfoRow(Icons.Default.AccountBalance, "Tarihî", translated ?: it)
                }

                tags?.bus?.takeUnless { it.isBlank() }?.let {
                    InfoRow(Icons.Default.DirectionsBus, "Otobüs", it)
                }

                tags?.amenity?.takeUnless { it.isBlank() }?.let {
                    val translated = Translations.translate(Translations.amenityMap, it)
                    InfoRow(Icons.Default.LocalOffer, "Tesis / Hizmet", translated ?: it)
                }

                tags?.cuisine?.takeUnless { it.isBlank() }?.let {
                    val translated = Translations.translate(Translations.cuisineMap, it)
                    InfoRow(Icons.Default.Restaurant, "Mutfak", translated ?: it)
                }

                tags?.healthcare?.takeUnless { it.isBlank() }?.let {
                    val translated = Translations.translate(Translations.healthcareMap, it)
                    InfoRow(Icons.Default.LocalHospital, "Sağlık", translated ?: it)
                }

                tags?.openingHours?.takeUnless { it.isBlank() }?.let {
                    InfoRow(Icons.Default.Schedule, "Açılış Saatleri", it)
                }

                tags?.operator?.takeUnless { it.isBlank() }?.let {
                    InfoRow(Icons.Default.AccountBox, "İşletmeci", it)
                }
                tags?.place?.takeUnless { it.isBlank() }?.let {
                    val translated = Translations.translate(Translations.placeMap, it)
                    InfoRow(Icons.Default.LocationCity, "Yer", translated ?: it)
                }

                tags?.publicTransport?.takeUnless { it.isBlank() }?.let {
                    val translated = Translations.translate(Translations.publicTransportMap, it)
                    InfoRow(Icons.Default.Directions, "Toplu Taşıma", translated ?: it)
                }

                tags?.railway?.takeUnless { it.isBlank() }?.let {
                    val translated = Translations.translate(Translations.railwayMap, it)
                    InfoRow(Icons.Default.Train, "Tren İstasyonu", translated ?: it)
                }

                tags?.shop?.takeUnless { it.isBlank() }?.let {
                    val translated = Translations.translate(Translations.shopMap, it)
                    InfoRow(Icons.Default.Store, "Mağaza", translated ?: it)
                }

                tags?.tourism?.takeUnless { it.isBlank() }?.let {
                    val translated = Translations.translate(Translations.tourismMap, it)
                    InfoRow(Icons.Default.Tour, "Turizm", translated ?: it)
                }

                tags?.train?.takeUnless { it.isBlank() }?.let {
                    val translated = Translations.translate(Translations.railwayMap, it)
                    InfoRow(Icons.Default.Train, "Tren", translated ?: it)
                }
            }
        }
    }
}

@Composable
fun InfoRow(icon: ImageVector, label: String, value: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(text = label, style = MaterialTheme.typography.labelLarge)
                Text(text = value, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
