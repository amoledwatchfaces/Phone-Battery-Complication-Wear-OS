/*
 * Copyright 2022 amoledwatchfaces™
 * support@amoledwatchfaces.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.weartools.phonebattcomp

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataItem
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import com.google.android.gms.wearable.WearableListenerService
import kotlinx.coroutines.runBlocking

const val BATTERY_PATH = "/battery-level"
const val BATTERY_KEY= "battery-key"
const val IS_CHARGING_KEY = "is-charging-key"

private const val REQUEST_PATH = "/request"
private const val FORCE_UPDATE_KEY = "force-update-key"

private const val ACTIVE_SYNC_PATH = "/active-sync"
private const val ACTIVE_SYNC_KEY = "active-sync-key"

class WearListener : WearableListenerService() {

    private val repository by lazy { DataRepository(this) }

    @SuppressLint("VisibleForTests")
    override fun onDataChanged(dataEvents: DataEventBuffer) {
        if (Log.isLoggable(TAG, Log.DEBUG)) { Log.d(TAG, "onDataChanged: $dataEvents") }

        dataEvents.forEach { dataEvent ->
            when (dataEvent.type) {
                DataEvent.TYPE_CHANGED -> {
                    when (dataEvent.dataItem.uri.path) {
                        REQUEST_PATH -> {
                                    val level = batteryLevel
                                    val forceUpdate = DataMapItem.fromDataItem(dataEvent.dataItem).dataMap.getBoolean(FORCE_UPDATE_KEY)
                                    val request = PutDataMapRequest.create(BATTERY_PATH).apply{
                                        dataMap.putInt(BATTERY_KEY, level)
                                        dataMap.putBoolean(IS_CHARGING_KEY, BatteryStatusBroadcastReceiver.getCurrentBatteryChargingStatus(applicationContext))
                                        if (forceUpdate){
                                            dataMap.putLong("immediate-update", System.currentTimeMillis())
                                        }}
                                        .asPutDataRequest()
                                        .setUrgent()

                                    val dataItemTask: Task<DataItem> = Wearable.getDataClient(this).putDataItem(request)
                                    dataItemTask
                                        .addOnSuccessListener { dataItem -> Log.d(TAG,"WL: Sending Phone Battery request was successful: $dataItem") }
                                        .addOnFailureListener { e -> Log.e(TAG,"WL: Sending request task failed!: $e") }
                                        .addOnCompleteListener{task -> Log.d(TAG,"WL: Sending request Task complete!: $task")}
                                    }
                        ACTIVE_SYNC_PATH -> {
                            Log.d(TAG,"Active Sync: Received status change info!")
                            val activeSyncState = DataMapItem.fromDataItem(dataEvent.dataItem).dataMap.getBoolean(ACTIVE_SYNC_KEY)
                            if (activeSyncState) {
                                Log.d(TAG,"Turning Active Sync ON")
                                BatteryStatusBroadcastReceiver.subscribeToUpdates(this)
                                runBlocking {
                                    repository.setActiveSyncState(true)
                                }
                            }
                            else {
                                Log.d(TAG,"Turning Active Sync OFF")
                                BatteryStatusBroadcastReceiver.unsubscribeFromUpdates(this)
                                runBlocking {
                                    repository.setActiveSyncState(false)
                                }
                            }
                        }
                    }
                }

                DataEvent.TYPE_DELETED -> { Log.v(TAG, "Data deleted : " + dataEvent.dataItem.toString()) }
                else -> { Log.e(TAG, "Unknown data event Type = " + dataEvent.type) }
            }
            }
        }

    private val batteryLevel: Int
        get() {
            val batteryStatus = registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
            val scale = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
            return 100 * level / scale
        }

}