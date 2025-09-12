package com.example.aroundme.ui

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.aroundme.ui.screens.MapScreen
import com.example.aroundme.ui.theme.AroundMeTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.aroundme.ui.screens.LoginScreen
import com.example.aroundme.ui.screens.SignUpScreen
import com.example.aroundme.ui.viewmodel.LocationViewModel
import com.example.aroundme.ui.viewmodel.UserViewModel
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
fun MainScreen(modifier: Modifier = Modifier,
               locationViewModel: LocationViewModel = hiltViewModel(),
               userViewModel: UserViewModel = hiltViewModel()) {

    val navController = rememberNavController()
    val isUserLoggedIn by userViewModel.isUserLoggedIn.collectAsState()

    LaunchedEffect(isUserLoggedIn) {
        if (isUserLoggedIn) {
            navController.navigate("map") {
                popUpTo(0) { inclusive = true }
            }
        } else {
            navController.navigate("login") {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    NavHost(navController = navController, startDestination = "login", modifier = modifier) {
        composable("login") { LoginScreen(navController) }
        composable("signup") { SignUpScreen(navController) }
        composable("map") {
            val locationPermissionState = rememberPermissionState(permission = Manifest.permission.ACCESS_FINE_LOCATION)
            val userLocation by locationViewModel.userLocation.collectAsState()

            LaunchedEffect(Unit) { locationPermissionState.launchPermissionRequest() }
            LaunchedEffect(locationPermissionState.status) {
                if (locationPermissionState.status.isGranted) locationViewModel.startLocationUpdates()
            }
            if (userLocation != null) {
                MapScreen(
                    latitude = userLocation!!.latitude,
                    longitude = userLocation!!.longitude,
                    navController = navController
                )

            } else {
                Text("Konum alınıyor...", modifier = Modifier.padding(16.dp))
            }
        }
    }
}
