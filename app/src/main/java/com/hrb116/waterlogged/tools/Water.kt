package com.hrb116.waterlogged.tools

import android.content.Context
import android.util.Log
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
    if (!sharedPreferences.contains("water.water")) {
        return null
    }

    val water = sharedPreferences.getFloat("water.water", 0.0f)
    val waterGoal = sharedPreferences.getFloat("water.water_goal", 0.0f)
    val waterGoalProgress = sharedPreferences.getFloat("water.water_goal_progress", 0.0f)

    return WaterLog(water.toDouble(), waterGoal.toDouble(), waterGoalProgress.toDouble())
}

private fun writeWaterToCache(context: Context, waterLog: WaterLog) {
    val sharedPreferences = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
    sharedPreferences.edit().putFloat("water.water", waterLog.water.toFloat()).apply()
    sharedPreferences.edit().putFloat("water.water_goal", waterLog.waterGoal.toFloat()).apply()
    sharedPreferences.edit().putFloat("water.water_goal_progress", waterLog.waterGoalProgress.toFloat()).apply()
}

suspend fun getWater(context: Context): Result<WaterLog> {
    return try {
        Log.d(TAG, "Fetching water...")

        val date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        val token = getValue(context, "access_token")

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

suspend fun postWater(context: Context, amount: String) {
    try {
        Log.d(TAG, "Saving ${amount}ml of water...")

        val date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        val token = getValue(context, "access_token")

        val responseJson = doPostRequest(
            url = "https://api.fitbit.com/1/user/-/foods/log/water.json",
            params = mapOf(
                "amount" to amount,
                "date" to date
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