package com.example.surakshasetu.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Signup : Screen("signup")
    object Home : Screen("home")
    object Map : Screen("map")
    object Alerts : Screen("alerts")
    object Community : Screen("community")
    object Profile : Screen("profile")
    object CircleOfTrust : Screen("circle_of_trust")
    object FakeCall : Screen("fake_call")
}
