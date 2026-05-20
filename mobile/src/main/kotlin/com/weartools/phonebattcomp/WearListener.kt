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
import android.content.Context
import android.os.BatteryManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import androidx.datastore.core.DataStore
import com.google.android.gms.wearable.CapabilityInfo
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.WearableListenerService
import com.weartools.phonebattcomp.data.UserPreferences
import com.weartools.phonebattcomp.di.ServiceCommunication
import com.weartools.phonebattcomp.receiver.CalendarContentObserver
import com.weartools.phonebattcomp.receiver.CalendarContentObserver.Companion.arePermissionsGranted
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

const val BATTERY_PATH = "/battery-level"
const val BATTERY_KEY= "battery-key"
const val IS_CHARGING_KEY = "is-charging-key"
const val CHARGE_TIME_REMAINING_KEY = "charge-time-remaining-key"

private const val REQUEST_PATH = "/request"
private const val FORCE_UPDATE_KEY = "force-update-key"

private const val CALENDAR_REQUEST_PATH = "/calendar-request"
private const val NOTIFICATIONS_REQUEST_PATH = "/notifications-request"

@AndroidEntryPoint
class WearListener : WearableListenerService() {

    private val ioScope = CoroutineScope(Dispatchers.IO)

    @Inject
    lateinit var dataStore: DataStore<UserPreferences>

    @Inject lateinit var dataClient: DataClient
    @Inject lateinit var batteryManager: BatteryManager

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
                            sendBatteryInfoToWatch(
                                level = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY),
                                isCharging = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_STATUS) == BatteryManager.BATTERY_STATUS_CHARGING,
                                forceUpdate = DataMapItem.fromDataItem(dataEvent.dataItem).dataMap.getBoolean(FORCE_UPDATE_KEY),
                                dataClient = dataClient,
                                batteryManager = batteryManager
                            )
                        }
                        CALENDAR_REQUEST_PATH -> {
                            ioScope.launch {
                                if (dataStore.data.first().calendarSync){
                                    if (applicationContext.arePermissionsGranted(Manifest.permission.READ_CALENDAR)) {
                                        CalendarContentObserver.queryAllFutureCalendarEventAndSend(applicationContext, dataStore.data.first().syncedCalendarsIds)
                                    }
                                    else {
                                        dataStore.updateData { it.copy(calendarSync = false) }
                                    }
                                }
                            }
                        }
                        NOTIFICATIONS_REQUEST_PATH -> {
                            ioScope.launch { sendNotificationsToWatch(this@WearListener, dataStore.data.first().notificationsSync) }
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

    override fun onCapabilityChanged(capabilityInfo: CapabilityInfo) {
        super.onCapabilityChanged(capabilityInfo)
        if (capabilityInfo.name == BuildConfig.CAPABILITY_WEAR_APP){
            //Log.d("MobileListener", "capabilityInfo.name matches ${BuildConfig.CAPABILITY_MOBILE_APP}")
            if (capabilityInfo.nodes.isNotEmpty()){
                //Log.d("MobileListener", "capability ${capabilityInfo.name} connected!")
                sendBatteryInfoToWatch(
                    level = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY),
                    isCharging = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_STATUS) == BatteryManager.BATTERY_STATUS_CHARGING,
                    forceUpdate = true,
                    dataClient = dataClient,
                    batteryManager = batteryManager,
                )
                // TODO: Test: maybe it's good to also send notifications to watch when watch is reconnected?
                ioScope.launch { sendNotificationsToWatch(this@WearListener,dataStore.data.first().notificationsSync) }
            }
        }
    }
    companion object{
        fun sendBatteryInfoToWatch(
            level: Int,
            isCharging: Boolean,
            forceUpdate: Boolean,
            dataClient: DataClient,
            batteryManager: BatteryManager
        ){
            val request = PutDataMapRequest.create(BATTERY_PATH).apply{
                dataMap.putInt(BATTERY_KEY, level)
                dataMap.putBoolean(IS_CHARGING_KEY, isCharging)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    dataMap.putLong(CHARGE_TIME_REMAINING_KEY, batteryManager.computeChargeTimeRemaining())
                }
                if (forceUpdate){
                    dataMap.putLong("immediate-update", System.currentTimeMillis())
                }
            }
                .asPutDataRequest()
                .setUrgent()

            try {
                dataClient.putDataItem(request)
            } catch (e: Exception) {
                Log.e(TAG, "sendBatteryInfoToWatch failed: ${e.message}")
            }
        }
        suspend fun sendNotificationsToWatch(context: Context, notificationsSync: Boolean){
            val isServiceRunning = NotificationManagerCompat.getEnabledListenerPackages(context).contains(BuildConfig.APPLICATION_ID)
            if (isServiceRunning && notificationsSync){
                //Log.i(TAG, "Wear OS device connected, sending notifications")
                ServiceCommunication.sendToWatchFlow.emit(Unit)
            }
        }
    }
}