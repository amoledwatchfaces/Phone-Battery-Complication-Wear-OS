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
import androidx.preference.PreferenceManager
import com.google.android.gms.tasks.Task
import com.google.android.gms.wearable.*
import com.weartools.phonebattcomp.complication.MobileBatteryComplicationService.Companion.updateBatteryComplication

private const val BATTERY_PATH = "/battery-level"
private const val BATTERY_KEY= "battery-key"
private const val REQUEST_PATH = "/request"
private const val REQUEST_KEY = "request-key"

class MobileListener : WearableListenerService() {

    private val preferences by lazy { PreferenceManager.getDefaultSharedPreferences(this)}

    @SuppressLint("ApplySharedPref", "VisibleForTests")
    override fun onDataChanged(dataEvents: DataEventBuffer) {
        if (Log.isLoggable(TAG, Log.DEBUG)) { Log.d(TAG, "onDataChanged: $dataEvents") }

        dataEvents.forEach { dataEvent ->
            when (dataEvent.type) {
                DataEvent.TYPE_CHANGED -> {
                    when (dataEvent.dataItem.uri.path) {
                        BATTERY_PATH -> {
                            val dataMapItem = DataMapItem.fromDataItem(dataEvent.dataItem)
                            val level = dataMapItem.dataMap.getInt(BATTERY_KEY)
                            preferences.edit()
                                .putInt(getString(R.string.key_pref_mobile_battery_level), level)
                                .putBoolean(getString(R.string.key_pref_has_mobile_app), true)
                                .putBoolean(getString(R.string.key_pref_after_mobile_result), true)
                                .putBoolean(getString(R.string.key_pref_connected), true)
                                .putLong(getString(R.string.key_pref_last_update),System.currentTimeMillis()) //TODO: TEST
                                .commit()
                            Log.d(TAG, "Received Phone Battery Level: $level")
                            updateBatteryComplication(this)
                        }
                    }
                }
                DataEvent.TYPE_DELETED -> {
                Log.v(TAG, "Data deleted : " + dataEvent.dataItem.toString())
                preferences.edit()
                    .putInt(getString(R.string.key_pref_mobile_battery_level), 0)
                    .putBoolean(getString(R.string.key_pref_has_mobile_app), false)
                    .putBoolean(getString(R.string.key_pref_after_mobile_result), false)
                    .putBoolean(getString(R.string.key_pref_connected), false)
                    .putLong(getString(R.string.key_pref_last_update),System.currentTimeMillis()) //TODO: TEST
                    .commit()
                updateBatteryComplication(this)
            }
                else -> { Log.e(ContentValues.TAG, "Unknown data event Type = " + dataEvent.type) }
        }
        }
    }

    override fun onCapabilityChanged(capabilityInfo: CapabilityInfo) {
        super.onCapabilityChanged(capabilityInfo)
        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        val mobileApp = preferences.getBoolean(getString(R.string.key_pref_has_mobile_app), false)
        val editor = preferences.edit()
        if (capabilityInfo.nodes.size > 0 && mobileApp) {

                    editor.putBoolean(getString(R.string.key_pref_connected), true)
                          .putBoolean(getString(R.string.key_pref_after_mobile_result), false)
                          .apply()
        }
        else {
            editor.putBoolean(getString(R.string.key_pref_connected), false)
                .putBoolean(getString(R.string.key_pref_after_mobile_result), true)
                .apply()
        }
        Log.d(TAG, "Capability changed: " + capabilityInfo.nodes.size)
        updateBatteryComplication(this)
    }

    companion object {
        @SuppressLint("VisibleForTests")
        fun sendPhoneBatteryRequest (lastUpdateTime: Long, context: Context) {
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastUpdateTime >= 5000) {
                val request = PutDataMapRequest.create(REQUEST_PATH).apply{
                    dataMap.putLong(REQUEST_KEY, currentTime) }
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
        private val TAG = MobileListener::class.java.simpleName
    }
}