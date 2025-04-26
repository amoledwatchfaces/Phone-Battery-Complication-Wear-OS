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
package com.weartools.phonebattcomp

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.concurrent.futures.await
import androidx.datastore.core.DataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.wear.remote.interactions.RemoteActivityHelper
import androidx.wear.watchface.complications.datasource.ComplicationDataSourceUpdateRequester
import com.weartools.phonebattcomp.complication.MobileBatteryComplicationService
import com.weartools.phonebattcomp.complication.NotificationsIconsComplicationService
import com.weartools.phonebattcomp.complication.WatchBatteryComplicationService
import com.weartools.phonebattcomp.complication.WatchTempComplicationService
import com.weartools.phonebattcomp.data.UserPreferences
import com.weartools.phonebattcomp.data.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

@HiltViewModel
class MainViewModel @Inject constructor(
    private val dataStore: DataStore<UserPreferences>,
    preferences: UserPreferencesRepository,
) : ViewModel(){

    val preferences: StateFlow<UserPreferences> = preferences
        .getPreferences()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = UserPreferences()
        )


    fun toggleEnabled(context: Context) {
        viewModelScope.launch {
            val newEnabledStatus = !preferences.value.tempUnit
            dataStore.updateData { it.copy(tempUnit = newEnabledStatus) }
            updateComplication(context = context, WatchTempComplicationService::class.java)
        }
    }
    fun togglePercentage(context: Context) {
        viewModelScope.launch {
            val newEnabledStatus = !preferences.value.percentage
            dataStore.updateData { it.copy(percentage = newEnabledStatus) }
            updateComplication(context = context, MobileBatteryComplicationService::class.java)
            updateComplication(context = context, WatchBatteryComplicationService::class.java)
        }
    }
    fun toggleMaterialSymbols(value: Boolean, context: Context) {
        viewModelScope.launch {
            dataStore.updateData { it.copy(materialSymbols = value) }
            updateComplication(context = context, MobileBatteryComplicationService::class.java)
            updateComplication(context = context, WatchBatteryComplicationService::class.java)
        }
    }
    fun toggleChargingSymbol(value: Boolean, context: Context) {
        viewModelScope.launch {
            dataStore.updateData { it.copy(chargingSymbolInsideIcon = value) }
            updateComplication(context = context, MobileBatteryComplicationService::class.java)
            updateComplication(context = context, WatchBatteryComplicationService::class.java)
        }
    }
    fun storeNotificationIconType(context: Context, type: Int) {
        viewModelScope.launch {
            dataStore.updateData { it.copy(notificationsIconType = type) }
            updateComplication(context = context, NotificationsIconsComplicationService::class.java)
        }
    }

    fun updateComplication(context: Context, clazz: Class<*>){
        ComplicationDataSourceUpdateRequester.create(
            context.applicationContext,
            ComponentName(context.applicationContext, clazz)
        ).run { requestUpdateAll() }
    }

    fun openExperimentalSettings(context: Context) {
        viewModelScope.launch {
            // Intent to launch your MainActivity
            val launchIntent = Intent(Intent.ACTION_VIEW)
                .addCategory(Intent.CATEGORY_BROWSABLE)
                .setData(Uri.parse("https://amoledwatchfaces.com/phonebattcomp"))

            try {
                RemoteActivityHelper(context).startRemoteActivity(targetIntent = launchIntent,targetNodeId = null).await()
                Toast.makeText(context,"Check more settings on your phone",Toast.LENGTH_LONG).show()
            } catch (cancellationException: CancellationException) {
                // Request was cancelled normally
            } catch (throwable: Throwable) {
                Toast.makeText(context,"Companion app not reachable",Toast.LENGTH_LONG).show()
            }
        }
    }

}


