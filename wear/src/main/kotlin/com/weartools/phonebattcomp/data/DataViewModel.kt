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

import android.content.ComponentName
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.wear.watchface.complications.datasource.ComplicationDataSourceUpdateRequester
import com.weartools.phonebattcomp.complication.MobileBatteryComplicationService
import com.weartools.phonebattcomp.complication.WatchBatteryComplicationService
import com.weartools.phonebattcomp.complication.WatchTempComplicationService
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class PassiveDataViewModel(
    private val dataRepository: DataRepository
) : ViewModel() {

    val batteryLevel = dataRepository.batteryLevel.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), 0)
    val nodeName = dataRepository.nodeName.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), "Not connected")
    val tempUnit = dataRepository.tempUnit.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), true)
    val percentage = dataRepository.percentage.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), true)

    init {
        viewModelScope.launch {
            dataRepository.batteryLevel.distinctUntilChanged().collect {}
            dataRepository.nodeName.distinctUntilChanged().collect {}
            dataRepository.tempUnit.distinctUntilChanged().collect {}
            dataRepository.percentage.distinctUntilChanged().collect {}
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

    fun updateComplication(context: Context, clazz: Class<*>){
        ComplicationDataSourceUpdateRequester.create(
            context.applicationContext,
            ComponentName(context.applicationContext, clazz)
        ).run { requestUpdateAll() }
    }

}

class PassiveDataViewModelFactory(
    private val dataRepository: DataRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PassiveDataViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PassiveDataViewModel(
                dataRepository = dataRepository
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}


