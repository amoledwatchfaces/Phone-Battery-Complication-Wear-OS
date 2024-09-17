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
import android.content.ContentValues
import android.util.Log
import androidx.wear.tiles.TileService
import com.google.android.gms.wearable.CapabilityInfo
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataItem
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.WearableListenerService
import com.weartools.phonebattcomp.complication.MobileBatteryComplicationService
import com.weartools.phonebattcomp.complication.NotificationsIconsComplicationService
import com.weartools.phonebattcomp.complication.PhoneBatteryState.afterMobileResult
import com.weartools.phonebattcomp.complication.PhoneBatteryState.lastUpdate
import com.weartools.phonebattcomp.complication.PhoneBatteryState.phoneBatteryLevel
import com.weartools.phonebattcomp.complication.PhoneBatteryState.phoneIsCharging
import com.weartools.phonebattcomp.complication.PhoneBatteryState.phoneIsConnected
import com.weartools.phonebattcomp.complication.notificationsByteArrayList
import com.weartools.phonebattcomp.data.DataStoreRepository
import com.weartools.phonebattcomp.tile.PhoneBatteryTileService
import com.weartools.phonebattcomp.utils.updateComplication
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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
private const val TAG = "MobileListener::"

@AndroidEntryPoint
class MobileListener : WearableListenerService() {

    @Inject lateinit var dataRepository: DataStoreRepository
    @Inject lateinit var dataClient: DataClient

    private val ioScope = CoroutineScope(Dispatchers.IO)

    override fun onDataChanged(dataEvents: DataEventBuffer) {

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

                            phoneBatteryLevel = dataMapItem.dataMap.getInt(BATTERY_KEY)
                            phoneIsCharging = dataMapItem.dataMap.getBoolean(IS_CHARGING_KEY)
                            phoneIsConnected = true
                            afterMobileResult = true
                            lastUpdate.value = System.currentTimeMillis()

                            //Log.d(TAG, "Received Phone Battery Level: $level")
                            updateComplication(MobileBatteryComplicationService::class.java)
                            TileService.getUpdater(this).requestUpdate(PhoneBatteryTileService::class.java)
                        }
                        ACTIVE_SYNC_PATH -> {
                            val state = DataMapItem.fromDataItem(dataEvent.dataItem).dataMap.getBoolean(ACTIVE_SYNC_KEY)
                            ioScope.launch {
                                dataRepository.setActiveSyncState(state)
                            }
                        }
                        URI -> {
                            processDataItem(dataEvent.dataItem)
                        }
                    }
                }
                DataEvent.TYPE_DELETED -> {
                //Log.v(TAG, "Data deleted : " + dataEvent.dataItem.toString())
                    phoneBatteryLevel = 0
                    phoneIsCharging = false
                    phoneIsConnected = false
                    afterMobileResult = false
                    lastUpdate.value = System.currentTimeMillis()

                    //Log.d(TAG, "Phone Companion Uninstalled!")
                    updateComplication(MobileBatteryComplicationService::class.java)
            }
                else -> { Log.e(ContentValues.TAG, "Unknown data event Type = " + dataEvent.type) }
            }
            /** Release dataEvents after processing them */
            dataEvents.release()
        }
    }

    override fun onCapabilityChanged(capabilityInfo: CapabilityInfo) {
        super.onCapabilityChanged(capabilityInfo)
        ioScope.launch{
            if (capabilityInfo.nodes.size > 0) { // Removed condition hasMobileApp?
                if (capabilityInfo.name == BuildConfig.CAPABILITY_MOBILE_APP) {
                    capabilityInfo.nodes.firstOrNull()?.displayName?.let { dataRepository.storeNodeName(it) }
                }
                sendPhoneBatteryRequest(0,dataClient, forceUpdate = true)
            }
            else {
                phoneIsConnected = false
                afterMobileResult = true
                updateComplication(MobileBatteryComplicationService::class.java)
            }
        }
        //Log.d(TAG, "Capability changed: " + capabilityInfo.nodes.size)
    }

    private fun processDataItem(dataItem: DataItem) {
        val dataMapItem = DataMapItem.fromDataItem(dataItem)
        val byteArrayList = mutableListOf<ByteArray>()
        var i = 0
        while (true) {
            val byteArray = dataMapItem.dataMap.getByteArray("icon$i") ?: break
            byteArrayList.add(byteArray)
            i++
        }
        notificationsByteArrayList = byteArrayList
        updateComplication(NotificationsIconsComplicationService::class.java)
    }

    companion object {

        @SuppressLint("VisibleForTests")
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
    }
}