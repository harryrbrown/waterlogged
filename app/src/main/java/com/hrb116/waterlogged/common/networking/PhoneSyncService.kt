package com.hrb116.waterlogged.common.networking

import android.util.Log
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.Wearable
import com.google.android.gms.wearable.WearableListenerService
import com.hrb116.waterlogged.common.WaterContainers
import com.hrb116.waterlogged.common.preferences.WaterLog
import com.hrb116.waterlogged.common.preferences.getWaterGoal
import com.hrb116.waterlogged.common.preferences.saveWaterGoal
import com.hrb116.waterlogged.common.preferences.saveWaterPreset
import com.hrb116.waterlogged.common.preferences.writeWaterToCache
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class PhoneSyncService : WearableListenerService() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val TAG = "PhoneSyncService"

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        super.onDataChanged(dataEvents)

        for (event in dataEvents) {
            if (event.type != com.google.android.gms.wearable.DataEvent.TYPE_CHANGED) continue

            val uri = event.dataItem.uri
            val path = uri.path ?: continue
            val dataMap = DataMapItem.fromDataItem(event.dataItem).dataMap

            scope.launch {
                try {
                    when (path) {
                        "/settings" -> {
                            val glass = dataMap.getString("glass_size")
                            val bottle = dataMap.getString("bottle_size")
                            val largeBottle = dataMap.getString("large_bottle_size")
                            val goal = dataMap.getString("daily_goal")
                            
                            Log.d(TAG, "Received settings from phone: glass=$glass, bottle=$bottle, large=$largeBottle, goal=$goal")
                            
                            if (glass != null) saveWaterPreset(this@PhoneSyncService, WaterContainers.GLASS, glass.toIntOrNull() ?: 250)
                            if (bottle != null) saveWaterPreset(this@PhoneSyncService, WaterContainers.BOTTLE, bottle.toIntOrNull() ?: 500)
                            if (largeBottle != null) saveWaterPreset(this@PhoneSyncService, WaterContainers.LARGE_BOTTLE, largeBottle.toIntOrNull() ?: 750)
                            if (goal != null) {
                                val goalDouble = goal.toDoubleOrNull()
                                if (goalDouble != null) saveWaterGoal(this@PhoneSyncService, goalDouble.toInt())
                            }
                            
                            androidx.wear.tiles.TileService.getUpdater(this@PhoneSyncService)
                                .requestUpdate(com.hrb116.waterlogged.tile.MainTileService::class.java)
                        }
                        "/daily_total" -> {
                            val totalMl = dataMap.getDouble("total_ml", 0.0)
                            Log.d(TAG, "Received daily total from phone: $totalMl ml")
                            
                            val goal = getWaterGoal(this@PhoneSyncService)
                            val progress = if (goal != 0.0) totalMl / goal else 0.0
                            val waterLog = WaterLog(totalMl, goal, progress)
                            
                            writeWaterToCache(this@PhoneSyncService, waterLog)
                            
                            androidx.wear.tiles.TileService.getUpdater(this@PhoneSyncService)
                                .requestUpdate(com.hrb116.waterlogged.tile.MainTileService::class.java)
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error processing sync event: $path", e)
                }
            }
        }
    }
}
