package com.jnetaol.findai

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.jnetaol.findai.ui.screens.detail.FileDetailScreen
import com.jnetaol.findai.ui.screens.home.HomeScreen
import com.jnetaol.findai.ui.screens.index.IndexScreen
import com.jnetaol.findai.ui.screens.results.ResultsScreen
import com.jnetaol.findai.ui.screens.settings.SettingsScreen

@Composable
fun FindAINavHost() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeScreen(navController)
        }
        composable("results/{query}") { backStackEntry ->
            val query = backStackEntry.arguments?.getString("query") ?: ""
            ResultsScreen(navController, query)
        }
        composable("detail/{fileId}", arguments = listOf(
            navArgument("fileId") { type = NavType.LongType }
        )) { backStackEntry ->
            val fileId = backStackEntry.arguments?.getLong("fileId") ?: 0L
            FileDetailScreen(navController, fileId)
        }
        composable("index") {
            IndexScreen(navController)
        }
        composable("settings") {
            SettingsScreen(navController)
        }
    }
}
