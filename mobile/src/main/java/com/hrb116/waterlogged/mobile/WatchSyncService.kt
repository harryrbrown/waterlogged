package com.hrb116.waterlogged.mobile

import android.util.Log
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.records.HydrationRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import androidx.health.connect.client.units.Volume
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import com.google.android.gms.wearable.WearableListenerService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class WatchSyncService : WearableListenerService() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val TAG = "WatchSyncService"

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        super.onDataChanged(dataEvents)
        
        for (event in dataEvents) {
            if (event.type != com.google.android.gms.wearable.DataEvent.TYPE_CHANGED) continue
            
            val uri = event.dataItem.uri
            val path = uri.path ?: continue
            
            if (path.startsWith("/water_log")) {
                val dataMap = DataMapItem.fromDataItem(event.dataItem).dataMap
                val amount = dataMap.getDouble("amount", 0.0)
                val unit = dataMap.getString("unit", "ml")
                val timestamp = dataMap.getLong("timestamp", System.currentTimeMillis())
                
                Log.d(TAG, "Received water log: $amount $unit at $timestamp")
                
                kotlinx.coroutines.runBlocking {
                    try {
                        saveToHealthConnect(amount, unit, timestamp)
                        Wearable.getDataClient(this@WatchSyncService).deleteDataItems(uri).await()
                    } catch (e: Exception) {
                        Log.e(TAG, "Error processing water log", e)
                    }
                }
            } else if (path.startsWith("/request_sync")) {
                Log.d(TAG, "Received sync request from watch")
                kotlinx.coroutines.runBlocking {
                    try {
                        val healthConnectClient = HealthConnectClient.getOrCreate(this@WatchSyncService)
                        pushDailyTotalToWatch(healthConnectClient)
                        Wearable.getDataClient(this@WatchSyncService).deleteDataItems(uri).await()
                    } catch (e: Exception) {
                        Log.e(TAG, "Error handling sync request", e)
                    }
                }
            }
        }
    }

    private suspend fun saveToHealthConnect(amount: Double, unit: String, timestampMillis: Long) {
        if (HealthConnectClient.getSdkStatus(this) != HealthConnectClient.SDK_AVAILABLE) {
            Log.e(TAG, "Health Connect SDK not available")
            return
        }
        
        val healthConnectClient = HealthConnectClient.getOrCreate(this)
        
        // Ensure permissions are granted before trying to write or read
        val granted = healthConnectClient.permissionController.getGrantedPermissions()
        val writePerm = androidx.health.connect.client.permission.HealthPermission.getWritePermission(HydrationRecord::class)
        val readPerm = androidx.health.connect.client.permission.HealthPermission.getReadPermission(HydrationRecord::class)
        
        if (!granted.contains(writePerm) || !granted.contains(readPerm)) {
            Log.e(TAG, "Missing required permissions. Granted: $granted")
            return
        }

        val volume = when (unit) {
            "fl oz" -> Volume.fluidOuncesUs(amount)
            "cup" -> Volume.fluidOuncesUs(amount * 8.0)
            else -> Volume.milliliters(amount)
        }

        val now = Instant.ofEpochMilli(timestampMillis)
        val zoneOffset = ZoneId.systemDefault().rules.getOffset(now)
        
        val record = HydrationRecord(
            startTime = now,
            endTime = now.plusSeconds(1),
            volume = volume,
            startZoneOffset = zoneOffset,
            endZoneOffset = zoneOffset
        )

        healthConnectClient.insertRecords(listOf(record))
        Log.d(TAG, "Successfully inserted HydrationRecord")
        
        // Push the updated total back to the watch
        pushDailyTotalToWatch(healthConnectClient)
    }

    private suspend fun pushDailyTotalToWatch(healthConnectClient: HealthConnectClient) {
        val today = LocalDate.now()
        val startOfDay = today.atStartOfDay(ZoneId.systemDefault()).toInstant()
        val endOfDay = today.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant()
        
        val request = androidx.health.connect.client.request.AggregateRequest(
            metrics = setOf(HydrationRecord.VOLUME_TOTAL),
            timeRangeFilter = TimeRangeFilter.between(startOfDay, endOfDay)
        )
        
        val response = healthConnectClient.aggregate(request)
        val totalMilliliters = response[HydrationRecord.VOLUME_TOTAL]?.inMilliliters ?: 0.0
        
        val putDataMapReq = PutDataMapRequest.create("/daily_total")
        putDataMapReq.dataMap.putDouble("total_ml", totalMilliliters)
        putDataMapReq.dataMap.putLong("timestamp", System.currentTimeMillis())
        
        val putRequest = putDataMapReq.asPutDataRequest()
        putRequest.setUrgent()
        Wearable.getDataClient(this).putDataItem(putRequest)
        Log.d(TAG, "Pushed daily total to watch: $totalMilliliters ml")
    }
}
