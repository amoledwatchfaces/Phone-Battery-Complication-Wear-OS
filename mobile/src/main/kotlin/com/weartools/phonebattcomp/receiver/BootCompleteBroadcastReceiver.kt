
package com.weartools.phonebattcomp.receiver

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.PutDataMapRequest
import com.weartools.phonebattcomp.ACTIVE_SYNC_KEY
import com.weartools.phonebattcomp.ACTIVE_SYNC_PATH
import com.weartools.phonebattcomp.data.DataStoreRepository
import com.weartools.phonebattcomp.receiver.CalendarContentObserver.Companion.arePermissionsGranted
import com.weartools.phonebattcomp.utils.registerCalendarObserver
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
    @Inject lateinit var calendarContentObserver: CalendarContentObserver

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
                        context.registerCalendarObserver(calendarContentObserver)
                    }
                    else {
                        dataRepository.setCalendarSyncState(false)
                    }
                }
            }
        }
    }
}