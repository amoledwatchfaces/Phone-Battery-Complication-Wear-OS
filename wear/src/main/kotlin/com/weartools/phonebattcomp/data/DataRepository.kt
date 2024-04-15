/*
 * Copyright 2022 The Android Open Source Project
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
package com.weartools.phonebattcomp.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.weartools.phonebattcomp.complication.NotificationsIconsComplicationService
import com.weartools.phonebattcomp.utils.updateComplication
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "passive_data")

class DataRepository(private val context: Context) {

    val activeSync: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[ACTIVE_SYNC] ?: false
    }

    val isCharging: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[IS_CHARGING] ?: false
    }


    val nodeName: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[NODE_NAME] ?: "Not connected"
    }

    val tempUnit: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[TEMP_UNIT] ?: true
    }
    val percentage: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[PERCENTAGE] ?: true
    }

    val batteryLevel: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[BATTERY_LEVEL] ?: 0
    }

    val hasMobileApp: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[HAS_MOBILE_APP] ?: false
    }
    val afterMobileResult: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[AFTER_MOBILE_RESULT] ?: false
    }
    val isConnected: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[IS_CONNECTED] ?: false
    }
    val lastUpdate: Flow<Long> = context.dataStore.data.map { prefs ->
        prefs[LAST_UPDATE] ?: 0
    }
    val isTileSet: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[IS_TILE_SET] ?: false
    }

    val byteArrayMutableListJsonString: Flow<String> = context.dataStore.data.map {
        it[BITMAP_LIST_KEY]?: ""
    }
    suspend fun storeByteArrayMutableList(byteArrayMutableList: String) {
        context.dataStore.edit {
            it[BITMAP_LIST_KEY] = byteArrayMutableList
        }
        context.updateComplication(NotificationsIconsComplicationService::class.java)
    }


    suspend fun storeNodeName(node: String) {
        context.dataStore.edit { prefs ->
            prefs[NODE_NAME] = node
        }
    }

    suspend fun storeTempUnit(tempUnit: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[TEMP_UNIT] = tempUnit
        }
    }

    suspend fun setActiveSyncState(state: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[ACTIVE_SYNC] = state
        }
    }

    suspend fun setChargingState(state: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[IS_CHARGING] = state
        }
    }

    suspend fun storePercentage(percentage: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[PERCENTAGE] = percentage
        }
    }

    suspend fun storeBatteryLevel(batteryLevel: Int) {
        context.dataStore.edit { prefs ->
            prefs[BATTERY_LEVEL] = batteryLevel
        }
    }

    suspend fun storeConnection(isConnected: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[IS_CONNECTED] = isConnected
        }
    }
    suspend fun storeResult(afterMobileResult: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[AFTER_MOBILE_RESULT] = afterMobileResult
        }
    }
    suspend fun storeMobileApp(hasMobileApp: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[HAS_MOBILE_APP] = hasMobileApp
        }
    }
    suspend fun storeTileSetState(set: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[IS_TILE_SET] = set
        }
    }

    suspend fun storeResponse(
        batteryLevel: Int,
        hasMobileApp: Boolean,
        afterMobileResult: Boolean,
        isConnected: Boolean,
        lastUpdate: Long,
        isCharging: Boolean
    ) {
        context.dataStore.edit { prefs ->
            prefs[BATTERY_LEVEL] = batteryLevel
            prefs[HAS_MOBILE_APP] = hasMobileApp
            prefs[AFTER_MOBILE_RESULT] = afterMobileResult
            prefs[IS_CONNECTED] = isConnected
            prefs[LAST_UPDATE] = lastUpdate
            prefs[IS_CHARGING] = isCharging
        }
    }

    companion object {
        private val NODE_NAME = stringPreferencesKey("node_name")
        private val TEMP_UNIT = booleanPreferencesKey("temp_unit")
        private val PERCENTAGE = booleanPreferencesKey("percentage")
        private val BATTERY_LEVEL = intPreferencesKey("battery_level")

        private val HAS_MOBILE_APP = booleanPreferencesKey("has_mobile_app")
        private val AFTER_MOBILE_RESULT = booleanPreferencesKey("after_mobile_result")
        private val IS_CONNECTED = booleanPreferencesKey("is_connected")
        private val LAST_UPDATE = longPreferencesKey("last_update_time")

        private val IS_TILE_SET = booleanPreferencesKey("is_tile_set")

        private val BITMAP_LIST_KEY = stringPreferencesKey("notify_bitmap_list")
        private val ACTIVE_SYNC = booleanPreferencesKey("active_sync")
        private val IS_CHARGING = booleanPreferencesKey("is_charging")
    }
}

