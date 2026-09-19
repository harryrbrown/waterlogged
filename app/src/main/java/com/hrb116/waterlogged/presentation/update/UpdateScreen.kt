package com.hrb116.waterlogged.presentation.update

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.ListHeader
import androidx.wear.remote.interactions.RemoteActivityHelper
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.layout.ScalingLazyColumn
import com.google.android.horologist.compose.layout.ScalingLazyColumnDefaults
import com.google.android.horologist.compose.layout.ScreenScaffold
import com.google.android.horologist.compose.layout.rememberResponsiveColumnState
import java.util.concurrent.Executors

@OptIn(ExperimentalHorologistApi::class)
@Composable
fun UpdateScreen() {
    val columnState = rememberResponsiveColumnState(
        contentPadding = ScalingLazyColumnDefaults.padding(
            first = ScalingLazyColumnDefaults.ItemType.Text,
            last = ScalingLazyColumnDefaults.ItemType.Chip
        )
    )
    val context = LocalContext.current
    
    ScreenScaffold(scrollState = columnState, modifier = Modifier.background(Color.Black).fillMaxSize()) {
        ScalingLazyColumn(columnState = columnState) {
            item {
                ListHeader {
                    Text("App Missing", textAlign = TextAlign.Center)
                }
            }
            item {
                Text(
                    text = "Waterlogged has moved to Health Connect, and now requires a companion mobile app.",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            item {
                Chip(
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW)
                            .addCategory(Intent.CATEGORY_BROWSABLE)
                            .setData(Uri.parse("market://details?id=${context.packageName}"))
                            
                        RemoteActivityHelper(context, Executors.newSingleThreadExecutor())
                            .startRemoteActivity(intent)
                    },
                    label = { 
                        Text(
                            text = "Install on Phone",
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        ) 
                    },
                    colors = ChipDefaults.primaryChipColors(),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
