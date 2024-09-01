package com.hrb116.waterlogged.presentation.menu

import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.ListHeader
import androidx.wear.compose.material.Text
import androidx.wear.compose.ui.tooling.preview.WearPreviewDevices
import androidx.wear.compose.ui.tooling.preview.WearPreviewFontScales
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.layout.ScalingLazyColumn
import com.google.android.horologist.compose.layout.ScalingLazyColumnDefaults
import com.google.android.horologist.compose.layout.ScreenScaffold
import com.google.android.horologist.compose.layout.rememberResponsiveColumnState
import com.hrb116.waterlogged.R
import com.hrb116.waterlogged.tools.getValue
import com.hrb116.waterlogged.tools.isTokenExpired
import com.hrb116.waterlogged.tools.refreshTokens
import kotlinx.coroutines.runBlocking
import kotlin.reflect.KSuspendFunction1

@OptIn(ExperimentalHorologistApi::class)
@Composable
fun ListScreen(
    onSignedOut: () -> Unit,
    refetchUserData: suspend (String) -> Result<String>
) {
    val context = LocalContext.current
    val accessToken = getValue(context, "access_token")
    val isAuthenticated = accessToken != null

    if (!isAuthenticated) {
        onSignedOut()
    }

    if (isTokenExpired(context)) {
        runBlocking { refreshTokens(context) }
    }

    val columnState = rememberResponsiveColumnState(
        contentPadding = ScalingLazyColumnDefaults.padding(
            first = ScalingLazyColumnDefaults.ItemType.Text,
            last = ScalingLazyColumnDefaults.ItemType.Chip
        )
    )

    ScreenScaffold(scrollState = columnState, modifier = Modifier.background(Color.Black)) {
        ScalingLazyColumn(columnState = columnState) {
            item {
                ListHeader {
                    Text(
                        stringResource(R.string.waterlogged),
                        textAlign = TextAlign.Center
                    )
                }
            }
            item {
                Text(
                    text = "Signed in as Harry B.",
                    textAlign = TextAlign.Center
                )
            }
            item {
                Chip(
                    onClick = {
                        runBlocking {
                            if (accessToken != null) {
                                refetchUserData(accessToken)
                            }
                        }
                    },
                    label = {
                        Text(
                            text = "Refetch user data"
                        )
                    },
                    icon = {
                        Icon(painter = painterResource(id = R.drawable.sync_24px), contentDescription = "Send to Phone")
                    },
                    colors = ChipDefaults.secondaryChipColors()
                )
            }
            item {
                Chip(
                    onClick = { onSignedOut() },
                    label = {
                        Text(
                            text = "Sign out"
                        )
                    },
                    icon = {
                        Icon(painter = painterResource(id = R.drawable.send_to_mobile_24px), contentDescription = "Send to Phone")
                    },
                    colors = ChipDefaults.secondaryChipColors()
                )
            }
        }
    }
}

@WearPreviewDevices
@WearPreviewFontScales
@Composable
fun ListScreenPreview() {
    val simpleFunction: suspend (String) -> Result<String> = { input ->
        if (input.isNotEmpty()) {
            Result.success("Success with input: $input")
        } else {
            Result.failure(IllegalArgumentException("Input cannot be empty"))
        }
    }

    ListScreen({}, simpleFunction)
}