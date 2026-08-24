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
import android.widget.Toast
import androidx.concurrent.futures.await
import androidx.core.net.toUri
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
import com.weartools.phonebattcomp.widget.PhoneBatteryWidget
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

@HiltViewModel
class MainViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dataStore: DataStore<UserPreferences>,
    preferences: UserPreferencesRepository,
) : ViewModel(){

    private val _isPreferencesLoaded = MutableStateFlow(false)
    val isPreferencesLoaded = _isPreferencesLoaded.asStateFlow()

    val preferences: StateFlow<UserPreferences> = preferences
        .getPreferences()
        .onEach { _isPreferencesLoaded.value = true }
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

    fun setUseDynamicColor(useDynamicColor: Boolean){
        viewModelScope.launch {
            dataStore.updateData { it.copy(useDynamicColor = useDynamicColor) }
            PhoneBatteryWidget(dataStore).triggerUpdateAll(context)
        }
    }
    fun setCrashlytics(enabled: Boolean) {
        viewModelScope.launch {
            dataStore.updateData { it.copy(crashlytics = enabled, crashlyticsNoticeAccepted = true) }
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
                .setData("https://amoledwatchfaces.com/phonebattcomp".toUri())

            try {
                RemoteActivityHelper(context).startRemoteActivity(targetIntent = launchIntent,targetNodeId = null).await()
                Toast.makeText(context,"Check more settings on your phone",Toast.LENGTH_LONG).show()
            } catch (_: CancellationException) {
                // Request was canceled normally
            } catch (_: Throwable) {
                Toast.makeText(context,"Companion app not reachable",Toast.LENGTH_LONG).show()
            }
        }
    }

}


