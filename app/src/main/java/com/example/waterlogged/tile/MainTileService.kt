package com.example.waterlogged.tile

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.wear.protolayout.LayoutElementBuilders
import androidx.wear.protolayout.ModifiersBuilders
import androidx.wear.protolayout.ResourceBuilders
import androidx.wear.protolayout.TimelineBuilders
import androidx.wear.protolayout.material.Button
import androidx.wear.protolayout.material.layouts.MultiButtonLayout
import androidx.wear.protolayout.material.layouts.PrimaryLayout
import androidx.wear.tiles.RequestBuilders
import androidx.wear.tiles.TileBuilders
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
            .addIdToImageMapping("bottle", drawableResToImageResource(R.drawable.tile_preview))
            .addIdToImageMapping("large_bottle", drawableResToImageResource(R.drawable.water_bottle_large_24px))
            .build()
    }

    override suspend fun tileRequest(
        requestParams: RequestBuilders.TileRequest
    ): TileBuilders.Tile {
        val singleTileTimeline = TimelineBuilders.Timeline.Builder().addTimelineEntry(
            TimelineBuilders.TimelineEntry.Builder().setLayout(
                LayoutElementBuilders.Layout.Builder().setRoot(tileLayout(this)).build()
            ).build()
        ).build()

        return TileBuilders.Tile.Builder().setResourcesVersion(RESOURCES_VERSION)
            .setTileTimeline(singleTileTimeline).build()
    }
}

private fun buttonLayout(
    context: Context,
    clickable: ModifiersBuilders.Clickable,
    iconId: String
) = Button.Builder(context, clickable)
    .setContentDescription(iconId)
    .setIconContent(iconId)
//    .apply {
//        setTextContent("HI")
////        setButtonColors(ButtonColors.primaryButtonColors())
//    }
    .build()

private fun tileLayout(context: Context): LayoutElementBuilders.LayoutElement {
    val glassButton = buttonLayout(context, emptyClickable, "glass")
    val bottleButton = buttonLayout(context, emptyClickable, "bottle")
    val largeBottleButton = buttonLayout(context, emptyClickable, "large_bottle")

    return PrimaryLayout.Builder(buildDeviceParameters(context.resources))
        .setResponsiveContentInsetEnabled(true)
        .setContent(
            MultiButtonLayout.Builder()
                .addButtonContent(glassButton)
                .addButtonContent(bottleButton)
                .addButtonContent(largeBottleButton)
                .build()
        ).build()
}

@Preview(
    device = Devices.WEAR_OS_SMALL_ROUND,
    showSystemUi = true,
    backgroundColor = 0xff000000,
    showBackground = true
)
@Composable
fun TilePreview() {
    val previewResources: ResourceBuilders.Resources.Builder.() -> Unit = {
        addIdToImageMapping("glass", drawableResToImageResource(R.drawable.glass_cup_24px))
        addIdToImageMapping("bottle", drawableResToImageResource(R.drawable.water_bottle_24px))
        addIdToImageMapping("large_bottle", drawableResToImageResource(R.drawable.water_bottle_large_24px))
    }

    LayoutRootPreview(root = tileLayout(LocalContext.current), tileResourcesFn = previewResources )
}