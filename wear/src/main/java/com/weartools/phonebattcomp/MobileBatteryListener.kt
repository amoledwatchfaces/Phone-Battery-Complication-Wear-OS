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
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService
import com.weartools.phonebattcomp.MobileBatteryComplicationService.Companion.updateBatteryComplication

class MobileBatteryListener : WearableListenerService() {

    override fun onMessageReceived(messageEvent: MessageEvent) {

        val path = messageEvent.path
        Log.d(TAG, path)
        if (path.startsWith("/battery_level")) {
            val level = path.replace("\\D".toRegex(), "").toInt()
            val preferences = PreferenceManager.getDefaultSharedPreferences(this)
            preferences.edit().putInt(getString(R.string.key_pref_mobile_battery_level), level)
                              .putBoolean(getString(R.string.key_pref_has_mobile_app), true)
                              .putBoolean(getString(R.string.key_pref_after_mobile_result), true)
                              .putBoolean(getString(R.string.key_pref_connected), true)
                              .putLong(getString(R.string.key_pref_last_update),System.currentTimeMillis()) //TODO: TEST
                              .apply()
            updateBatteryComplication(this)
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
        private val TAG = MobileBatteryListener::class.java.simpleName
    }
}