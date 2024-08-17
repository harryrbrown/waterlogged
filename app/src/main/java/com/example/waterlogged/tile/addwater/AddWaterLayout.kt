package com.example.waterlogged.tile.addwater

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
import com.example.waterlogged.R
import com.example.waterlogged.tile.previewResources
import com.example.waterlogged.tools.emptyClickable
import com.google.android.horologist.compose.tools.LayoutRootPreview
import com.google.android.horologist.compose.tools.buildDeviceParameters

fun addWaterLayout(context: Context, container: String, amount: String): LayoutElementBuilders.LayoutElement {
    val containerName = when (container) {
        "glass" -> "glass"
        "bottle" -> "bottle"
        else -> "large bottle"
    }

    return PrimaryLayout.Builder(buildDeviceParameters(context.resources))
        .setResponsiveContentInsetEnabled(true)
        .setPrimaryLabelTextContent(
            Text.Builder(context, context.getString(R.string.add_water))
                .setTypography(Typography.TYPOGRAPHY_TITLE2)
                .setColor(ColorProp.Builder(Color.WHITE).build())
                .build())
        .setContent(
            Chip.Builder(context, emptyClickable, buildDeviceParameters(context.resources))
                .setPrimaryLabelContent("${context.getString(R.string.add)} $containerName")
                .setSecondaryLabelContent("${amount}ml")
                .setIconContent(container)
                .setWidth(expand())
                .build())
        .setPrimaryChipContent(
            CompactChip.Builder(
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
        )
        .build()
}

@Preview(device = WearDevices.SMALL_ROUND)
@Preview(device = WearDevices.LARGE_ROUND)
@Composable
fun AddWaterTilePreview() =
    LayoutRootPreview(root = addWaterLayout(LocalContext.current, "large_bottle", "750"), tileResourcesFn = previewResources)
