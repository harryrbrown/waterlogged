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
    val unit = getWaterUnit(context) ?: "ml"
    if (unit == "ml") {
        return when (container) {
            WaterContainers.GLASS -> "250 ml"
            WaterContainers.BOTTLE -> "500 ml"
            else -> "750 ml"
        }
    } else if (unit == "fl oz") {
        return when (container) {
            WaterContainers.GLASS -> "8 oz"
            WaterContainers.BOTTLE -> "16 oz"
            else -> "24 oz"
        }
    } else {
        // cup
        return when (container) {
            WaterContainers.GLASS -> "1 cup"
            WaterContainers.BOTTLE -> "2 cups"
            else -> "3 cups"
        }
    }
}
