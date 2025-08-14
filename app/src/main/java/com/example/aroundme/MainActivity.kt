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
import com.example.aroundme.ui.screens.MapScreen
import com.example.aroundme.ui.theme.AroundMeTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.tasks.await
import com.google.android.gms.maps.model.LatLng

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

    val locationPermissionState = rememberPermissionState(permission = Manifest.permission.ACCESS_FINE_LOCATION)
    var userLocation by remember { mutableStateOf<LatLng?>(null) }

    LaunchedEffect(Unit) {
        locationPermissionState.launchPermissionRequest()
    }

    LaunchedEffect(locationPermissionState.status) {
        if (locationPermissionState.status.isGranted) {
            try {
                val lastKnown = fusedLocationClient.lastLocation.await()
                if (lastKnown != null) {
                    userLocation = LatLng(lastKnown.latitude, lastKnown.longitude)
                }

                val locationRequest = com.google.android.gms.location.LocationRequest.Builder(
                    com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY,
                    2000
                ).build()

                fusedLocationClient.requestLocationUpdates(
                    locationRequest,
                    object : com.google.android.gms.location.LocationCallback() {
                        override fun onLocationResult(result: com.google.android.gms.location.LocationResult) {
                            val location = result.lastLocation
                            if (location != null) {
                                userLocation = LatLng(location.latitude, location.longitude)
                            }
                        }
                    },
                    context.mainLooper
                )

            } catch (e: SecurityException) {
            }
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        if (userLocation != null) {
            MapScreen(latitude = userLocation!!.latitude, longitude = userLocation!!.longitude)
        } else {
            Text("Konum alınıyor...", modifier = Modifier.padding(16.dp))
        }
    }
}
