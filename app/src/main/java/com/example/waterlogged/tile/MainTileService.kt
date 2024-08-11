package com.example.waterlogged.tile

import android.content.Context
import android.graphics.Color
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.wear.protolayout.ColorBuilders.ColorProp
import androidx.wear.protolayout.DimensionBuilders
import androidx.wear.protolayout.DimensionBuilders.DpProp
import androidx.wear.protolayout.DimensionBuilders.SpacerDimension
import androidx.wear.protolayout.LayoutElementBuilders
import androidx.wear.protolayout.LayoutElementBuilders.Column
import androidx.wear.protolayout.LayoutElementBuilders.Spacer
import androidx.wear.protolayout.ModifiersBuilders
import androidx.wear.protolayout.ResourceBuilders
import androidx.wear.protolayout.TimelineBuilders
import androidx.wear.protolayout.material.Button
import androidx.wear.protolayout.material.Chip
import androidx.wear.protolayout.material.CircularProgressIndicator
import androidx.wear.protolayout.material.CompactChip
import androidx.wear.protolayout.material.Text
import androidx.wear.protolayout.material.TitleChip
import androidx.wear.protolayout.material.Typography
import androidx.wear.protolayout.material.layouts.EdgeContentLayout
import androidx.wear.protolayout.material.layouts.MultiButtonLayout
import androidx.wear.protolayout.material.layouts.MultiSlotLayout
import androidx.wear.protolayout.material.layouts.PrimaryLayout
import androidx.wear.tiles.RequestBuilders
import androidx.wear.tiles.TileBuilders
import androidx.wear.tooling.preview.devices.WearDevices
import com.example.waterlogged.R
import com.example.waterlogged.tools.emptyClickable
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.tools.LayoutRootPreview
import com.google.android.horologist.compose.tools.buildDeviceParameters
import com.google.android.horologist.tiles.SuspendingTileService
import com.google.android.horologist.tiles.images.drawableResToImageResource

private const val RESOURCES_VERSION = "0"

/**
 * Skeleton for a tile with no images.
 */
@OptIn(ExperimentalHorologistApi::class)
class MainTileService : SuspendingTileService() {

    override suspend fun resourcesRequest(
        requestParams: RequestBuilders.ResourcesRequest
    ): ResourceBuilders.Resources {
        return ResourceBuilders.Resources.Builder()
            .setVersion(RESOURCES_VERSION)
            .addIdToImageMapping("glass", drawableResToImageResource(R.drawable.glass_cup_24px))
            .addIdToImageMapping("bottle", drawableResToImageResource(R.drawable.water_bottle_24px))
            .addIdToImageMapping("large_bottle", drawableResToImageResource(R.drawable.water_bottle_large_24px))
            .addIdToImageMapping("keyboard", drawableResToImageResource(R.drawable.keyboard_24px))
            .build()
    }

    override suspend fun tileRequest(
        requestParams: RequestBuilders.TileRequest
    ): TileBuilders.Tile {
        val root: LayoutElementBuilders.LayoutElement

        if (false) {
            root = waterLayout(this)
        } else {
            root = loginLayout(this)
        }

        val singleTileTimeline = TimelineBuilders.Timeline.Builder().addTimelineEntry(
            TimelineBuilders.TimelineEntry.Builder().setLayout(
                LayoutElementBuilders.Layout.Builder().setRoot(root).build()
            ).build()
        ).build()

        return TileBuilders.Tile.Builder().setResourcesVersion(RESOURCES_VERSION)
            .setTileTimeline(singleTileTimeline).build()
    }
}

private fun buttonLayout(context: Context, clickable: ModifiersBuilders.Clickable, iconId: String) =
    Button.Builder(context, clickable)
        .setContentDescription(iconId)
        .setIconContent(iconId)
        .build()

private fun buttonsLayout(context: Context): MultiButtonLayout {
    val glassButton = buttonLayout(context, emptyClickable, "glass")
    val bottleButton = buttonLayout(context, emptyClickable, "bottle")
    val largeBottleButton = buttonLayout(context, emptyClickable, "large_bottle")
    val keyboardButton = buttonLayout(context, emptyClickable, "keyboard")

    return MultiButtonLayout.Builder()
        .addButtonContent(glassButton)
        .addButtonContent(bottleButton)
        .addButtonContent(largeBottleButton)
        .addButtonContent(keyboardButton)
        .build()
}

private fun circularProgressLayout(): CircularProgressIndicator {
    return CircularProgressIndicator.Builder()
        .setStartAngle(30.0f)
        .setEndAngle(330.0f)
        .setStrokeWidth(5.0f)
        .setProgress(0.5f)
        .build()
}

private fun waterLayout(context: Context): LayoutElementBuilders.LayoutElement {
    return EdgeContentLayout.Builder(buildDeviceParameters(context.resources))
        .setResponsiveContentInsetEnabled(true)
        .setContent(buttonsLayout(context))
        .setEdgeContent(circularProgressLayout())
        .setEdgeContentThickness(5.0f)
        .build()
}

private fun loginColumnLayout(context: Context): Column {
    return Column.Builder()
        .setWidth(DimensionBuilders.expand())
        .addContent(
            Text.Builder(context, "Sign in")
                .setTypography(Typography.TYPOGRAPHY_TITLE1)
                .setColor(ColorProp.Builder(Color.WHITE).build())
                .build()
        )
        .addContent(Spacer.Builder().setHeight(DpProp.Builder(10.0f).build()).build())
        .addContent(
            Text.Builder(context, "Allow Waterlogged to access your Fitbit water data.")
                .setTypography(Typography.TYPOGRAPHY_BODY1)
                .setMaxLines(3)
                .setColor(ColorProp.Builder(Color.WHITE).build())
                .build()
        )
        .addContent(Spacer.Builder().setHeight(DpProp.Builder(10.0f).build()).build())
        .addContent(
            Chip.Builder(context, emptyClickable, buildDeviceParameters(context.resources))
                .setPrimaryLabelContent("Sign in")
                .setWidth(DimensionBuilders.expand())
                .build()
        ).build()
}

private fun loginLayout(context: Context): LayoutElementBuilders.LayoutElement {
    return PrimaryLayout.Builder(buildDeviceParameters(context.resources))
        .setResponsiveContentInsetEnabled(true)
        .setContent(loginColumnLayout(context))
        .build()
}

val previewResources: ResourceBuilders.Resources.Builder.() -> Unit = {
    addIdToImageMapping("glass", drawableResToImageResource(R.drawable.glass_cup_24px))
    addIdToImageMapping("bottle", drawableResToImageResource(R.drawable.water_bottle_24px))
    addIdToImageMapping("large_bottle", drawableResToImageResource(R.drawable.water_bottle_large_24px))
    addIdToImageMapping("keyboard", drawableResToImageResource(R.drawable.keyboard_24px))
}

@Preview(device = WearDevices.SMALL_ROUND)
@Preview(device = WearDevices.LARGE_ROUND)
@Composable
fun TilePreview() =
    LayoutRootPreview(root = waterLayout(LocalContext.current), tileResourcesFn = previewResources)


@Preview(device = WearDevices.SMALL_ROUND)
@Preview(device = WearDevices.LARGE_ROUND)
@Composable
fun LoginTilePreview() =
    LayoutRootPreview(root = loginLayout(LocalContext.current), tileResourcesFn = previewResources)
