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

import android.Manifest
import android.content.ContentValues.TAG
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.util.Log
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.WearableListenerService
import com.weartools.phonebattcomp.CalendarContentObserver.Companion.arePermissionsGranted
import com.weartools.phonebattcomp.data.DataStoreRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

const val BATTERY_PATH = "/battery-level"
const val BATTERY_KEY= "battery-key"
const val IS_CHARGING_KEY = "is-charging-key"

private const val REQUEST_PATH = "/request"
private const val FORCE_UPDATE_KEY = "force-update-key"

const val ACTIVE_SYNC_PATH = "/active-sync"
const val ACTIVE_SYNC_KEY = "active-sync-key"

private const val CALENDAR_REQUEST_PATH = "/calendar-request"

@AndroidEntryPoint
class WearListener : WearableListenerService() {

    private val ioScope = CoroutineScope(Dispatchers.IO)

    @Inject
    lateinit var dataRepository: DataStoreRepository
    @Inject
    lateinit var dataClient: DataClient

    override fun onDataChanged(dataEvents: DataEventBuffer) {

        /** Freeze dataEvents before processing **/
        val frozenDataEvents = dataEvents.map {
            it.freeze()
        }

        frozenDataEvents.forEach { dataEvent ->
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
                                }
                            }
                                .asPutDataRequest()
                                .setUrgent()

                            dataClient.putDataItem(request)
                        }
                        CALENDAR_REQUEST_PATH -> {
                            Log.i(TAG, "Calendar request path received")
                            ioScope.launch {
                                if (dataRepository.calendarSync.first()){
                                    if (applicationContext.arePermissionsGranted(Manifest.permission.READ_CALENDAR)) {
                                        CalendarContentObserver.queryAllFutureCalendarEventAndSend(applicationContext)
                                    }
                                    else {
                                        dataRepository.setCalendarSyncState(false)
                                    }
                                }
                            }
                        }
                    }
                }
                DataEvent.TYPE_DELETED -> { Log.v(TAG, "Data deleted : " + dataEvent.dataItem.toString()) }
                else -> { Log.e(TAG, "Unknown data event Type = " + dataEvent.type) }
            }
            /** Release dataEvents after processing them */
            dataEvents.release()
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