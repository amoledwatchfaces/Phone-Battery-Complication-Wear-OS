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

import android.content.ContentValues.TAG
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.wear.tiles.TileService
import com.google.android.gms.wearable.CapabilityInfo
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMap
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.WearableListenerService
import com.weartools.phonebattcomp.complication.MobileBatteryComplicationService
import com.weartools.phonebattcomp.complication.NotificationsIconsComplicationService
import com.weartools.phonebattcomp.complication.notificationsList
import com.weartools.phonebattcomp.data.CalendarEvent
import com.weartools.phonebattcomp.data.UserPreferences
import com.weartools.phonebattcomp.data.UserPreferencesRepository
import com.weartools.phonebattcomp.tile.PhoneBatteryTileService
import com.weartools.phonebattcomp.utils.updateCalendarComplications
import com.weartools.phonebattcomp.utils.updateComplication
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject

private const val BATTERY_PATH = "/battery-level"
private const val BATTERY_KEY= "battery-key"
private const val REQUEST_PATH = "/request"
private const val REQUEST_KEY = "request-key"
private const val FORCE_UPDATE_KEY = "force-update-key"

private const val ACTIVE_SYNC_PATH = "/active-sync"
private const val ACTIVE_SYNC_KEY = "active-sync-key"
private const val IS_CHARGING_KEY = "is-charging-key"
private const val URI = "/foobar"

private const val CALENDAR_EVENTS_PATH = "/calendar-events"
private const val CALENDAR_EVENTS_KEY = "events"
const val CALENDAR_REQUEST_PATH = "/calendar-request"

const val NOTIFICATIONS_REQUEST_PATH = "/notifications-request"
const val NOTIFICATIONS_UPDATE_KEY = "ts"

var lastCalendarRequestTime = 0L

@AndroidEntryPoint
class MobileListener : WearableListenerService() {

    @Inject
    lateinit var dataStore: DataStore<UserPreferences>
    private val preferences by lazy { UserPreferencesRepository(dataStore).getPreferences() }

    @Inject lateinit var dataClient: DataClient

    private val ioScope = CoroutineScope(Dispatchers.IO)

    private val mutex = Mutex()

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        super.onDataChanged(dataEvents)


        /** Freeze dataEvents before processing **/
        val frozenDataEvents = dataEvents.map {
            it.freeze()
        }

        frozenDataEvents.forEach { dataEvent ->

            /** Process dataEvent **/
            when (dataEvent.type) {
                DataEvent.TYPE_CHANGED -> {
                    when (dataEvent.dataItem.uri.path) {
                        BATTERY_PATH -> {
                            val dataMapItem = DataMapItem.fromDataItem(dataEvent.dataItem)

                            ioScope.launch {
                                dataStore.updateData {
                                    it.copy(
                                        phoneBatteryLevel = dataMapItem.dataMap.getInt(BATTERY_KEY),
                                        phoneIsCharging = dataMapItem.dataMap.getBoolean(IS_CHARGING_KEY),
                                        phoneIsConnected = true,
                                        afterMobileResult = true,
                                        lastUpdate = System.currentTimeMillis()
                                    )
                                }
                                updateComplication(MobileBatteryComplicationService::class.java)
                                TileService.getUpdater(this@MobileListener).requestUpdate(PhoneBatteryTileService::class.java)
                            }
                        }
                        ACTIVE_SYNC_PATH -> {
                            val state = DataMapItem.fromDataItem(dataEvent.dataItem).dataMap.getBoolean(ACTIVE_SYNC_KEY)
                            ioScope.launch {
                                dataStore.updateData { it.copy(activeSync = state) }
                            }
                        }
                        URI -> {
                            processDataItem(DataMapItem.fromDataItem(dataEvent.dataItem).dataMap)
                        }
                        CALENDAR_EVENTS_PATH -> {
                            val dataMap = dataEvent.dataItem.data?.let { DataMap.fromByteArray(it) }
                            val eventDataMaps = dataMap?.getDataMapArrayList(CALENDAR_EVENTS_KEY) ?: emptyList()
                            // Convert each DataMap back to CalendarEvent
                            val events = eventDataMaps.map { CalendarEvent.fromDataMap(it) }
                            // TODO: we're taking only first 200 items to reduce overhead (take(200))
                            ioScope.launch {
                                dataStore.updateData { it.copy(calendarEvents = events.take(200)) }
                                updateCalendarComplications()
                            }
                        }
                    }
                }
                DataEvent.TYPE_DELETED -> {
                //Log.v(TAG, "Data deleted : " + dataEvent.dataItem.toString())
                    ioScope.launch {
                        dataStore.updateData {
                            it.copy(
                                phoneBatteryLevel = 0,
                                phoneIsCharging = false,
                                phoneIsConnected = false,
                                afterMobileResult = false,
                                lastUpdate = System.currentTimeMillis()
                            )
                        }
                        //Log.d(TAG, "Phone Companion Uninstalled!")
                        updateComplication(MobileBatteryComplicationService::class.java)
                    }
            }
                else -> { Log.e(TAG, "Unknown data event Type = " + dataEvent.type) }
            }
            /** Release dataEvents after processing them */
            dataEvents.release()
        }
    }

    override fun onCapabilityChanged(capabilityInfo: CapabilityInfo) {
        super.onCapabilityChanged(capabilityInfo)
        if (capabilityInfo.name == BuildConfig.CAPABILITY_MOBILE_APP){
            //Log.d("MobileListener", "capabilityInfo.name matches ${BuildConfig.CAPABILITY_MOBILE_APP}")
            if (capabilityInfo.nodes.size > 0){
                //Log.d("MobileListener", "capability ${capabilityInfo.name} connected!")
                capabilityInfo.nodes.firstOrNull()?.let { node ->
                    ioScope.launch {
                        dataStore.updateData { it.copy(nodeName = node.displayName ) }
                    }
                }
                // we are sending battery level now when change is detected on mobile app
                //sendPhoneBatteryRequest(0,dataClient, forceUpdate = true)
            }
            else {
                //Log.d("MobileListener", "capability ${capabilityInfo.name} disconnected!")
                ioScope.launch {
                    dataStore.updateData {
                        it.copy(
                            phoneIsConnected = false,
                            afterMobileResult = true,
                        )
                    }
                    notificationsList = mutableListOf()
                    updateComplication(MobileBatteryComplicationService::class.java)
                    updateComplication(NotificationsIconsComplicationService::class.java)
                }
            }
        }
    }

    private fun processDataItem(dataMap: DataMap) {
        ioScope.launch {
            mutex.withLock {
                val lastUpdateTime = preferences.first().lastNotificationsUpdateTime
                //Log.i("MobileListener", "processDataItem, lastNotificationsUpdateTime: $lastUpdateTime")
                val updateTime = dataMap.getLong(NOTIFICATIONS_UPDATE_KEY)
                if (updateTime < lastUpdateTime){
                    //Log.i("MobileListener", "Notifications Update Time is lower than Last Update Time, discard & return!")
                    return@launch
                }

                //Log.i("MobileListener", "processDataItem, setting new last update time $updateTime")
                dataStore.updateData { it.copy(lastNotificationsUpdateTime = updateTime) }

                val byteArrayList = mutableListOf<ByteArray>()
                var i = 0
                while (true) {
                    val byteArray = dataMap.getByteArray("icon$i") ?: break
                    byteArrayList.add(byteArray)
                    i++
                }
                notificationsList = byteArrayList
                //Log.i("MobileListener", "Icons count: ${byteArrayList.size}")
                updateComplication(NotificationsIconsComplicationService::class.java)
            }
        }
    }

    companion object {
        fun sendPhoneBatteryRequest (lastUpdateTime: Long, dataClient: DataClient, forceUpdate: Boolean) {
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastUpdateTime >= 5000) {
                val request = PutDataMapRequest.create(REQUEST_PATH).apply{
                    dataMap.putLong(REQUEST_KEY, currentTime)
                    dataMap.putBoolean(FORCE_UPDATE_KEY, forceUpdate)}
                    .asPutDataRequest()
                    .setUrgent()

                dataClient.putDataItem(request)
            }
            else
                Log.e(TAG, "Too many updates")
        }
        fun sendCalendarRequest (currentTime: Long, dataClient: DataClient) {
            // Send request only every 30 minutes to avoid looping requests
            if (currentTime - lastCalendarRequestTime > 1800000 ){
                lastCalendarRequestTime = currentTime
                val request = PutDataMapRequest.create(CALENDAR_REQUEST_PATH).apply {
                    dataMap.putLong("calendarUpdate", currentTime) }
                    .asPutDataRequest()
                    .setUrgent()

                dataClient.putDataItem(request)
            }
        }
        fun sendNotificationsRequest(dataClient: DataClient){
            val request = PutDataMapRequest.create(NOTIFICATIONS_REQUEST_PATH).apply {
                dataMap.putLong("notificationsUpdate", System.currentTimeMillis()) }
                .asPutDataRequest()
                .setUrgent()

            dataClient.putDataItem(request)
        }
    }
}

