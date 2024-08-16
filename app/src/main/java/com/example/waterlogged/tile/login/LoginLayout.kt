package com.example.waterlogged.tile.login

import android.content.Context
import android.graphics.Color
import androidx.wear.protolayout.ActionBuilders
import androidx.wear.protolayout.ActionBuilders.AndroidActivity
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
import com.google.android.horologist.compose.tools.buildDeviceParameters

private fun loginColumnLayout(context: Context): Column {
    return Column.Builder()
        .setWidth(expand())
        .addContent(
            Text.Builder(context, "Sign in")
                .setTypography(Typography.TYPOGRAPHY_TITLE2)
                .setColor(ColorProp.Builder(Color.WHITE).build())
                .build()
        )
        .addContent(Spacer.Builder().setHeight(DpProp.Builder(10.0f).build()).build())
        .addContent(
            Text.Builder(context, "Allow Waterlogged to access your water data.")
                .setTypography(Typography.TYPOGRAPHY_BODY2)
                .setMaxLines(3)
                .setColor(ColorProp.Builder(Color.WHITE).build())
                .build()
        )
        .addContent(Spacer.Builder().setHeight(DpProp.Builder(10.0f).build()).build())
        .addContent(
            CompactChip.Builder(
                context,
                Clickable.Builder()
                    .setId("foo")
                    .setOnClick(
                        ActionBuilders.LaunchAction.Builder().setAndroidActivity(
                            AndroidActivity.Builder()
                                .setPackageName("com.example.waterlogged")
                                .setClassName("com.example.waterlogged.presentation.oauth.pkce.AuthPKCEActivity")
                                .build()
                        ).build()) .build(),
                buildDeviceParameters(context.resources)
            )
                .setTextContent("Authorise")
                .build()
        ).build()
}

fun loginLayout(context: Context): LayoutElementBuilders.LayoutElement {
    return PrimaryLayout.Builder(buildDeviceParameters(context.resources))
        .setResponsiveContentInsetEnabled(true)
        .setContent(loginColumnLayout(context))
        .build()
}
