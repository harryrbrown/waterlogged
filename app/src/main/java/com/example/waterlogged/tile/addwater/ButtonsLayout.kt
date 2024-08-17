package com.example.waterlogged.tile.addwater

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.wear.protolayout.ActionBuilders
import androidx.wear.protolayout.LayoutElementBuilders
import androidx.wear.protolayout.ModifiersBuilders.Clickable
import androidx.wear.protolayout.StateBuilders
import androidx.wear.protolayout.material.Button
import androidx.wear.protolayout.material.CircularProgressIndicator
import androidx.wear.protolayout.material.layouts.EdgeContentLayout
import androidx.wear.protolayout.material.layouts.MultiButtonLayout
import androidx.wear.tooling.preview.devices.WearDevices
import com.example.waterlogged.tile.previewResources
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

private fun buttonsLayout(context: Context): MultiButtonLayout {
    val glassButton = buttonLayout(context, "glass")
    val bottleButton = buttonLayout(context, "bottle")
    val largeBottleButton = buttonLayout(context, "large_bottle")
    val keyboardButton = buttonLayout(context, "keyboard")

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

fun waterLayout(context: Context): LayoutElementBuilders.LayoutElement = runBlocking {
    launch {
        getWater(context)
    }
    return@runBlocking EdgeContentLayout.Builder(buildDeviceParameters(context.resources))
        .setResponsiveContentInsetEnabled(true)
        .setContent(buttonsLayout(context))
        .setEdgeContent(circularProgressLayout())
        .setEdgeContentThickness(5.0f)
        .build()
}

@Preview(device = WearDevices.SMALL_ROUND)
@Preview(device = WearDevices.LARGE_ROUND)
@Composable
fun WaterTilePreview() =
    LayoutRootPreview(root = waterLayout(LocalContext.current), tileResourcesFn = previewResources)