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

import android.content.ContentValues
import android.util.Log
import androidx.preference.PreferenceManager
import com.google.android.gms.wearable.*
import com.weartools.phonebattcomp.MobileBatteryComplicationService.Companion.updateBatteryComplication

private const val BATTERY_PATH = "/battery_level"
private const val BATTERY_KEY= "battery_level"

class MobileListener : WearableListenerService() {

    override fun onDataChanged(dataEventBuffer: DataEventBuffer) {
        for (event in dataEventBuffer) {
            if (event.type == DataEvent.TYPE_CHANGED) {
                val path = event.dataItem.uri.path
                if (path == BATTERY_PATH) {
                    val dataMapItem = DataMapItem.fromDataItem(event.dataItem)
                    val level = dataMapItem.dataMap.getInt(BATTERY_KEY)
                    Log.d(TAG, "Received Phone Battery Level: $level")
                    val preferences = PreferenceManager.getDefaultSharedPreferences(this)
                    preferences.edit().putInt(getString(R.string.key_pref_mobile_battery_level), level)
                        .putBoolean(getString(R.string.key_pref_has_mobile_app), true)
                        .putBoolean(getString(R.string.key_pref_after_mobile_result), true)
                        .putBoolean(getString(R.string.key_pref_connected), true)
                        .putLong(getString(R.string.key_pref_last_update),System.currentTimeMillis()) //TODO: TEST
                        .apply()
                    updateBatteryComplication(this)

                }
                else { Log.e(ContentValues.TAG, "Unrecognized path: $path") }
            }
            else if (event.type == DataEvent.TYPE_DELETED) { Log.v(ContentValues.TAG, "Data deleted : " + event.dataItem.toString()) }
            else { Log.e(ContentValues.TAG, "Unknown data event Type = " + event.type) }
        }
    }

    override fun onCapabilityChanged(capabilityInfo: CapabilityInfo) {
        super.onCapabilityChanged(capabilityInfo)
        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        val editor = preferences.edit()
        if (capabilityInfo.nodes.size > 0) {
            for (node in capabilityInfo.nodes) {
                if (node.isNearby) {
                    editor.putBoolean(getString(R.string.key_pref_connected), true)
                          .putBoolean(getString(R.string.key_pref_after_mobile_result), false)
                          .apply() }
                else {
                    editor.putBoolean(getString(R.string.key_pref_after_mobile_result), false)
                          .apply()
                     }
            }
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
        private val TAG = MobileListener::class.java.simpleName
    }
}