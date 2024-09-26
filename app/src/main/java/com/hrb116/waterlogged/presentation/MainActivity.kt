package com.hrb116.waterlogged.presentation

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.google.android.horologist.compose.layout.AppScaffold
import com.hrb116.waterlogged.common.tokens.clearTokens
import com.hrb116.waterlogged.presentation.oauth.pkce.AuthPKCEViewModel
import com.hrb116.waterlogged.presentation.oauth.pkce.AuthenticateScreen
import com.hrb116.waterlogged.presentation.menu.ListScreen
import com.hrb116.waterlogged.presentation.menu.presets.EditPresetScreen
import com.hrb116.waterlogged.presentation.menu.presets.PresetsScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            WearApp(pkceViewModel = viewModel())
        }
    }
}

@Composable
fun WearApp(pkceViewModel: AuthPKCEViewModel) {
    val navController = rememberSwipeDismissableNavController()
    val context = LocalContext.current

    AppScaffold {
        val uiState = pkceViewModel.uiState.collectAsState()
        SwipeDismissableNavHost(navController = navController, startDestination = "menu") {
            composable("signin") {
                AuthenticateScreen(
                    uiState.value.statusCode,
                    uiState.value.resultMessage,
                    { (pkceViewModel::startAuthFlow){ signIn(navController) } },
                )
            }
            composable("menu") {
                ListScreen(
                    onSignedOut = { signOut(navController, context) },
                    onPresets = { navController.navigate("presets") },
                    refetchUserData = pkceViewModel::retrieveUserProfile
                )
            }
            composable("presets") {
                PresetsScreen(
                    onSignedOut = { signOut(navController, context) },
                    onEdit = { containerName ->
                        navController.navigate("edit_presets/${containerName.name}")
                    }
                )
            }
            composable("edit_presets/{containerName}") {backStackEntry ->
                val containerName = backStackEntry.arguments?.getString("containerName") ?: "0"
                EditPresetScreen(
                    container = containerName,
                    onSignedOut = { signOut(navController, context) },
                    navController = navController
                )
            }
        }
    }
}

private fun signIn(navController: NavController) {
    navController.navigate("menu") {
        popUpTo(0)
    }
}

private fun signOut(navController: NavController, context: Context) {
    clearTokens(context)
    navController.navigate("signin") {
        popUpTo(0)
    }
}