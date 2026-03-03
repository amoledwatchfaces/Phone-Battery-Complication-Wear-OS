package com.weartools.phonebattcomp.receiver

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import androidx.datastore.core.DataStore
import com.google.android.gms.wearable.DataClient
import com.weartools.phonebattcomp.BuildConfig
import com.weartools.phonebattcomp.data.UserPreferences
import com.weartools.phonebattcomp.receiver.CalendarContentObserver.Companion.arePermissionsGranted
import com.weartools.phonebattcomp.utils.registerCalendarObserver
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class AppUpdateBroadcastReceiver : BroadcastReceiver() {

    @Inject
    lateinit var dataStore: DataStore<UserPreferences>

    @Inject lateinit var dataClient: DataClient
    @Inject lateinit var calendarContentObserver: CalendarContentObserver

    private val ioScope = CoroutineScope(Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent?) {
        if( intent?.action != "android.intent.action.MY_PACKAGE_REPLACED" ) {
            return
        }

        ioScope.launch {
            // CHECK BACKGROUND SERVICE ON UPDATE
            // Enable all syncs when user had background service running
            val isServiceRunning = NotificationManagerCompat.getEnabledListenerPackages(context).contains(BuildConfig.APPLICATION_ID)

            if (isServiceRunning){
                dataStore.updateData { it.copy(
                    backgroundServiceState = true,
                    calendarSync = true
                ) }

                BatteryStatusBroadcastReceiver.subscribeToUpdates(context)

                // Initiate Calendar Sync
                if (context.arePermissionsGranted(Manifest.permission.READ_CALENDAR)) {
                    if (dataStore.data.first().syncedCalendarsIds.isEmpty()){
                        val allCalendars = CalendarContentObserver.getAllCalendars(context)
                        dataStore.updateData {
                            it.copy(
                                syncedCalendars = allCalendars,
                                syncedCalendarsIds = allCalendars.map { calendar -> calendar.calendarId }.joinToString(",")
                            )
                        }
                    }
                    context.registerCalendarObserver(calendarContentObserver)
                }
                else {
                    dataStore.updateData { it.copy(calendarSync = false) }
                }
            }
            else {
                dataStore.updateData { it.copy(
                    backgroundServiceState = false,
                    calendarSync = false
                ) }
            }
        }
    }

}