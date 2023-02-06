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
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.android.gms.wearable.*

private const val REQUEST_PATH = "/request"
private const val REQUEST_KEY = "request"
private const val BATTERY_PATH = "/battery_level"
private const val BATTERY_KEY= "battery_level"

class WearListener : WearableListenerService() {

    override fun onDataChanged(dataEventBuffer: DataEventBuffer) {

        for (event in dataEventBuffer) {
            if (event.type == DataEvent.TYPE_CHANGED) {
                val path = event.dataItem.uri.path
                if (path == REQUEST_PATH) {
                    val dataMapItem = DataMapItem.fromDataItem(event.dataItem)
                    val requestTime = dataMapItem.dataMap.getLong(REQUEST_KEY)
                    Log.v(TAG, "Received Phone Battery request with updateTime: $requestTime")
                    val level = batteryLevel
                    sendBatteryLevel(level) }
                else { Log.e(TAG, "Unrecognized path: $path") } }
            else if (event.type == DataEvent.TYPE_DELETED) { Log.v(TAG, "Data deleted : " + event.dataItem.toString()) }
            else { Log.e(TAG, "Unknown data event Type = " + event.type) }
        }
    }

    private val batteryLevel: Int
        get() {
            val batteryStatus = registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
            val scale = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
            return 100 * level / scale
        }

    private fun sendBatteryLevel (level: Int) {
        val dataMap = PutDataMapRequest.create(BATTERY_PATH)
        dataMap.dataMap.putInt(BATTERY_KEY, level)
        val request = dataMap.asPutDataRequest()
        request.setUrgent()

        val dataItemTask: Task<DataItem> = Wearable.getDataClient(this).putDataItem(request)
        dataItemTask
            .addOnSuccessListener { dataItem -> Log.d(TAG,"Sending Phone Battery level ($level) was successful: $dataItem") }
            .addOnFailureListener { e -> Log.e(TAG,"Sending phone battery level FAILED with error: $e") }
    }
}