package com.example.aroundme.ui

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.aroundme.ui.screens.MapScreen
import com.example.aroundme.ui.theme.AroundMeTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.aroundme.ui.screens.FavoritesScreen
import com.example.aroundme.ui.screens.LoginScreen
import com.example.aroundme.ui.screens.SignUpScreen
import com.example.aroundme.ui.viewmodel.LocationViewModel
import com.example.aroundme.ui.viewmodel.PlacesViewModel
import com.example.aroundme.ui.viewmodel.UserViewModel
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
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
fun MainScreen(
    modifier: Modifier = Modifier,
    locationViewModel: LocationViewModel = hiltViewModel(),
    userViewModel: UserViewModel = hiltViewModel(),
    placesViewModel: PlacesViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    val isUserLoggedIn by userViewModel.isUserLoggedIn.collectAsState()

    var showBottomBar by remember { mutableStateOf(true) }

    LaunchedEffect(isUserLoggedIn) {
        if (isUserLoggedIn) {
            navController.navigate("map") { popUpTo(0) { inclusive = true } }
        } else {
            navController.navigate("login") { popUpTo(0) { inclusive = true } }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            if (isUserLoggedIn && showBottomBar) {
                BottomBar(navController, userViewModel,placesViewModel)
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            NavHost(navController = navController, startDestination = "login") {
                composable("login") { LoginScreen(navController) }
                composable("signup") { SignUpScreen(navController) }
                composable("map") {
                    val locationPermissionState =
                        rememberPermissionState(permission = Manifest.permission.ACCESS_FINE_LOCATION)
                    val userLocation by locationViewModel.userLocation.collectAsState()

                    LaunchedEffect(Unit) { locationPermissionState.launchPermissionRequest() }
                    LaunchedEffect(locationPermissionState.status) {
                        if (locationPermissionState.status.isGranted) {
                            locationViewModel.startLocationUpdates()
                        }
                    }

                    if (userLocation != null) {
                        MapScreen(
                            latitude = userLocation!!.latitude,
                            longitude = userLocation!!.longitude,
                            placesViewModel = placesViewModel,
                            onBottomBarVisibleChange = { isVisible ->
                                showBottomBar = isVisible
                            }
                        )
                    } else {
                        Text("Konum alınıyor...", modifier = Modifier.padding(16.dp))
                    }
                }
                composable("favorite") { FavoritesScreen() }
            }
        }
    }
}

@Composable
fun BottomBar(navController: NavController, userViewModel: UserViewModel,placesViewModel: PlacesViewModel ) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(onClick = {
                    navController.navigate("map")
                }) {
                    Icon(Icons.Default.Home, contentDescription = "Ana Sayfa", tint = MaterialTheme.colorScheme.primary)
                }
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(onClick = {
                    val uid = FirebaseAuth.getInstance().currentUser?.uid
                    if (uid != null) {
                        navController.navigate("favorite")
                    } else {
                        navController.navigate("login")
                    }
                }) {
                    Icon(Icons.Default.Favorite, contentDescription = "Favoriler", tint = MaterialTheme.colorScheme.primary)
                }
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(onClick = {
                    userViewModel.logoutUser()
                    placesViewModel.clearMapData()
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                }) {
                    Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Çıkış", tint = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}


