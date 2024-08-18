package com.example.waterlogged.tile.addwater

import android.content.Context
import android.graphics.Color
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.wear.protolayout.ActionBuilders
import androidx.wear.protolayout.ColorBuilders.ColorProp
import androidx.wear.protolayout.LayoutElementBuilders
import androidx.wear.protolayout.LayoutElementBuilders.Column
import androidx.wear.protolayout.ModifiersBuilders.Clickable
import androidx.wear.protolayout.StateBuilders
import androidx.wear.protolayout.TypeBuilders.FloatProp
import androidx.wear.protolayout.expression.DynamicBuilders.DynamicFloat
import androidx.wear.protolayout.material.Button
import androidx.wear.protolayout.material.CircularProgressIndicator
import androidx.wear.protolayout.material.Text
import androidx.wear.protolayout.material.Typography
import androidx.wear.protolayout.material.layouts.EdgeContentLayout
import androidx.wear.protolayout.material.layouts.MultiButtonLayout
import androidx.wear.tooling.preview.devices.WearDevices
import com.example.waterlogged.tile.MainTileService.Companion.KEY_WATER_INTAKE_RATIO
import com.example.waterlogged.tile.previewResources
import com.example.waterlogged.tools.WaterLog
import com.example.waterlogged.tools.getWater
import com.google.android.horologist.compose.tools.LayoutRootPreview
import com.google.android.horologist.compose.tools.buildDeviceParameters
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
    val glassButton = buttonLayout(context, "glass")
    val bottleButton = buttonLayout(context, "bottle")
    val largeBottleButton = buttonLayout(context, "large_bottle")
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

private fun circularProgressLayout(progress: Double): CircularProgressIndicator {
    return CircularProgressIndicator.Builder()
        .setStartAngle(30.0f)
        .setEndAngle(330.0f)
        .setProgress(FloatProp.Builder()
            .setValue(progress.toFloat())
            // TODO - find a way to get this to work
//            .setDynamicValue(DynamicFloat.from(KEY_WATER_INTAKE_RATIO).animate())
            .build())
        .build()
}

fun waterLayout(context: Context): LayoutElementBuilders.LayoutElement {
    val water = runBlocking {
        return@runBlocking getWater(context)
    }.getOrDefault(WaterLog())

    return EdgeContentLayout.Builder(buildDeviceParameters(context.resources))
        .setResponsiveContentInsetEnabled(true)
//        .setPrimaryLabelTextContent(
//            Text.Builder(context, "Test ml")
//                .setTypography(Typography.TYPOGRAPHY_CAPTION2)
//                .setColor(ColorProp.Builder(Color.WHITE).build())
//                .build())
        .setContent(buttonsLayout(context))
        .setEdgeContent(circularProgressLayout(water.waterGoalProgress))
        .build()
}

@Preview(device = WearDevices.SMALL_ROUND)
@Preview(device = WearDevices.LARGE_ROUND)
@Composable
fun WaterTilePreview() =
    LayoutRootPreview(root = waterLayout(LocalContext.current), tileResourcesFn = previewResources)