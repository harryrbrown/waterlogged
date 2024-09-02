package com.hrb116.waterlogged.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.google.android.horologist.compose.layout.AppScaffold
import com.hrb116.waterlogged.common.tokens.clearTokens
import com.hrb116.waterlogged.presentation.oauth.pkce.AuthPKCEViewModel
import com.hrb116.waterlogged.presentation.oauth.pkce.AuthenticateScreen
import com.hrb116.waterlogged.presentation.menu.ListScreen

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
                    { (pkceViewModel::startAuthFlow){ navController.navigate("menu") } },
                )
            }
            composable("menu") {
                ListScreen(
                    onSignedOut = { clearTokens(context); navController.navigate("signin") },
                    refetchUserData = pkceViewModel::retrieveUserProfile
                )
            }
        }
    }
}