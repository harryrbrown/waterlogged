package com.hrb116.waterlogged.common.networking

import android.content.Context
import android.util.Log
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import com.hrb116.waterlogged.common.WaterContainers
import com.hrb116.waterlogged.common.preferences.WaterLog
import com.hrb116.waterlogged.common.preferences.getLocalisedWaterVolume
import com.hrb116.waterlogged.common.preferences.getWaterGoal
import com.hrb116.waterlogged.common.preferences.getWaterUnit
import com.hrb116.waterlogged.common.preferences.getWaterFromCache
import com.hrb116.waterlogged.common.preferences.writeWaterToCache
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.CancellationException

private const val TAG = "SyncManager"

suspend fun postWater(context: Context, container: WaterContainers): Double? {
    try {
        val amountString = getLocalisedWaterVolume(context, container)
        val amount = amountString.split(" ")[0].toDouble()
        val unit = getWaterUnit(context) ?: "ml"
        Log.d(TAG, "Syncing $amountString of water to phone...")

        val putDataMapReq = PutDataMapRequest.create("/water_log/${System.currentTimeMillis()}")
        putDataMapReq.dataMap.putDouble("amount", amount)
        putDataMapReq.dataMap.putString("unit", unit)
        putDataMapReq.dataMap.putLong("timestamp", System.currentTimeMillis())

        val request = putDataMapReq.asPutDataRequest()
        request.setUrgent()
        Wearable.getDataClient(context).putDataItem(request).await()
        Log.d(TAG, "Successfully synced water to phone")

        // Optimistically update the local cache so the UI updates instantly
        val currentLog = getWater(context).getOrNull() ?: WaterLog(0.0, getWaterGoal(context), 0.0)
        val oldProgress = if (currentLog.waterGoal > 0) currentLog.water / currentLog.waterGoal else 0.0
        val newWater = currentLog.water + amount
        val newProgress = if (currentLog.waterGoal > 0) newWater / currentLog.waterGoal else 0.0
        writeWaterToCache(context, WaterLog(newWater, currentLog.waterGoal, newProgress))

        // Trigger a tile update
        androidx.wear.tiles.TileService.getUpdater(context)
            .requestUpdate(com.hrb116.waterlogged.tile.MainTileService::class.java)
            
        return oldProgress
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Log.e(TAG, "Failed to sync water", e)
        return null
    }
}

suspend fun getWater(context: Context): Result<WaterLog> {
    // The daily total is pushed from the phone asynchronously to the watch.
    // So we just return the local cached value.
    return try {
        val cached = getWaterFromCache(context) ?: WaterLog(0.0, getWaterGoal(context), 0.0)
        Result.success(cached)
    } catch (e: Exception) {
        Result.failure(e)
    }
}

suspend fun requestSync(context: Context) {
    try {
        val putDataMapReq = PutDataMapRequest.create("/request_sync/${System.currentTimeMillis()}")
        putDataMapReq.dataMap.putLong("timestamp", System.currentTimeMillis())
        val request = putDataMapReq.asPutDataRequest()
        request.setUrgent()
        Wearable.getDataClient(context).putDataItem(request).await()
        Log.d(TAG, "Sent sync request to phone")
    } catch (e: Exception) {
        Log.e(TAG, "Failed to send sync request", e)
    }
}
