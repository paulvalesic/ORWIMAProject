package com.example.mediaappprojectfororwim

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowInsetsControllerCompat
import com.example.mediaappprojectfororwim.ui.theme.MediaAppProjectForORWIMTheme
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

import com.example.mediaappprojectfororwim.Screens.HomeScreen
import com.example.mediaappprojectfororwim.Screens.LoginScreen
import com.example.mediaappprojectfororwim.Screens.RegisterScreen
import com.example.mediaappprojectfororwim.Screens.ProfileScreen
import com.example.mediaappprojectfororwim.Screens.Comments
import com.example.mediaappprojectfororwim.Screens.ChatScreen


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        enableEdgeToEdge()
        setContent {
            MediaAppProjectForORWIMTheme {
                val navController = rememberNavController()

                // NavHost koji definira sve ekrane i rute
                NavHost(navController = navController, startDestination = "login") {
                    composable("login") {
                        LoginScreen(navController)  // LoginScreen
                    }
                    composable("register") {
                        RegisterScreen(navController)  // RegisterScreen
                    }
                    composable("home") {
                        HomeScreen(navController)
                    }
                    composable("profile") {
                        ProfileScreen(navController)
                    }
                    composable("comments") {
                        Comments(navController)
                    }
                    composable("chat_screen/{username}") { backStackEntry ->
                        val username = backStackEntry.arguments?.getString("username") ?: ""
                        ChatScreen(navController, username)
                    }
                }

            }
        }
    }
}


