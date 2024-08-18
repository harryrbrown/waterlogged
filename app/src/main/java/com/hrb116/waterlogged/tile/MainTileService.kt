package com.hrb116.waterlogged.tile

import androidx.wear.protolayout.LayoutElementBuilders
import androidx.wear.protolayout.ResourceBuilders
import androidx.wear.protolayout.StateBuilders
import androidx.wear.protolayout.TimelineBuilders
import androidx.wear.protolayout.expression.AppDataKey
import androidx.wear.protolayout.expression.DynamicBuilders.DynamicFloat
import androidx.wear.protolayout.expression.DynamicDataBuilders
import androidx.wear.tiles.RequestBuilders
import androidx.wear.tiles.TileBuilders
import com.hrb116.waterlogged.R
import com.hrb116.waterlogged.tile.addwater.addWaterLayout
import com.hrb116.waterlogged.tile.addwater.waterLayout
import com.hrb116.waterlogged.tile.login.loginLayout
import com.hrb116.waterlogged.tools.getValue
import com.hrb116.waterlogged.tools.isTokenExpired
import com.hrb116.waterlogged.tools.postWater
import com.hrb116.waterlogged.tools.refreshTokens
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.tiles.SuspendingTileService
import com.google.android.horologist.tiles.images.drawableResToImageResource

private const val RESOURCES_VERSION = "0"

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

        when (requestParams.currentState.lastClickableId) {
            "add_250ml" -> postWater(this, "250")
            "add_500ml" -> postWater(this, "500")
            "add_750ml" -> postWater(this, "750")
        }

        val timeline = TimelineBuilders.Timeline.fromLayoutElement(
            when (requestParams.currentState.lastClickableId) {
                "glass" -> addWaterLayout(this, "glass", "250")
                "bottle" -> addWaterLayout(this, "bottle", "500")
                "large_bottle" -> addWaterLayout(this, "large_bottle", "750")
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
            .build()
    }
}

val previewResources: ResourceBuilders.Resources.Builder.() -> Unit = {
    addIdToImageMapping("glass", drawableResToImageResource(R.drawable.glass_cup_24px))
    addIdToImageMapping("bottle", drawableResToImageResource(R.drawable.water_bottle_24px))
    addIdToImageMapping("large_bottle", drawableResToImageResource(R.drawable.water_bottle_large_24px))
    addIdToImageMapping("keyboard", drawableResToImageResource(R.drawable.keyboard_24px))
}