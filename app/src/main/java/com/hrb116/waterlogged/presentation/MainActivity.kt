package com.hrb116.waterlogged.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.google.android.horologist.compose.layout.AppScaffold
import com.hrb116.waterlogged.presentation.menu.ListScreen
import com.hrb116.waterlogged.presentation.menu.presets.EditPresetScreen
import com.hrb116.waterlogged.presentation.menu.presets.PresetsScreen

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val route = intent.getStringExtra("route") ?: "menu"
        
        setContent {
            WearApp(startDestination = route)
        }
    }
}

@Composable
fun WearApp(startDestination: String = "menu") {
    val navController = rememberSwipeDismissableNavController()
    val context = LocalContext.current

    androidx.compose.runtime.LaunchedEffect(Unit) {
        com.hrb116.waterlogged.common.networking.requestSync(context)
    }

    AppScaffold {
        SwipeDismissableNavHost(navController = navController, startDestination = startDestination) {
            composable("menu") {
                ListScreen(
                    onPresets = { navController.navigate("presets") }
                )
            }
            composable("update") {
                com.hrb116.waterlogged.presentation.update.UpdateScreen()
            }
            composable("presets") {
                PresetsScreen(
                    onEdit = { containerName ->
                        navController.navigate("edit_presets/$containerName")
                    }
                )
            }
            composable("edit_presets/{containerName}") { backStackEntry ->
                val containerName = backStackEntry.arguments?.getString("containerName") ?: "0"
                EditPresetScreen(
                    container = containerName,
                    navController = navController
                )
            }
        }
    }
}