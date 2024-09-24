package com.weartools.phonebattcomp.receiver

import android.Manifest
import android.content.ContentUris
import android.content.Context
import android.content.pm.PackageManager
import android.database.ContentObserver
import android.os.Handler
import android.provider.CalendarContract
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.android.gms.wearable.DataMap
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class CalendarContentObserver(handler: Handler, private val context: Context) : ContentObserver(handler) {

    // Coroutine scope for managing coroutines (using Dispatchers.Main for debouncing)
    private val scope = CoroutineScope(Dispatchers.Main)

    // Coroutine job for debouncing
    private var debounceJob: Job? = null
    private val debounceDelay = 1000L // 1 second debounce delay

    override fun onChange(selfChange: Boolean) {
        super.onChange(selfChange)
        // Cancel any existing debounce jobs
        debounceJob?.cancel()

        // Launch a new coroutine with a debounce delay
        debounceJob = scope.launch {
            delay(debounceDelay) // Wait for the debounce delay

            Log.d("CalendarContentObserver", "onChange!")

            if (context.arePermissionsGranted(Manifest.permission.READ_CALENDAR)) {
                // Call the suspend function after the delay
                withContext(Dispatchers.IO){
                    queryAllFutureCalendarEventAndSend(context)
                }
            } else {
                context.contentResolver.unregisterContentObserver(this@CalendarContentObserver)
                scope.cancel()
            }
        }
    }

    companion object {
        fun queryAllFutureCalendarEventAndSend(context: Context) {

            Log.d("CalendarContentObserver", "Getting Calendar Events!")

            val dataClient = Wearable.getDataClient(context)
            val events = mutableListOf<CalendarEvent>()
            val currentTime = System.currentTimeMillis()

            // Use the Instances content URI with the time range
            val builder = CalendarContract.Instances.CONTENT_URI.buildUpon()
            ContentUris.appendId(builder,currentTime)
            ContentUris.appendId(builder,currentTime + TimeUnit.DAYS.toMillis(14)) // generate 2 weeks ahead

            // Query for all future events
            val cursor = context.contentResolver.query(
                builder.build(),
                arrayOf(CalendarContract.Instances.TITLE, CalendarContract.Instances.BEGIN, CalendarContract.Instances.END, CalendarContract.Instances.ALL_DAY),
                null,
                null,
                "${CalendarContract.Instances.BEGIN} ASC"
            )

            cursor?.use {
                while (it.moveToNext()) {
                    val eventTitle = it.getString(it.getColumnIndexOrThrow(CalendarContract.Instances.TITLE))
                    val eventStartTime = it.getLong(it.getColumnIndexOrThrow(CalendarContract.Instances.BEGIN))
                    val eventEndTime = it.getLong(it.getColumnIndexOrThrow(CalendarContract.Instances.END))
                    val isAllDay = it.getInt(it.getColumnIndexOrThrow(CalendarContract.Instances.ALL_DAY))

                    // Add the event to the list
                    events.add(CalendarEvent(eventTitle, eventStartTime, eventEndTime, isAllDay))
                }
            }

            // Log the number of events found and return the list of events
            /*
            Log.i("queryAllFutureEvents", "Found ${events.size} future events.")
            if (events.isNotEmpty()){
                Log.i("queryAllFutureEvents", "First Event Title: ${events[0].title}")
            }
             */

            val dataMapList = events.map { it.toDataMap() }
            val putDataMapReq = PutDataMapRequest.create("/calendar-events")
            putDataMapReq.dataMap.putDataMapArrayList("events", ArrayList(dataMapList))
            putDataMapReq.setUrgent()
            dataClient.putDataItem(putDataMapReq.asPutDataRequest())
        }

        fun Context.arePermissionsGranted(vararg permissions: String): Boolean {
            return permissions.all { permission ->
                ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
            }
        }
    }
}



data class CalendarEvent(
    val title: String,
    val startTime: Long,
    val endTime: Long,
    val allDay: Int
){
    fun toDataMap(): DataMap {
        val dataMap = DataMap()
        dataMap.putString("title", title)
        dataMap.putLong("startTime", startTime)
        dataMap.putLong("endTime", endTime)
        dataMap.putInt("allDay", allDay)
        return dataMap
    }
}