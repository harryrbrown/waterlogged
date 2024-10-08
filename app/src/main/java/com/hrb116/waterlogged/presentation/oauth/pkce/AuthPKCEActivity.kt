package com.hrb116.waterlogged.presentation.oauth.pkce

// Based on the samples in https://github.com/android/wear-os-samples/tree/main/WearOAuth

import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import com.hrb116.waterlogged.R
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.layout.ScalingLazyColumn
import com.google.android.horologist.compose.layout.ScalingLazyColumnDefaults
import com.google.android.horologist.compose.layout.ScreenScaffold
import com.google.android.horologist.compose.layout.rememberResponsiveColumnState

@OptIn(ExperimentalHorologistApi::class)
@Composable
fun AuthenticateScreen(
    statusCode: Int,
    resultMessage: String,
    startAuthFlow: () -> Unit
) {
    val columnState = rememberResponsiveColumnState(
        contentPadding = ScalingLazyColumnDefaults.padding(
            first = ScalingLazyColumnDefaults.ItemType.Text,
            last = ScalingLazyColumnDefaults.ItemType.Text
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
                    text = stringResource(R.string.sign_in_fitbit),
                    textAlign = TextAlign.Center
                )
            }
            item {
                Chip(
                    onClick = { startAuthFlow() },
                    label = {
                        Text(
                            text = stringResource(R.string.sign_in_on_phone)
                        )
                    },
                    icon = {
                        Icon(painter = painterResource(id = R.drawable.send_to_mobile_24px), contentDescription = "Send to Phone")
                    },
                    colors = ChipDefaults.secondaryChipColors()
                )
            }
            item { Text(stringResource(id = statusCode)) }
            item { Text(resultMessage) }
        }
    }
}

@WearPreviewDevices
@WearPreviewFontScales
@Composable
fun AuthenticateScreenPreview() {
    AuthenticateScreen(
        statusCode = R.string.status_retrieved,
        resultMessage = "Bobby Bonson",
        startAuthFlow = {}
    )
}

@WearPreviewDevices
@WearPreviewFontScales
@Composable
fun AuthenticateScreenFailedPreview() {
    AuthenticateScreen(
        statusCode = R.string.status_failed,
        resultMessage = "",
        startAuthFlow = {}
    )
}
