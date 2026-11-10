package com.hrb116.waterlogged.presentation.menu

import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.ListHeader
import androidx.wear.compose.material.Text
import androidx.wear.compose.ui.tooling.preview.WearPreviewDevices
import androidx.wear.compose.ui.tooling.preview.WearPreviewFontScales
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.layout.ScalingLazyColumn
import com.google.android.horologist.compose.layout.ScalingLazyColumnDefaults
import com.google.android.horologist.compose.layout.ScreenScaffold
import com.google.android.horologist.compose.layout.rememberResponsiveColumnState
import com.hrb116.waterlogged.R

import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.wear.remote.interactions.RemoteActivityHelper
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalHorologistApi::class)
@Composable
fun ListScreen(
    onPresets: () -> Unit
) {
    val columnState = rememberResponsiveColumnState(
        contentPadding = ScalingLazyColumnDefaults.padding(
            first = ScalingLazyColumnDefaults.ItemType.Text,
            last = ScalingLazyColumnDefaults.ItemType.Chip
        )
    )

    val context = LocalContext.current
    var isCompanionInstalled by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        try {
            val capabilityClient = Wearable.getCapabilityClient(context)
            val capabilityInfo = capabilityClient
                .getCapability("waterlogged_phone_app", CapabilityClient.FILTER_ALL)
                .await()
            isCompanionInstalled = capabilityInfo.nodes.isNotEmpty()
            
            capabilityClient.addListener({ info ->
                isCompanionInstalled = info.nodes.isNotEmpty()
            }, "waterlogged_phone_app")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    ScreenScaffold(scrollState = columnState, modifier = Modifier.background(Color.Black)) {
        ScalingLazyColumn(columnState = columnState) {
            item {
                ListHeader {
                    Text(
                        stringResource(R.string.waterlogged),
                        textAlign = TextAlign.Center
                    )
                }
            }
            item {
                Chip(
                    onClick = { onPresets() },
                    label = { Text(text = stringResource(R.string.edit_presets)) },
                    icon = {
                        Icon(painter = painterResource(id = R.drawable.edit_24px), contentDescription = "Edit presets")
                    },
                    colors = ChipDefaults.secondaryChipColors()
                )
            }
            if (!isCompanionInstalled) {
                item {
                    Chip(
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW)
                                .addCategory(Intent.CATEGORY_BROWSABLE)
                                .setData(Uri.parse("market://details?id=${context.packageName}"))
                            
                            RemoteActivityHelper(context, java.util.concurrent.Executors.newSingleThreadExecutor())
                                .startRemoteActivity(intent)
                        },
                        label = { Text("Install Phone App") },
                        colors = ChipDefaults.primaryChipColors()
                    )
                }
            }
        }
    }
}

@WearPreviewDevices
@WearPreviewFontScales
@Composable
fun ListScreenPreview() {
    ListScreen({})
}