package com.example.waterlogged.tile.addwater

import android.content.Context
import android.graphics.Color
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
import com.example.waterlogged.tools.emptyClickable
import com.google.android.horologist.compose.tools.buildDeviceParameters

fun addWaterLayout(context: Context, container: String, amount: String): LayoutElementBuilders.LayoutElement {
    val iconName = if (container == "glass") {
        "glass"
    } else if (container == "bottle") {
        "bottle"
    } else {
        "large_bottle"
    }

    return PrimaryLayout.Builder(buildDeviceParameters(context.resources))
        .setResponsiveContentInsetEnabled(true)
        .setPrimaryLabelTextContent(
            Text.Builder(context, "Add water")
                .setTypography(Typography.TYPOGRAPHY_TITLE2)
                .setColor(ColorProp.Builder(Color.WHITE).build())
                .build())
        .setContent(
            Chip.Builder(context, emptyClickable, buildDeviceParameters(context.resources))
                .setPrimaryLabelContent("Add $container")
                .setSecondaryLabelContent("${amount}ml")
                .setIconContent(iconName)
                .setWidth(expand())
                .build())
        .setPrimaryChipContent(
            CompactChip.Builder(
                context,
                "Back",
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