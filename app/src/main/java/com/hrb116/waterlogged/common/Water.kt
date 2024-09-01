package com.hrb116.waterlogged.common

import android.content.Context
import android.util.Log
import com.hrb116.waterlogged.common.tokens.Tokens
import com.hrb116.waterlogged.common.tokens.getValue
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.CancellationException

private const val TAG = "Water"

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

private fun writeWaterToCache(context: Context, waterLog: WaterLog) {
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

suspend fun getWater(context: Context): Result<WaterLog> {
    return try {
        Log.d(TAG, "Fetching water...")

        val date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        val token = getValue(context, Tokens.ACCESS_TOKEN)

        val waterLogJson = doGetRequest(
            url = "https://api.fitbit.com/1/user/-/foods/log/water/date/$date.json",
            requestHeaders = mapOf(
                "Authorization" to "Bearer $token"
            )
        )

        val waterGoalJson = doGetRequest(
            url = "https://api.fitbit.com/1/user/-/foods/log/water/goal.json",
            requestHeaders = mapOf(
                "Authorization" to "Bearer $token"
            )
        )

        Log.d(TAG, waterLogJson.toString())
        Log.d(TAG, waterGoalJson.toString())

        val water = waterLogJson.getJSONObject("summary").getDouble("water")
        val goal = waterGoalJson.getJSONObject("goal").getDouble("goal")
        val progress = if (goal != 0.0) { water / goal } else { 0.0 }

        val result = WaterLog(water, goal, progress)

        writeWaterToCache(context, result)

        Result.success(result)
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        e.printStackTrace()
        Result.failure(e)
    }
}

suspend fun postWater(context: Context, container: WaterContainers) {
    try {
        val amountString = getLocalisedWaterVolume(context, container)
        val amount = amountString.split(" ")[0]
        Log.d(TAG, "Saving $amountString of water...")

        val date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        val token = getValue(context, Tokens.ACCESS_TOKEN)

        val responseJson = doPostRequest(
            url = "https://api.fitbit.com/1/user/-/foods/log/water.json",
            params = mapOf(
                "amount" to amount,
                "date" to date,
                "unit" to (getWaterUnit(context) ?: "ml")
            ),
            requestHeaders = mapOf(
                "Authorization" to "Bearer $token"
            )
        )

        Log.d(TAG, responseJson.toString())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
