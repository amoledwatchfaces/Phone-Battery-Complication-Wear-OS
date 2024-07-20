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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.wear.remote.interactions.RemoteActivityHelper
import androidx.wear.watchface.complications.datasource.ComplicationDataSourceUpdateRequester
import com.weartools.phonebattcomp.complication.MobileBatteryComplicationService
import com.weartools.phonebattcomp.complication.NotificationsIconsComplicationService
import com.weartools.phonebattcomp.complication.WatchBatteryComplicationService
import com.weartools.phonebattcomp.complication.WatchTempComplicationService
import com.weartools.phonebattcomp.data.DataStoreRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.guava.await
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

@HiltViewModel
class MainViewModel @Inject constructor(
    private val dataRepository: DataStoreRepository
) : ViewModel(){

    val batteryLevel = dataRepository.batteryLevel.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), 0)
    val nodeName = dataRepository.nodeName.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), "Not connected")
    val tempUnit = dataRepository.tempUnit.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), true)
    val percentage = dataRepository.percentage.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), true)
    val activeSync = dataRepository.activeSync.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), false)
    val notificationIconType = dataRepository.notificationsIconType.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), 1)

    init {
        viewModelScope.launch {
            dataRepository.batteryLevel.distinctUntilChanged().collect {}
            dataRepository.nodeName.distinctUntilChanged().collect {}
            dataRepository.tempUnit.distinctUntilChanged().collect {}
            dataRepository.percentage.distinctUntilChanged().collect {}
            dataRepository.activeSync.distinctUntilChanged().collect {}
            dataRepository.notificationsIconType.distinctUntilChanged().collect {}
        }
    }

    fun toggleEnabled(context: Context) {
        viewModelScope.launch {
            val newEnabledStatus = !tempUnit.value
            dataRepository.storeTempUnit(newEnabledStatus)
            updateComplication(context = context, WatchTempComplicationService::class.java)
        }
    }
    fun togglePercentage(context: Context) {
        viewModelScope.launch {
            val newEnabledStatus = !percentage.value
            dataRepository.storePercentage(newEnabledStatus)
            updateComplication(context = context, MobileBatteryComplicationService::class.java)
            updateComplication(context = context, WatchBatteryComplicationService::class.java)
        }
    }
    fun storeNotificationIconType(context: Context, type: Int) {
        viewModelScope.launch {
            dataRepository.storeNotifIconType(type)
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
                Toast.makeText(context,"Check Experimental settings on your phone",Toast.LENGTH_LONG).show()
            } catch (cancellationException: CancellationException) {
                // Request was cancelled normally
            } catch (throwable: Throwable) {
                Toast.makeText(context,"Companion app not reachable",Toast.LENGTH_LONG).show()
            }
        }
    }

}


