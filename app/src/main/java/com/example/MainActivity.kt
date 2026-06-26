package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.screens.AddConfigScreen
import com.example.ui.screens.ConfigManagementScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.VpnViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: VpnViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                VpnApp(viewModel)
            }
        }
    }
}

@Composable
fun VpnApp(viewModel: VpnViewModel) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "splash") {
        composable("splash") {
            SplashScreen(
                onNavigateToDashboard = {
                    navController.navigate("dashboard") {
                        popUpTo("splash") { inclusive = true }
                    }
                }
            )
        }
        composable("dashboard") {
            DashboardScreen(
                viewModel = viewModel,
                onNavigateToSettings = { navController.navigate("settings") },
                onNavigateToConfigs = { navController.navigate("configs") }
            )
        }
        composable("settings") {
            SettingsScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
        composable("configs") {
            ConfigManagementScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onNavigateToAdd = { navController.navigate("add_config") },
                onNavigateToEdit = { configId -> navController.navigate("edit_config/$configId") }
            )
        }
        composable("add_config") {
            AddConfigScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
        composable("edit_config/{configId}") { backStackEntry ->
            val configId = backStackEntry.arguments?.getString("configId")?.toIntOrNull()
            val configToEdit = viewModel.allConfigs.value.find { it.id == configId }
            AddConfigScreen(
                viewModel = viewModel,
                configToEdit = configToEdit,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
