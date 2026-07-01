package com.hrb116.waterlogged.common.networking

import android.content.Context
import android.util.Log
import com.hrb116.waterlogged.common.WaterContainers
import com.hrb116.waterlogged.common.preferences.WaterLog
import com.hrb116.waterlogged.common.preferences.getLocalisedWaterVolume
import com.hrb116.waterlogged.common.preferences.getWaterGoal
import com.hrb116.waterlogged.common.preferences.getWaterUnit
import com.hrb116.waterlogged.common.preferences.writeWaterToCache
import com.hrb116.waterlogged.common.tokens.Tokens
import com.hrb116.waterlogged.common.tokens.getValue
import kotlinx.coroutines.CancellationException
import org.json.JSONObject
import java.net.URLEncoder
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

private const val TAG = "Water"
private const val BASE_URL = "https://health.googleapis.com/v4/users/me/dataTypes"

private fun toMilliliters(amount: Double, unit: String): Double = when (unit) {
    "fl oz" -> amount * 29.5735
    "cup" -> amount * 236.588
    else -> amount
}

private fun fromMilliliters(milliliters: Double, unit: String): Double = when (unit) {
    "fl oz" -> milliliters / 29.5735
    "cup" -> milliliters / 236.588
    else -> milliliters
}

private fun unitToGoogleEnum(unit: String): String = when (unit) {
    "fl oz" -> "FLUID_OUNCE_US"
    "cup" -> "CUP_US"
    else -> "MILLILITER"
}

suspend fun postWater(context: Context, container: WaterContainers) {
    try {
        val amountString = getLocalisedWaterVolume(context, container)
        val amount = amountString.split(" ")[0].toDouble()
        val unit = getWaterUnit(context) ?: "ml"
        Log.d(TAG, "Saving $amountString of water...")

        val token = getValue(context, Tokens.ACCESS_TOKEN)
        val now = Instant.now()
        val offsetSeconds = ZoneId.systemDefault().rules.getOffset(now).totalSeconds
        val utcOffsetString = "${offsetSeconds}s"

        val intervalJson = JSONObject().apply {
            put("startTime", now.toString())
            put("startUtcOffset", utcOffsetString)
            put("endTime", now.plusSeconds(1).toString())
            put("endUtcOffset", utcOffsetString)
        }
        val amountConsumedJson = JSONObject().apply {
            put("milliliters", toMilliliters(amount, unit))
            put("userProvidedUnit", unitToGoogleEnum(unit))
        }
        val body = JSONObject().apply {
            put("hydrationLog", JSONObject().apply {
                put("interval", intervalJson)
                put("amountConsumed", amountConsumedJson)
            })
        }

        val responseJson = doPostJsonRequest(
            url = "$BASE_URL/hydration-log/dataPoints",
            body = body,
            requestHeaders = mapOf("Authorization" to "Bearer $token")
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

        val token = getValue(context, Tokens.ACCESS_TOKEN)
        val unit = getWaterUnit(context) ?: "ml"
        val today = LocalDate.now()
        val tomorrow = today.plusDays(1)

        val filter = "hydration_log.interval.civil_start_time >= \"${today}T00:00:00\" AND " +
                "hydration_log.interval.civil_start_time < \"${tomorrow}T00:00:00\""
        val url = "$BASE_URL/hydration-log/dataPoints?filter=${URLEncoder.encode(filter, "UTF-8")}"

        val responseJson = doGetRequest(
            url = url,
            requestHeaders = mapOf("Authorization" to "Bearer $token")
        )

        Log.d(TAG, responseJson.toString())

        val dataPoints = responseJson.optJSONArray("dataPoints")
        var totalMilliliters = 0.0
        if (dataPoints != null) {
            for (i in 0 until dataPoints.length()) {
                val milliliters = dataPoints.getJSONObject(i)
                    .optJSONObject("hydrationLog")
                    ?.optJSONObject("amountConsumed")
                    ?.optDouble("milliliters", 0.0) ?: 0.0
                totalMilliliters += milliliters
            }
        }

        val water = fromMilliliters(totalMilliliters, unit)
        val goal = getWaterGoal(context)
        val progress = if (goal != 0.0) water / goal else 0.0

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
