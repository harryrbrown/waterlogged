package com.hrb116.waterlogged.tile.addwater

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.wear.protolayout.ActionBuilders
import androidx.wear.protolayout.ColorBuilders
import androidx.wear.protolayout.LayoutElementBuilders
import androidx.wear.protolayout.LayoutElementBuilders.Column
import androidx.wear.protolayout.ModifiersBuilders.Clickable
import androidx.wear.protolayout.StateBuilders
import androidx.wear.protolayout.TypeBuilders.FloatProp
import androidx.wear.protolayout.expression.AnimationParameterBuilders.AnimationParameters
import androidx.wear.protolayout.expression.AnimationParameterBuilders.AnimationSpec
import androidx.wear.protolayout.expression.DynamicBuilders.DynamicFloat
import androidx.wear.protolayout.material.Button
import androidx.wear.protolayout.material.CircularProgressIndicator
import androidx.wear.protolayout.material.ProgressIndicatorColors
import androidx.wear.protolayout.material.layouts.EdgeContentLayout
import androidx.wear.protolayout.material.layouts.MultiButtonLayout
import androidx.wear.tooling.preview.devices.WearDevices
import com.hrb116.waterlogged.tile.previewResources
import com.hrb116.waterlogged.common.preferences.WaterLog
import com.hrb116.waterlogged.common.networking.getWater
import com.hrb116.waterlogged.common.preferences.getWaterFromCache
import com.google.android.horologist.compose.tools.LayoutRootPreview
import com.google.android.horologist.compose.tools.buildDeviceParameters
import com.hrb116.waterlogged.common.WaterContainers
import kotlinx.coroutines.*

private fun buttonLayout(context: Context, iconId: String) =
    Button.Builder(context, Clickable.Builder()
        .setId(iconId)
        .setOnClick(
            ActionBuilders.LoadAction.Builder()
                .setRequestState(
                    StateBuilders.State.Builder()
                        .build()
                ).build())
        .build())
        .setContentDescription(iconId)
        .setIconContent(iconId)
        .build()

private fun buttonsLayout(context: Context): Column {
    val glassButton = buttonLayout(context, WaterContainers.GLASS.container)
    val bottleButton = buttonLayout(context, WaterContainers.BOTTLE.container)
    val largeBottleButton = buttonLayout(context, WaterContainers.LARGE_BOTTLE.container)
    val keyboardButton = buttonLayout(context, "keyboard")

    val bottleButtons = MultiButtonLayout.Builder()
        .addButtonContent(bottleButton)
        .addButtonContent(largeBottleButton)
        .build()

    return Column.Builder()
        .addContent(glassButton)
        .addContent(bottleButtons)
        .build()
}

private fun circularProgressLayout(progress: Double, cachedProgress: Double?): CircularProgressIndicator {
    var animationStart = cachedProgress ?: 0.0
    var animationEnd = progress

    val reachedGoal = progress > 1.0
    while (animationEnd > 1.0) {
        animationStart -= 1.0
        animationEnd -= 1.0
    }

    var builder = CircularProgressIndicator.Builder()
        .setStartAngle(-150.0f)
        .setEndAngle(150.0f)
        .setProgress(FloatProp.Builder()
            .setValue(animationEnd.toFloat())
            .setDynamicValue(DynamicFloat.animate(
                animationStart.toFloat(),
                animationEnd.toFloat(),
                AnimationSpec.Builder().setAnimationParameters(
                    AnimationParameters.Builder().setDurationMillis(800).build()
                ).build()
            ))
            .build())

    if (reachedGoal) {
        builder = builder
            .setCircularProgressIndicatorColors(
                ProgressIndicatorColors(
                    ColorBuilders.argb(0xFFFADFAF.toInt()),
                    ColorBuilders.argb(0xFFAFCAFA.toInt())
                )
            )
    }
        
    return builder.build()
}

fun waterLayout(context: Context, readFromCache: Boolean = false): LayoutElementBuilders.LayoutElement {
    val cachedWater = getWaterFromCache(context)

    val water = if (!readFromCache) {
        runBlocking {
            return@runBlocking getWater(context)
        }.getOrDefault(WaterLog())
    } else {
        cachedWater
    }

    return EdgeContentLayout.Builder(buildDeviceParameters(context.resources))
        .setResponsiveContentInsetEnabled(true)
//        .setPrimaryLabelTextContent(
//            Text.Builder(context, "Test ml")
//                .setTypography(Typography.TYPOGRAPHY_CAPTION2)
//                .setColor(ColorProp.Builder(Color.WHITE).build())
//                .build())
        .setContent(buttonsLayout(context))
        .setEdgeContent(circularProgressLayout(water!!.waterGoalProgress, cachedWater?.waterGoalProgress))
        .build()
}

@Preview(device = WearDevices.SMALL_ROUND)
@Preview(device = WearDevices.LARGE_ROUND)
@Composable
fun WaterTilePreview() =
    LayoutRootPreview(root = waterLayout(LocalContext.current), tileResourcesFn = previewResources)