package com.hrb116.waterlogged.common.preferences

import android.content.Context
import android.util.Log
import com.hrb116.waterlogged.common.WaterContainers
import com.hrb116.waterlogged.common.networking.doGetRequest
import com.hrb116.waterlogged.common.tokens.Tokens
import com.hrb116.waterlogged.common.tokens.getValue
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.CancellationException

data class WaterLog(
    val water: Double = 0.0,
    val waterGoal: Double = 0.0,
    val waterGoalProgress: Double = 0.0
)

fun getWaterFromCache(context: Context): WaterLog? {
    val sharedPreferences = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
    if (!sharedPreferences.contains(Preferences.WATER.key)) {
        return null
    }

    val water = sharedPreferences.getFloat(Preferences.WATER.key, 0.0f)
    val waterGoal = sharedPreferences.getFloat(Preferences.WATER_GOAL.key, 0.0f)
    val waterGoalProgress = sharedPreferences.getFloat(Preferences.WATER_GOAL_PROGRESS.key, 0.0f)

    return WaterLog(water.toDouble(), waterGoal.toDouble(), waterGoalProgress.toDouble())
}

fun writeWaterToCache(context: Context, waterLog: WaterLog) {
    val sharedPreferences = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
    sharedPreferences.edit().putFloat(Preferences.WATER.key, waterLog.water.toFloat()).apply()
    sharedPreferences.edit().putFloat(Preferences.WATER_GOAL.key, waterLog.waterGoal.toFloat()).apply()
    sharedPreferences.edit().putFloat(Preferences.WATER_GOAL_PROGRESS.key, waterLog.waterGoalProgress.toFloat()).apply()
}

fun getWaterUnit(context: Context): String? {
    val sharedPreferences = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
    return sharedPreferences.getString(Preferences.WATER_UNIT.key, null)
}

fun saveWaterUnit(context: Context, unit: String) {
    val sharedPreferences = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
    sharedPreferences.edit().putString(Preferences.WATER_UNIT.key, unit).apply()
}

fun getLocalisedWaterVolume(context: Context, container: WaterContainers): String {
    var unit = getWaterUnit(context) ?: "ml"
    if (unit == "fl oz") unit = "oz"
    return "${getWaterPreset(context, container)} $unit"
}

fun getWaterPreset(context: Context, container: WaterContainers): Int {
    val sharedPreferences = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
    return when (container) {
        WaterContainers.GLASS -> sharedPreferences.getInt(Preferences.WATER_AMOUNT_1.key, getDefaultWaterAmount(context, container))
        WaterContainers.BOTTLE -> sharedPreferences.getInt(Preferences.WATER_AMOUNT_2.key, getDefaultWaterAmount(context, container))
        WaterContainers.LARGE_BOTTLE -> sharedPreferences.getInt(Preferences.WATER_AMOUNT_3.key, getDefaultWaterAmount(context, container))
    }
}

private fun getDefaultWaterAmount(context: Context, container: WaterContainers): Int {
    val unit = getWaterUnit(context) ?: "ml"
    if (unit == "ml") {
        return when (container) {
            WaterContainers.GLASS -> 250
            WaterContainers.BOTTLE -> 500
            WaterContainers.LARGE_BOTTLE -> 750
        }
    } else if (unit == "fl oz") {
        return when (container) {
            WaterContainers.GLASS -> 8
            WaterContainers.BOTTLE -> 16
            WaterContainers.LARGE_BOTTLE -> 24
        }
    } else {
        // cup
        return when (container) {
            WaterContainers.GLASS -> 1
            WaterContainers.BOTTLE -> 2
            WaterContainers.LARGE_BOTTLE -> 3
        }
    }
}

fun saveWaterPreset(context: Context, container: WaterContainers, amount: Int) {
    val sharedPreferences = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
    when (container) {
        WaterContainers.GLASS -> sharedPreferences.edit().putInt(Preferences.WATER_AMOUNT_1.key, amount).apply()
        WaterContainers.BOTTLE -> sharedPreferences.edit().putInt(Preferences.WATER_AMOUNT_2.key, amount).apply()
        WaterContainers.LARGE_BOTTLE -> sharedPreferences.edit().putInt(Preferences.WATER_AMOUNT_3.key, amount).apply()
    }
}
