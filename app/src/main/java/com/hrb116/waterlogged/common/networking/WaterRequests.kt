package com.hrb116.waterlogged.common.networking

import android.content.Context
import android.util.Log
import com.hrb116.waterlogged.common.WaterContainers
import com.hrb116.waterlogged.common.preferences.WaterLog
import com.hrb116.waterlogged.common.preferences.getLocalisedWaterVolume
import com.hrb116.waterlogged.common.preferences.getWaterUnit
import com.hrb116.waterlogged.common.preferences.writeWaterToCache
import com.hrb116.waterlogged.common.tokens.Tokens
import com.hrb116.waterlogged.common.tokens.getValue
import kotlinx.coroutines.CancellationException
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private const val TAG = "Water"

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
