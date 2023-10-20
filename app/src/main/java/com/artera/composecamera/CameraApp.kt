package com.artera.composecamera

import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.artera.composecamera.ui.common.PurchaseHelper
import com.artera.composecamera.ui.navigation.Screen
import com.artera.composecamera.ui.screen.camera.CameraScreen
import com.artera.composecamera.ui.screen.home.HomeScreen
import com.artera.composecamera.ui.screen.location.LocationScreen
import com.artera.composecamera.ui.screen.payment.PaymentScreen
import com.artera.composecamera.ui.theme.ComposeCameraTheme

@Composable
fun CameraApp(
    purchaseHelper: PurchaseHelper,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        modifier = modifier
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    navigateToCamera = {
                        navController.navigate(Screen.Camera.route)
                    },
                    navigateToLocation = {
                        navController.navigate(Screen.Location.route)
                    },
                    navigateToPayment = {
                        navController.navigate(Screen.Payment.route)
                    },
                )
            }
            composable(Screen.Camera.route) {
                CameraScreen(
                    navigateBack = {
                        navController.navigateUp()
                    },
                )
            }
            composable(Screen.Location.route) {
                LocationScreen(
                    navigateBack = {
                        navController.navigateUp()
                    },
                )
            }
            composable(Screen.Payment.route) {
                PaymentScreen(
                    purchaseHelper,
                    navigateBack = {
                        navController.navigateUp()
                    },
                )
            }
        }
    }
}

//@Preview(showBackground = true)
//@Composable
//fun SubmissionAppPreview() {
//    ComposeCameraTheme {
//        CameraApp(PurchaseHelper())
//    }
//}