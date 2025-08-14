package com.example.aroundme

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.aroundme.ui.ShowInfoDialog
import com.example.aroundme.ui.theme.AroundMeTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.tasks.await

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AroundMeTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainScreen(Modifier.padding(innerPadding))
                }
            }
        }
    }
}
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MainScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    var locationText by remember { mutableStateOf("Konum alınıyor...") }

    val locationPermissionState = rememberPermissionState(permission = Manifest.permission.ACCESS_FINE_LOCATION)

    LaunchedEffect(Unit) {
        locationPermissionState.launchPermissionRequest()
    }

    LaunchedEffect(locationPermissionState.status) {
        if (locationPermissionState.status.isGranted) {
            val playServicesAvailable = com.google.android.gms.common.GoogleApiAvailability
                .getInstance()
                .isGooglePlayServicesAvailable(context)

            if (playServicesAvailable == com.google.android.gms.common.ConnectionResult.SUCCESS) {
                try {
                    val location = fusedLocationClient.lastLocation.await()
                    locationText = if (location != null) {
                        "Konum: ${location.latitude}, ${location.longitude}"
                    } else {
                        "Konum alınamadı"
                    }
                } catch (e: Exception) {
                    locationText = "Hata: ${e.localizedMessage}"
                }
            } else {
                locationText = "Google Play Hizmetleri eksik veya uyumsuz"
            }
        } else {
            locationText = "Konum izni verilmedi"
        }
    }

    Text(text = locationText, modifier = modifier.padding(16.dp))
    ShowInfoDialog(locationText,true)
}
