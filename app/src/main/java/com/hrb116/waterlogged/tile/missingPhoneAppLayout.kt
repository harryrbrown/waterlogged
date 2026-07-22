package com.hrb116.waterlogged.tile

import android.content.Context
import android.graphics.Color
import androidx.wear.protolayout.ActionBuilders
import androidx.wear.protolayout.ActionBuilders.AndroidActivity
import androidx.wear.protolayout.ActionBuilders.AndroidStringExtra
import androidx.wear.protolayout.ColorBuilders.ColorProp
import androidx.wear.protolayout.DimensionBuilders.DpProp
import androidx.wear.protolayout.DimensionBuilders.expand
import androidx.wear.protolayout.LayoutElementBuilders
import androidx.wear.protolayout.LayoutElementBuilders.Column
import androidx.wear.protolayout.LayoutElementBuilders.Spacer
import androidx.wear.protolayout.ModifiersBuilders.Clickable
import androidx.wear.protolayout.material.CompactChip
import androidx.wear.protolayout.material.Text
import androidx.wear.protolayout.material.Typography
import androidx.wear.protolayout.material.layouts.PrimaryLayout
import com.hrb116.waterlogged.R
import com.google.android.horologist.compose.tools.buildDeviceParameters

private fun missingPhoneAppColumnLayout(context: Context): Column {
    val spacer = Spacer.Builder().setHeight(DpProp.Builder(10.0f).build()).build()

    val titleText = Text.Builder(context, "Update required")
        .setTypography(Typography.TYPOGRAPHY_TITLE2)
        .setColor(ColorProp.Builder(Color.WHITE).build())
        .build()

    val bodyText = Text.Builder(context, "Waterlogged now requires a phone app.")
        .setTypography(Typography.TYPOGRAPHY_BODY2)
        .setMaxLines(3)
        .setColor(ColorProp.Builder(Color.WHITE).build())
        .build()

    val installChip = CompactChip.Builder(
        context,
        Clickable.Builder()
            .setId("open_update_screen")
            .setOnClick(
                ActionBuilders.LaunchAction.Builder().setAndroidActivity(
                    AndroidActivity.Builder()
                        .setPackageName("com.hrb116.waterlogged")
                        .setClassName("com.hrb116.waterlogged.presentation.MainActivity")
                        .addKeyToExtraMapping("route", AndroidStringExtra.Builder().setValue("update").build())
                        .build()
                    ).build())
            .build(),
        buildDeviceParameters(context.resources))
        .setTextContent("Install App")
        .build()

    return Column.Builder()
        .setWidth(expand())
        .addContent(titleText)
        .addContent(spacer)
        .addContent(bodyText)
        .addContent(spacer)
        .addContent(installChip)
        .build()
}

fun missingPhoneAppLayout(context: Context): LayoutElementBuilders.LayoutElement {
    return PrimaryLayout.Builder(buildDeviceParameters(context.resources))
        .setResponsiveContentInsetEnabled(true)
        .setContent(missingPhoneAppColumnLayout(context))
        .build()
}
