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
import android.content.Context
import android.util.Log
import androidx.wear.tiles.TileService
import com.google.android.gms.tasks.Task
import com.google.android.gms.wearable.*
import com.weartools.phonebattcomp.complication.MobileBatteryComplicationService
import com.weartools.phonebattcomp.data.DataRepository
import com.weartools.phonebattcomp.tile.PhoneBatteryTileService
import com.weartools.phonebattcomp.utils.updateComplication
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.util.Base64

private const val BATTERY_PATH = "/battery-level"
private const val BATTERY_KEY= "battery-key"
private const val REQUEST_PATH = "/request"
private const val REQUEST_KEY = "request-key"
private const val FORCE_UPDATE_KEY = "force-update-key"
private const val URI = "/foobar"
private const val TAG = "MobileListener::"

@SuppressLint("VisibleForTests")
class MobileListener : WearableListenerService() {

    private val repository by lazy { DataRepository(this) }

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        if (Log.isLoggable(TAG, Log.DEBUG)) { Log.d(TAG, "onDataChanged: $dataEvents") }

        dataEvents.forEach { dataEvent ->
            when (dataEvent.type) {
                DataEvent.TYPE_CHANGED -> {
                    when (dataEvent.dataItem.uri.path) {
                        BATTERY_PATH -> {
                            val dataMapItem = DataMapItem.fromDataItem(dataEvent.dataItem)
                            val level = dataMapItem.dataMap.getInt(BATTERY_KEY)
                            runBlocking { repository.storeResponse(
                                batteryLevel = level,
                                hasMobileApp = true,
                                afterMobileResult = true,
                                isConnected = true,
                                lastUpdate = System.currentTimeMillis()
                            ) }
                            //Log.d(TAG, "Received Phone Battery Level: $level")
                            this.updateComplication(MobileBatteryComplicationService::class.java)
                            TileService.getUpdater(this).requestUpdate(PhoneBatteryTileService::class.java)
                        }
                        URI -> {

                            processDataItem(dataEvent.dataItem)
                            dataEvents.release()
                        }
                    }
                }
                DataEvent.TYPE_DELETED -> {
                //Log.v(TAG, "Data deleted : " + dataEvent.dataItem.toString())
                    runBlocking { repository.storeResponse(
                        batteryLevel = 0,
                        hasMobileApp = false,
                        afterMobileResult = false,
                        isConnected = false,
                        lastUpdate = System.currentTimeMillis()
                    ) }
                //Log.d(TAG, "Phone Companion Uninstalled!")
                    this.updateComplication(MobileBatteryComplicationService::class.java)
            }
                else -> { Log.e(ContentValues.TAG, "Unknown data event Type = " + dataEvent.type) }
        }
        }
    }

    override fun onCapabilityChanged(capabilityInfo: CapabilityInfo) {
        super.onCapabilityChanged(capabilityInfo)
        val hasMobileApp = runBlocking { repository.hasMobileApp.first() }
        if (capabilityInfo.nodes.size > 0 && hasMobileApp) {
            sendPhoneBatteryRequest(0,this, forceUpdate = true)
        }
        else {
            runBlocking {
                repository.storeConnection(false)
                repository.storeResult(true)
            }
            this.updateComplication(MobileBatteryComplicationService::class.java)
        }
        //Log.d(TAG, "Capability changed: " + capabilityInfo.nodes.size)
    }


    private fun processDataItem(dataItem: DataItem) {
        val newBitmaps = mutableListOf<ByteArray>()
        val dataMapItem = DataMapItem.fromDataItem(dataItem)
        var i = 0
        while (true) {
            val byteArray = dataMapItem.dataMap.getByteArray("icon$i") ?: break
            newBitmaps.add(byteArray)
            i++
        }
        //Log.w(TAG, "Bitmap list size: ${newBitmaps.size}")

        val concatenatedString = newBitmaps.joinToString("|") { Base64.getEncoder().encodeToString(it) }

        runBlocking {repository.storeByteArrayMutableList(concatenatedString)}
    }

    companion object {
        @SuppressLint("VisibleForTests")
        fun sendPhoneBatteryRequest (lastUpdateTime: Long, context: Context, forceUpdate: Boolean) {
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastUpdateTime >= 5000) {
                val request = PutDataMapRequest.create(REQUEST_PATH).apply{
                    dataMap.putLong(REQUEST_KEY, currentTime)
                    dataMap.putBoolean(FORCE_UPDATE_KEY, forceUpdate)}
                    .asPutDataRequest()
                    .setUrgent()

                val dataItemTask: Task<DataItem> = Wearable.getDataClient(context).putDataItem(request)
                dataItemTask
                    .addOnSuccessListener { dataItem -> Log.d(TAG,"Sending Phone Battery request was successful: $dataItem") }
                    .addOnFailureListener { e -> Log.e(TAG,"Sending request task failed!: $e") }
                    .addOnCompleteListener{task -> Log.d(TAG,"Sending request Task complete!: $task")}
            }
            else
                Log.e(TAG, "Too many updates")
        }
    }
}