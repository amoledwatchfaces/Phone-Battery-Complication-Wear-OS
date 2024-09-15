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

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class DataStoreRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
): Repository {

    val preferencesVersion: Flow<Int> = dataStore.data.map { prefs ->
        prefs[PREFS_VERSION]?: 6
    }
    val activeSync: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[ACTIVE_SYNC] ?: false
    }
    val nodeName: Flow<String> = dataStore.data.map { prefs ->
        prefs[NODE_NAME] ?: "Disconnected"
    }
    val tempUnit: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[TEMP_UNIT] ?: true
    }
    val percentage: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[PERCENTAGE] ?: true
    }
    val isTileSet: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[IS_TILE_SET] ?: false
    }
    val notificationsIconType: Flow<Int> = dataStore.data.map { prefs ->
        prefs[NOTIF_ICON_TYPE] ?: 1 // 1 = PHOTO, 0 = ICON, Default for this app is 1
    }
    val byteArrayMutableListJsonString: Flow<String> = dataStore.data.map {
        it[BITMAP_LIST_KEY]?: ""
    }

    suspend fun storeByteArrayMutableList(byteArrayMutableList: String) {
        dataStore.edit {
            it[BITMAP_LIST_KEY] = byteArrayMutableList
        }
    }
    suspend fun storeNotifIconType(type: Int) {
        dataStore.edit { prefs ->
            prefs[NOTIF_ICON_TYPE] = type
        }
    }
    suspend fun storeNodeName(node: String) {
        dataStore.edit { prefs ->
            prefs[NODE_NAME] = node
        }
    }
    suspend fun storeTempUnit(tempUnit: Boolean) {
        dataStore.edit { prefs ->
            prefs[TEMP_UNIT] = tempUnit
        }
    }
    suspend fun setActiveSyncState(state: Boolean) {
        dataStore.edit { prefs ->
            prefs[ACTIVE_SYNC] = state
        }
    }
    suspend fun storePercentage(percentage: Boolean) {
        dataStore.edit { prefs ->
            prefs[PERCENTAGE] = percentage
        }
    }
    suspend fun storeTileSetState(set: Boolean) {
        dataStore.edit { prefs ->
            prefs[IS_TILE_SET] = set
        }
    }

    companion object {
        private val NODE_NAME = stringPreferencesKey("node_name")
        private val TEMP_UNIT = booleanPreferencesKey("temp_unit")
        private val PERCENTAGE = booleanPreferencesKey("percentage")
        private val IS_TILE_SET = booleanPreferencesKey("is_tile_set")
        private val BITMAP_LIST_KEY = stringPreferencesKey("notify_bitmap_list")
        private val NOTIF_ICON_TYPE = intPreferencesKey("notifications_icon_type")
        private val ACTIVE_SYNC = booleanPreferencesKey("active_sync")

        val PREFS_VERSION = intPreferencesKey(name = "preferencesVersion")
    }
}

