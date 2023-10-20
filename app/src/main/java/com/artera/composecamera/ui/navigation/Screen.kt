package com.artera.composecamera.ui.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Camera : Screen("camera")
    object Location : Screen("location")
    object Payment : Screen("payment")
}