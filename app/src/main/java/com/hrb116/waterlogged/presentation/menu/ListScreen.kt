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
import com.hrb116.waterlogged.common.preferences.getUserName
import com.hrb116.waterlogged.common.tokens.Tokens
import com.hrb116.waterlogged.common.tokens.getValue
import com.hrb116.waterlogged.common.tokens.isTokenExpired
import com.hrb116.waterlogged.common.tokens.refreshTokens
import kotlinx.coroutines.runBlocking

@OptIn(ExperimentalHorologistApi::class)
@Composable
fun ListScreen(
    onSignedOut: () -> Unit,
    onPresets: () -> Unit,
    refetchUserData: suspend (String) -> Result<String>
) {
    val context = LocalContext.current
    val accessToken = getValue(context, Tokens.ACCESS_TOKEN)
    val isAuthenticated = accessToken != null

    if (!isAuthenticated) {
        onSignedOut()
    }

    val name = getUserName(context) ?: ""

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
                    text = stringResource(R.string.signed_in_as) + " $name",
                    textAlign = TextAlign.Center
                )
            }
            item {
                Chip(
                    onClick = { onPresets() },
                    label = { Text(text = stringResource(R.string.edit_presets)) },
                    icon = {
                        Icon(painter = painterResource(id = R.drawable.edit_24px), contentDescription = "Refetch user data")
                    },
                    colors = ChipDefaults.secondaryChipColors()
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
                    label = { Text(text = stringResource(R.string.refetch_user_data)) },
                    icon = {
                        Icon(painter = painterResource(id = R.drawable.sync_24px), contentDescription = "Refetch user data")
                    },
                    colors = ChipDefaults.secondaryChipColors()
                )
            }
            item {
                Chip(
                    onClick = { onSignedOut() },
                    label = { Text(text = stringResource(R.string.sign_out)) },
                    icon = {
                        Icon(painter = painterResource(id = R.drawable.send_to_mobile_24px), contentDescription = "Sign out")
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

    ListScreen({}, {}, simpleFunction)
}