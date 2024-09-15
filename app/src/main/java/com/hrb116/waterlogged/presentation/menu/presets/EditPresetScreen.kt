package com.hrb116.waterlogged.presentation.menu.presets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.navigation.NavController
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.Text
import androidx.wear.compose.ui.tooling.preview.WearPreviewDevices
import androidx.wear.compose.ui.tooling.preview.WearPreviewFontScales
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.layout.ScalingLazyColumn
import com.google.android.horologist.compose.layout.ScalingLazyColumnDefaults
import com.google.android.horologist.compose.layout.ScreenScaffold
import com.google.android.horologist.compose.layout.rememberResponsiveColumnState
import com.hrb116.waterlogged.R
import com.hrb116.waterlogged.common.WaterContainers
import com.hrb116.waterlogged.common.preferences.getWaterUnit
import com.hrb116.waterlogged.common.preferences.saveWaterPreset
import com.hrb116.waterlogged.common.tokens.Tokens
import com.hrb116.waterlogged.common.tokens.getValue

@OptIn(ExperimentalHorologistApi::class)
@Composable
fun EditPresetScreen(
    container: String,
    onSignedOut: () -> Unit,
    navController: NavController
) {
    val context = LocalContext.current
    val accessToken = getValue(context, Tokens.ACCESS_TOKEN)
    val isAuthenticated = accessToken != null

    if (!isAuthenticated) {
        onSignedOut()
    }

    val columnState = rememberResponsiveColumnState(
        contentPadding = ScalingLazyColumnDefaults.padding(
            first = ScalingLazyColumnDefaults.ItemType.Text,
            last = ScalingLazyColumnDefaults.ItemType.Chip
        )
    )

    var presetValue by remember { mutableStateOf("0") }
    val unit = getWaterUnit(context)

    ScreenScaffold(scrollState = columnState, modifier = Modifier.background(Color.Black)) {
        ScalingLazyColumn(columnState = columnState) {
            item {
                    Text(
                        "$presetValue $unit",
                        textAlign = TextAlign.Center
                    )
            }
            item {
                Row {
                    Button(
                        onClick = { presetValue = updateValue(presetValue, '1') },
                        modifier = Modifier.size(ButtonDefaults.ExtraSmallButtonSize),
                        content = { Text(text = "1")},
                        colors = ButtonDefaults.outlinedButtonColors()
                    )
                    Button(
                        onClick = { presetValue = updateValue(presetValue, '2') },
                        modifier = Modifier.size(ButtonDefaults.ExtraSmallButtonSize),
                        content = { Text(text = "2")},
                        colors = ButtonDefaults.outlinedButtonColors()
                    )
                    Button(
                        onClick = { presetValue = updateValue(presetValue, '3') },
                        modifier = Modifier.size(ButtonDefaults.ExtraSmallButtonSize),
                        content = { Text(text = "3")},
                        colors = ButtonDefaults.outlinedButtonColors()
                    )
                }
            }
            item {
                Row {
                    Button(
                        onClick = { presetValue = updateValue(presetValue, '4') },
                        modifier = Modifier.size(ButtonDefaults.ExtraSmallButtonSize),
                        content = { Text(text = "4")},
                        colors = ButtonDefaults.outlinedButtonColors()
                    )
                    Button(
                        onClick = { presetValue = updateValue(presetValue, '5') },
                        modifier = Modifier.size(ButtonDefaults.ExtraSmallButtonSize),
                        content = { Text(text = "5")},
                        colors = ButtonDefaults.outlinedButtonColors()
                    )
                    Button(
                        onClick = { presetValue = updateValue(presetValue, '6') },
                        modifier = Modifier.size(ButtonDefaults.ExtraSmallButtonSize),
                        content = { Text(text = "6")},
                        colors = ButtonDefaults.outlinedButtonColors()
                    )
                }
            }
            item {
                Row {
                    Button(
                        onClick = { presetValue = updateValue(presetValue, '7') },
                        modifier = Modifier.size(ButtonDefaults.ExtraSmallButtonSize),
                        content = { Text(text = "7")},
                        colors = ButtonDefaults.outlinedButtonColors()
                    )
                    Button(
                        onClick = { presetValue = updateValue(presetValue, '8') },
                        modifier = Modifier.size(ButtonDefaults.ExtraSmallButtonSize),
                        content = { Text(text = "8")},
                        colors = ButtonDefaults.outlinedButtonColors()
                    )
                    Button(
                        onClick = { presetValue = updateValue(presetValue, '9') },
                        modifier = Modifier.size(ButtonDefaults.ExtraSmallButtonSize),
                        content = { Text(text = "9")},
                        colors = ButtonDefaults.outlinedButtonColors()
                    )
                }
            }
            item {
                Row {
                    Button(
                        onClick = { presetValue = backspace(presetValue) },
                        modifier = Modifier.size(ButtonDefaults.ExtraSmallButtonSize),
                        content = { Icon(painter = painterResource(id = R.drawable.backspace_24px), contentDescription = "Backspace") },
                        colors = ButtonDefaults.outlinedButtonColors()
                    )
                    Button(
                        onClick = { presetValue = updateValue(presetValue, '0') },
                        modifier = Modifier.size(ButtonDefaults.ExtraSmallButtonSize),
                        content = { Text(text = "0")},
                        colors = ButtonDefaults.outlinedButtonColors()
                    )
                    Button(
                        onClick = {
                            saveWaterPreset(context, WaterContainers.valueOf(container), presetValue.toInt())
                            navController.popBackStack()
                        },
                        modifier = Modifier.size(ButtonDefaults.ExtraSmallButtonSize),
                        content = { Icon(painter = painterResource(id = R.drawable.check_24px), contentDescription = "Check") },
                        colors = ButtonDefaults.outlinedButtonColors()
                    )
                }
            }
        }
    }
}

private fun updateValue(curr: String, newChar: Char): String {
    return when (curr) {
        "0" -> newChar.toString()
        else -> curr + newChar
    }
}

private fun backspace(curr: String): String {
    return if (curr.length == 1) {
        "0"
    } else {
        curr.dropLast(1)
    }
}

@WearPreviewDevices
@WearPreviewFontScales
@Composable
fun EditPresetScreenPreview() {
    EditPresetScreen(WaterContainers.GLASS.container, {}, NavController(LocalContext.current))
}