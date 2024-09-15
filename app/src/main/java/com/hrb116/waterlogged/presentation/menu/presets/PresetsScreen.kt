package com.hrb116.waterlogged.presentation.menu.presets

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
import com.hrb116.waterlogged.common.WaterContainers
import com.hrb116.waterlogged.common.preferences.getLocalisedWaterVolume
import com.hrb116.waterlogged.common.tokens.Tokens
import com.hrb116.waterlogged.common.tokens.getValue

@OptIn(ExperimentalHorologistApi::class)
@Composable
fun PresetsScreen(
    onSignedOut: () -> Unit,
    onEdit: () -> Unit
) {
    val context = LocalContext.current
    val accessToken = getValue(context, Tokens.ACCESS_TOKEN)
    val isAuthenticated = accessToken != null

    if (!isAuthenticated) {
        onSignedOut()
    }

    val columnState = rememberResponsiveColumnState(
        contentPadding = ScalingLazyColumnDefaults.padding(
            first = ScalingLazyColumnDefaults.ItemType.Text,
            last = ScalingLazyColumnDefaults.ItemType.Chip
        )
    )

    val smallAmount = getLocalisedWaterVolume(context, WaterContainers.GLASS);
    val mediumAmount = getLocalisedWaterVolume(context, WaterContainers.BOTTLE);
    val largeAmount = getLocalisedWaterVolume(context, WaterContainers.LARGE_BOTTLE);

    ScreenScaffold(scrollState = columnState, modifier = Modifier.background(Color.Black)) {
        ScalingLazyColumn(columnState = columnState) {
            item {
                ListHeader {
                    Text(
                        stringResource(R.string.edit_presets),
                        textAlign = TextAlign.Center
                    )
                }
            }
            item {
                Chip(
                    onClick = { onEdit() },
                    label = { Text(text = stringResource(R.string.small)) },
                    secondaryLabel = { Text(text = smallAmount)},
                    icon = {
                        Icon(painter = painterResource(id = R.drawable.glass_cup_24px), contentDescription = "Small water container")
                    },
                    colors = ChipDefaults.secondaryChipColors()
                )
            }
            item {
                Chip(
                    onClick = {},
                    label = { Text(text = stringResource(R.string.medium)) },
                    secondaryLabel = { Text(text = mediumAmount)},
                    icon = {
                        Icon(painter = painterResource(id = R.drawable.water_bottle_24px), contentDescription = "Medium water container")
                    },
                    colors = ChipDefaults.secondaryChipColors()
                )
            }
            item {
                Chip(
                    onClick = {},
                    label = { Text(text = stringResource(R.string.large)) },
                    secondaryLabel = { Text(text = largeAmount)},
                    icon = {
                        Icon(painter = painterResource(id = R.drawable.water_bottle_large_24px), contentDescription = "Large water container")
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
fun PresetsScreenPreview() {
    PresetsScreen({}, {})
}