/*
 *   Copyright 2022 Benoit LETONDOR
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package com.weartools.phonebattcomp

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.provider.CalendarContract
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.PutDataMapRequest
import com.weartools.phonebattcomp.CalendarContentObserver.Companion.arePermissionsGranted
import com.weartools.phonebattcomp.data.DataStoreRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class BootCompleteBroadcastReceiver : BroadcastReceiver() {

    @Inject lateinit var dataRepository: DataStoreRepository
    @Inject lateinit var dataClient: DataClient

    private val ioScope = CoroutineScope(Dispatchers.IO)
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED || intent.action == "android.intent.action.QUICKBOOT_POWERON") {

            ioScope.launch {
                if (dataRepository.activeSync.first()){
                    BatteryStatusBroadcastReceiver.subscribeToUpdates(context)
                }
                else {
                    val request = PutDataMapRequest.create(ACTIVE_SYNC_PATH).apply{
                        dataMap.putBoolean(ACTIVE_SYNC_KEY, false)
                        dataMap.putLong("immediate-update", System.currentTimeMillis()) }
                        .asPutDataRequest()
                        .setUrgent()

                    dataClient.putDataItem(request)

                }

                if (dataRepository.calendarSync.first()){
                    if (context.arePermissionsGranted(Manifest.permission.READ_CALENDAR)) {
                        val handler = Handler(context.mainLooper)
                        val observer = CalendarContentObserver(handler, context)
                        // Call the suspend function after the delay
                        context.contentResolver.registerContentObserver(
                            CalendarContract.Events.CONTENT_URI,
                            true, // true for recursive monitoring of child URIs
                            observer
                        )
                    }
                    else {
                        dataRepository.setCalendarSyncState(false)
                    }
                }
            }
        }
    }
}