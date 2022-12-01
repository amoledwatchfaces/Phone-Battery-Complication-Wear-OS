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

import android.util.Log
import androidx.preference.PreferenceManager
import com.google.android.gms.wearable.CapabilityInfo
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService
import com.weartools.phonebattcomp.MobileBatteryComplicationService.Companion.updateBatteryComplication

class MobileBatteryListener : WearableListenerService() {

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        dataEvents.release()
    }

    override fun onMessageReceived(messageEvent: MessageEvent) {

        val path = messageEvent.path
        Log.d(TAG, path)
        if (path.startsWith("/battery_level")) {
            val level = path.replace("\\D".toRegex(), "").toInt()
            val preferences = PreferenceManager.getDefaultSharedPreferences(this)
            val editor = preferences.edit()
            editor.putInt(getString(R.string.key_pref_mobile_battery_level), level)
            editor.putBoolean(getString(R.string.key_pref_has_mobile_app), true)
            editor.putBoolean(getString(R.string.key_pref_after_mobile_result), true)
            editor.putBoolean(getString(R.string.key_pref_connected), true)
            editor.apply()
            updateBatteryComplication(this)
        }
    }

    override fun onCapabilityChanged(capabilityInfo: CapabilityInfo) {
        super.onCapabilityChanged(capabilityInfo)
        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        val editor = preferences.edit()
        if (capabilityInfo.nodes.size > 0) {
            for (node in capabilityInfo.nodes) {
                if (node.isNearby) {editor.putBoolean(getString(R.string.key_pref_connected), true)
                                    editor.putBoolean(getString(R.string.key_pref_after_mobile_result), false)}

                else {editor.putBoolean(getString(R.string.key_pref_connected), false)
                      editor.putBoolean(getString(R.string.key_pref_after_mobile_result), true)}
            }
        } else {
            editor.putBoolean(getString(R.string.key_pref_connected), false)
            editor.putBoolean(getString(R.string.key_pref_after_mobile_result), true)
        }
        editor.apply()
        Log.d(TAG, "capability changed: " + capabilityInfo.nodes.size)
        updateBatteryComplication(this)
    }

    companion object {
        private val TAG = MobileBatteryListener::class.java.simpleName
    }
}