package com.hrb116.waterlogged.tile.addwater

import android.content.Context
import android.graphics.Color
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.wear.protolayout.ActionBuilders
import androidx.wear.protolayout.ColorBuilders.ColorProp
import androidx.wear.protolayout.DimensionBuilders.expand
import androidx.wear.protolayout.LayoutElementBuilders
import androidx.wear.protolayout.ModifiersBuilders.Clickable
import androidx.wear.protolayout.StateBuilders
import androidx.wear.protolayout.material.Chip
import androidx.wear.protolayout.material.CompactChip
import androidx.wear.protolayout.material.Text
import androidx.wear.protolayout.material.Typography
import androidx.wear.protolayout.material.layouts.PrimaryLayout
import androidx.wear.tooling.preview.devices.WearDevices
import com.hrb116.waterlogged.R
import com.hrb116.waterlogged.tile.previewResources
import com.google.android.horologist.compose.tools.LayoutRootPreview
import com.google.android.horologist.compose.tools.buildDeviceParameters
import com.hrb116.waterlogged.common.WaterContainers
import com.hrb116.waterlogged.common.getLocalisedWaterVolume

fun addWaterLayout(context: Context, container: WaterContainers): LayoutElementBuilders.LayoutElement {
    val containerName = when (container) {
        WaterContainers.GLASS -> "glass"
        WaterContainers.BOTTLE -> "bottle"
        else -> "large bottle"
    }

    val primaryLabelText = Text.Builder(context, context.getString(R.string.add_water))
        .setTypography(Typography.TYPOGRAPHY_TITLE2)
        .setColor(ColorProp.Builder(Color.WHITE).build())
        .build()

    val contentChipSecondaryLabelText = getLocalisedWaterVolume(context, container)

    val contentChip = Chip.Builder(
        context,
        Clickable.Builder()
            .setId("add_${container}")
            .setOnClick(ActionBuilders.LoadAction.Builder().build())
            .build(),
        buildDeviceParameters(context.resources))
            .setPrimaryLabelContent("${context.getString(R.string.add)} $containerName")
            .setSecondaryLabelContent(contentChipSecondaryLabelText)
            .setIconContent(container.container)
            .setWidth(expand())
            .build()

    val primaryChip = CompactChip.Builder(
        context,
        context.getString(R.string.back),
        Clickable.Builder()
            .setId("back")
            .setOnClick(
                ActionBuilders.LoadAction.Builder()
                    .setRequestState(
                        StateBuilders.State.Builder()
                            .build()
                    ).build())
            .build(),
        buildDeviceParameters(context.resources)
    ).build()

    return PrimaryLayout.Builder(buildDeviceParameters(context.resources))
        .setResponsiveContentInsetEnabled(true)
        .setPrimaryLabelTextContent(primaryLabelText)
        .setContent(contentChip)
        .setPrimaryChipContent(primaryChip)
        .build()
}

@Preview(device = WearDevices.SMALL_ROUND)
@Preview(device = WearDevices.LARGE_ROUND)
@Composable
fun AddWaterTilePreview() =
    LayoutRootPreview(root = addWaterLayout(LocalContext.current, WaterContainers.LARGE_BOTTLE), tileResourcesFn = previewResources)
