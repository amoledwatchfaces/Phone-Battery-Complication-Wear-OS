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
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "passive_data")

class DataRepository(private val context: Context) {

    val nodeName: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[NODE_NAME] ?: "Not connected"
    }

    val tempUnit: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[TEMP_UNIT] ?: true
    }

    val batteryLevel: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[BATTERY_LEVEL] ?: 0
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

    suspend fun storeBatteryLevel(batteryLevel: Int) {
        context.dataStore.edit { prefs ->
            prefs[BATTERY_LEVEL] = batteryLevel
        }
    }

    companion object {
        private val NODE_NAME = stringPreferencesKey("node_name")
        private val TEMP_UNIT = booleanPreferencesKey("temp_unit")
        private val BATTERY_LEVEL = intPreferencesKey("battery_level")
    }
}

