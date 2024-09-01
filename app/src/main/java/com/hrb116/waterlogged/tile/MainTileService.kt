package com.hrb116.waterlogged.tile

import androidx.wear.protolayout.ResourceBuilders
import androidx.wear.protolayout.TimelineBuilders
import androidx.wear.tiles.RequestBuilders
import androidx.wear.tiles.TileBuilders
import com.hrb116.waterlogged.R
import com.hrb116.waterlogged.tile.addwater.addWaterLayout
import com.hrb116.waterlogged.tile.addwater.waterLayout
import com.hrb116.waterlogged.tile.login.loginLayout
import com.hrb116.waterlogged.tools.tokens.getValue
import com.hrb116.waterlogged.tools.tokens.isTokenExpired
import com.hrb116.waterlogged.tools.postWater
import com.hrb116.waterlogged.tools.tokens.refreshTokens
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.tiles.SuspendingTileService
import com.google.android.horologist.tiles.images.drawableResToImageResource
import com.hrb116.waterlogged.tools.WaterContainers
import com.hrb116.waterlogged.tools.tokens.Tokens

private const val RESOURCES_VERSION = "0"

@OptIn(ExperimentalHorologistApi::class)
class MainTileService : SuspendingTileService() {
    override suspend fun resourcesRequest(
        requestParams: RequestBuilders.ResourcesRequest
    ): ResourceBuilders.Resources {
        return ResourceBuilders.Resources.Builder()
            .setVersion(RESOURCES_VERSION)
            .addIdToImageMapping(WaterContainers.GLASS.container, drawableResToImageResource(R.drawable.glass_cup_24px))
            .addIdToImageMapping(WaterContainers.BOTTLE.container, drawableResToImageResource(R.drawable.water_bottle_24px))
            .addIdToImageMapping(WaterContainers.LARGE_BOTTLE.container, drawableResToImageResource(R.drawable.water_bottle_large_24px))
            .addIdToImageMapping("keyboard", drawableResToImageResource(R.drawable.keyboard_24px))
            .build()
    }

    override suspend fun tileRequest(
        requestParams: RequestBuilders.TileRequest
    ): TileBuilders.Tile {
        val isAuthenticated = getValue(this, Tokens.ACCESS_TOKEN) != null
        if (isTokenExpired(this)) {
            refreshTokens(this)
        }

        when (requestParams.currentState.lastClickableId) {
            "add_glass" -> postWater(this, WaterContainers.GLASS)
            "add_bottle" -> postWater(this, WaterContainers.BOTTLE)
            "add_large_bottle" -> postWater(this, WaterContainers.LARGE_BOTTLE)
        }

        val timeline = TimelineBuilders.Timeline.fromLayoutElement(
            when (requestParams.currentState.lastClickableId) {
                "glass" -> addWaterLayout(this, WaterContainers.GLASS)
                "bottle" -> addWaterLayout(this, WaterContainers.BOTTLE)
                "large_bottle" -> addWaterLayout(this, WaterContainers.LARGE_BOTTLE)
                else -> {
                    if (isAuthenticated && !isTokenExpired(this)) {
                        waterLayout(this, requestParams.currentState.lastClickableId == "back")
                    } else {
                        loginLayout(this)
                    }
                }
            }
        )

        return TileBuilders.Tile.Builder().setResourcesVersion(RESOURCES_VERSION)
            .setTileTimeline(timeline)
            .setFreshnessIntervalMillis(10 * 60 * 1000) // refresh every 10 mins
            .build()
    }
}

val previewResources: ResourceBuilders.Resources.Builder.() -> Unit = {
    addIdToImageMapping(WaterContainers.GLASS.container, drawableResToImageResource(R.drawable.glass_cup_24px))
    addIdToImageMapping(WaterContainers.BOTTLE.container, drawableResToImageResource(R.drawable.water_bottle_24px))
    addIdToImageMapping(WaterContainers.LARGE_BOTTLE.container, drawableResToImageResource(R.drawable.water_bottle_large_24px))
    addIdToImageMapping("keyboard", drawableResToImageResource(R.drawable.keyboard_24px))
}
