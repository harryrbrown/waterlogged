package com.example.waterlogged.tile

import android.content.Context
import android.graphics.Color
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.wear.protolayout.ActionBuilders
import androidx.wear.protolayout.ColorBuilders.ColorProp
import androidx.wear.protolayout.DimensionBuilders.expand
import androidx.wear.protolayout.LayoutElementBuilders
import androidx.wear.protolayout.ModifiersBuilders.Clickable
import androidx.wear.protolayout.ResourceBuilders
import androidx.wear.protolayout.StateBuilders
import androidx.wear.protolayout.TimelineBuilders
import androidx.wear.protolayout.material.Button
import androidx.wear.protolayout.material.Chip
import androidx.wear.protolayout.material.CircularProgressIndicator
import androidx.wear.protolayout.material.CompactChip
import androidx.wear.protolayout.material.Text
import androidx.wear.protolayout.material.Typography
import androidx.wear.protolayout.material.layouts.EdgeContentLayout
import androidx.wear.protolayout.material.layouts.MultiButtonLayout
import androidx.wear.protolayout.material.layouts.PrimaryLayout
import androidx.wear.tiles.RequestBuilders
import androidx.wear.tiles.TileBuilders
import androidx.wear.tooling.preview.devices.WearDevices
import com.example.waterlogged.R
import com.example.waterlogged.tile.addwater.addWaterLayout
import com.example.waterlogged.tile.addwater.waterLayout
import com.example.waterlogged.tile.login.loginLayout
import com.example.waterlogged.tools.emptyClickable
import com.example.waterlogged.tools.getValue
import com.example.waterlogged.tools.isTokenExpired
import com.example.waterlogged.tools.refreshTokens
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
        val isAuthenticated = getValue(this, "access_token") != null
        if (isTokenExpired(this)) {
            refreshTokens(this)
        }

        Log.d("TAG", getValue(this, "access_token") ?: "test")

        val root: LayoutElementBuilders.LayoutElement = if (isAuthenticated && !isTokenExpired(this)) {
            waterLayout(this)
        } else {
            loginLayout(this)
        }

        val timeline = TimelineBuilders.Timeline.fromLayoutElement(
            when (requestParams.currentState.lastClickableId) {
                "glass" -> addWaterLayout(this, "glass", "250")
                "bottle" -> addWaterLayout(this, "bottle", "500")
                "large_bottle" -> addWaterLayout(this, "large_bottle", "750")
                else -> root
            }
        )

        return TileBuilders.Tile.Builder().setResourcesVersion(RESOURCES_VERSION)
            .setTileTimeline(timeline).build()
    }
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

@Preview(device = WearDevices.SMALL_ROUND)
@Preview(device = WearDevices.LARGE_ROUND)
@Composable
fun AddWaterTilePreview() =
    LayoutRootPreview(root = addWaterLayout(LocalContext.current, "glass", "250"), tileResourcesFn = previewResources)