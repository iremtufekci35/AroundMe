package com.example.aroundme.ui.cards

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
            elevation = CardDefaults.cardElevation(16.dp),
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 20.dp, vertical = 16.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .width(60.dp)
                        .height(6.dp)
                        .padding(bottom = 16.dp)
                        .background(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(50)
                        )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Place,
                        contentDescription = "",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = tags?.name ?: "Bilinmiyor",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onClose) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Kapat",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                val infoList = listOf(
                    Triple(tags?.addrHousenumber ?: tags?.addrStreet, Icons.Default.Place, "Sokak / Ev"),
                    Triple(tags?.historic, Icons.Default.AccountBalance, "Tarihî"),
                    Triple(tags?.bus, Icons.Default.DirectionsBus, "Otobüs"),
                    Triple(tags?.amenity, Icons.Default.LocalOffer, "Tesis / Hizmet"),
                    Triple(tags?.cuisine, Icons.Default.Restaurant, "Mutfak"),
                    Triple(tags?.healthcare, Icons.Default.LocalHospital, "Sağlık"),
                    Triple(tags?.openingHours, Icons.Default.Schedule, "Açılış Saatleri"),
                    Triple(tags?.operator, Icons.Default.AccountBox, "İşletmeci"),
                    Triple(tags?.place, Icons.Default.LocationCity, "Yer"),
                    Triple(tags?.publicTransport, Icons.Default.Directions, "Toplu Taşıma"),
                    Triple(tags?.railway, Icons.Default.Train, "Tren İstasyonu"),
                    Triple(tags?.shop, Icons.Default.Store, "Mağaza"),
                    Triple(tags?.tourism, Icons.Default.Tour, "Turizm"),
                    Triple(tags?.train, Icons.Default.Train, "Tren")
                )

                infoList.forEach { (value, icon, label) ->
                    value?.takeUnless { it.isBlank() }?.let {
                        val translated = when (label) {
                            "Tarihî" -> Translations.translate(Translations.historicMap, it)
                            "Tesis / Hizmet" -> Translations.translate(Translations.amenityMap, it)
                            "Mutfak" -> Translations.translate(Translations.cuisineMap, it)
                            "Sağlık" -> Translations.translate(Translations.healthcareMap, it)
                            "Yer" -> Translations.translate(Translations.placeMap, it)
                            "Toplu Taşıma" -> Translations.translate(Translations.publicTransportMap, it)
                            "Tren İstasyonu", "Tren" -> Translations.translate(Translations.railwayMap, it)
                            "Mağaza" -> Translations.translate(Translations.shopMap, it)
                            "Turizm" -> Translations.translate(Translations.tourismMap, it)
                            else -> it
                        }
                        InfoRow(icon = icon, label = label, value = translated ?: it)
                    }
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
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

