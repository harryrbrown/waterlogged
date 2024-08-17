package com.example.waterlogged.tile.login

import android.content.Context
import android.graphics.Color
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
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
import androidx.wear.tooling.preview.devices.WearDevices
import com.example.waterlogged.R
import com.example.waterlogged.tile.previewResources
import com.google.android.horologist.compose.tools.LayoutRootPreview
import com.google.android.horologist.compose.tools.buildDeviceParameters

private fun loginColumnLayout(context: Context): Column {
    return Column.Builder()
        .setWidth(expand())
        .addContent(
            Text.Builder(context, context.getString(R.string.sign_in))
                .setTypography(Typography.TYPOGRAPHY_TITLE2)
                .setColor(ColorProp.Builder(Color.WHITE).build())
                .build()
        )
        .addContent(Spacer.Builder().setHeight(DpProp.Builder(10.0f).build()).build())
        .addContent(
            Text.Builder(context, context.getString(R.string.waterlogged_allow_access))
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
                .setTextContent(context.getString(R.string.authenticate))
                .build()
        ).build()
}

fun loginLayout(context: Context): LayoutElementBuilders.LayoutElement {
    return PrimaryLayout.Builder(buildDeviceParameters(context.resources))
        .setResponsiveContentInsetEnabled(true)
        .setContent(loginColumnLayout(context))
        .build()
}

@Preview(device = WearDevices.SMALL_ROUND)
@Preview(device = WearDevices.LARGE_ROUND)
@Composable
fun LoginTilePreview() =
    LayoutRootPreview(root = loginLayout(LocalContext.current), tileResourcesFn = previewResources)