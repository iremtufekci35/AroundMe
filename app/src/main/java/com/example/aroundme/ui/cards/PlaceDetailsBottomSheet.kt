package com.example.aroundme.ui.cards

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.aroundme.data.model.Place
import com.example.aroundme.data.repository.CommentRepository
import com.example.aroundme.data.repository.FavoriteRepository
import com.example.aroundme.utils.Translations
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaceDetailsBottomSheet(
    place: Place.Element,
    onClose: () -> Unit
) {

    ModalBottomSheet(
        onDismissRequest = onClose,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        PlaceDetailsContent(place = place, onClose = onClose)
    }
}

@Composable
private fun PlaceDetailsContent(place: Place.Element, onClose: () -> Unit) {
    val tags = place.tags
    val favoriteRepository = FavoriteRepository()
    val commentRepository = CommentRepository()
    val firebaseUser = FirebaseAuth.getInstance().currentUser
    val userId = firebaseUser?.uid ?: ""
    val userName = firebaseUser?.displayName ?: "Anonim"
    val isFavorite = remember { mutableStateOf(false) }
    var commentText by remember { mutableStateOf("") }
    var rating by remember { mutableIntStateOf(0) }
    var isCommentSent by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .width(60.dp)
                .height(6.dp)
                .padding(bottom = 12.dp)
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
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = tags?.name ?: "Bilinmiyor",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = {
                favoriteRepository.addFavorite(
                    userId,
                    place.id.toString(),
                    tags?.name.orEmpty()
                ) { success -> if (success) isFavorite.value = true }
            }) {
                Icon(
                    imageVector = if (isFavorite.value) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Favori",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onClose) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Kapat",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

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
        Spacer(modifier = Modifier.height(8.dp))

        infoList.forEach { (value, icon, label) ->
            value?.takeUnless { it.isBlank() }?.let { originalText ->
                val translatedText = remember { mutableStateOf<String?>(null) }

                LaunchedEffect(originalText) {
                    Translations.translateText(originalText) { result ->
                        translatedText.value = result
                    }
                }

                InfoRow(
                    icon = icon,
                    label = label,
                    value = translatedText.value ?: originalText
                )
            }
        }
        if (!isCommentSent) {
            OutlinedTextField(
                value = commentText,
                onValueChange = { commentText = it },
                label = { Text("Yorum Yazın") },
                modifier = Modifier.fillMaxWidth()
                    .heightIn(min = 56.dp, max = 70.dp),
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                repeat(5) { index ->
                    IconButton(onClick = { rating = index + 1 }) {
                        Icon(
                            imageVector = if (index < rating) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Button(onClick = {
                if (commentText.isNotBlank()) {
                    commentRepository.addComment(
                        userId = userId,
                        placeId = place.id.toString(),
                        placeName = tags?.name.orEmpty(),
                        userComment = commentText,
                        userName = userName,
                        rating = rating
                    ) { success ->
                        if (success) {
                            isCommentSent = true
                        }
                    }
                }
            }) {
                Text("Gönder")
            }
        } else {
            Text("Yorumunuz gönderildi!", color = MaterialTheme.colorScheme.primary)
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
