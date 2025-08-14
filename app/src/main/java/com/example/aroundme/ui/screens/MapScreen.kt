package com.example.aroundme.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.util.GeoPoint

@Composable
fun MapScreen(latitude: Double, longitude: Double, modifier: Modifier = Modifier) {
    AndroidView(factory = { context ->
        MapView(context).apply {
            setMultiTouchControls(true)
            controller.setZoom(15.0)
            controller.setCenter(GeoPoint(latitude, longitude))

            val marker = Marker(this)
            marker.position = GeoPoint(latitude, longitude)
            marker.title = "Konumunuz"
            this.overlays.add(marker)
        }
    }, modifier = modifier)
}
