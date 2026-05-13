package com.example.surakshasetu

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.surakshasetu.navigation.Screen
import com.example.surakshasetu.ui.auth.LoginScreen
import com.example.surakshasetu.ui.auth.SignupScreen
import com.example.surakshasetu.ui.circle.CircleOfTrustScreen
import com.example.surakshasetu.ui.fakecall.FakeCallScreen
import com.example.surakshasetu.ui.fakecall.IncomingCallScreen
import com.example.surakshasetu.ui.home.HomeScreen
import com.example.surakshasetu.ui.map.MapScreen
import com.example.surakshasetu.ui.profile.ProfileScreen
import com.example.surakshasetu.ui.theme.SurakshaSetuTheme
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        val startDestination = if (auth.currentUser != null) {
            Screen.Home.route
        } else {
            Screen.Login.route
        }

        setContent {
            SurakshaSetuTheme {
                val navController = rememberNavController()
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = startDestination,
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable(Screen.Login.route) {
                            LoginScreen(navController)
                        }
                        composable(Screen.Signup.route) {
                            SignupScreen(navController)
                        }
                        composable(Screen.Home.route) {
                            HomeScreen(navController)
                        }
                        composable(Screen.Map.route) {
                            MapScreen(navController)
                        }
                        composable(Screen.CircleOfTrust.route) {
                            CircleOfTrustScreen(navController)
                        }
                        composable(Screen.Profile.route) {
                            ProfileScreen(navController)
                        }
                        composable(Screen.FakeCall.route) {
                            FakeCallScreen(navController)
                        }
                        composable(
                            route = "incoming_call/{name}/{number}",
                            arguments = listOf(
                                navArgument("name") { type = NavType.StringType },
                                navArgument("number") { type = NavType.StringType }
                            )
                        ) { backStackEntry ->
                            val name = backStackEntry.arguments?.getString("name") ?: "Unknown"
                            val number = backStackEntry.arguments?.getString("number") ?: "Unknown"
                            IncomingCallScreen(navController, name, number)
                        }
                    }
                }
            }
        }
    }
}
