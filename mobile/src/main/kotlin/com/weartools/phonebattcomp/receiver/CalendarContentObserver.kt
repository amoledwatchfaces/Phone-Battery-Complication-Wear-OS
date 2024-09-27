package com.weartools.phonebattcomp.receiver

import android.Manifest
import android.content.ContentUris
import android.content.Context
import android.content.pm.PackageManager
import android.database.ContentObserver
import android.database.Cursor
import android.os.Handler
import android.provider.CalendarContract
import androidx.core.content.ContextCompat
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import com.weartools.phonebattcomp.data.CalendarEvent
import com.weartools.phonebattcomp.data.CalendarInfo
import com.weartools.phonebattcomp.data.DataStoreRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.TimeZone
import java.util.concurrent.TimeUnit

class CalendarContentObserver(
    handler: Handler,
    private val context: Context,
    private val dataRepository: DataStoreRepository,
) : ContentObserver(handler) {

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

            //Log.d("CalendarContentObserver", "onChange!")

            if (context.arePermissionsGranted(Manifest.permission.READ_CALENDAR)) {
                // Call the suspend function after the delay
                withContext(Dispatchers.IO){
                    queryAllFutureCalendarEventAndSend(context, dataRepository.syncedCalendarsIdsString.first())
                }
            } else {
                context.contentResolver.unregisterContentObserver(this@CalendarContentObserver)
                scope.cancel()
            }
        }
    }

    companion object {
        fun queryAllFutureCalendarEventAndSend(context: Context, syncedCalendarIds: String) {

            // Early return if the calendar list is empty, no need to query
            if (syncedCalendarIds.isEmpty()) return

            // Create a selection filter for the calendar IDs
            val selection = "${CalendarContract.Instances.CALENDAR_ID} IN ($syncedCalendarIds)"  // SQL IN clause
            //Log.i("CalendarContextObserver","Calendar Selection: $selection")

            //Log.d("CalendarContentObserver", "Getting Calendar Events!")
            val offsetMillis = TimeZone.getDefault().getOffset(System.currentTimeMillis())
            //Log.d("CalendarContentObserver", "UTC offset: ${offsetMillis/(1000 * 60 * 60)}")

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
                selection,
                null,
                "${CalendarContract.Instances.BEGIN} ASC"
            )

            cursor?.use {
                while (it.moveToNext()) {
                    val eventTitle = it.getString(it.getColumnIndexOrThrow(CalendarContract.Instances.TITLE))
                    val eventStartTime = it.getLong(it.getColumnIndexOrThrow(CalendarContract.Instances.BEGIN))
                    val eventEndTime = it.getLong(it.getColumnIndexOrThrow(CalendarContract.Instances.END))
                    val isAllDay = it.getInt(it.getColumnIndexOrThrow(CalendarContract.Instances.ALL_DAY))

                    if (isAllDay == 1){
                        // Calendar sets all-day events to start at 00:00 AM UTC+0 which is not corrected by offset
                        // We need to subtract offset so UTC+0 really defines local day midnight
                        events.add(CalendarEvent(eventTitle, eventStartTime-offsetMillis, eventEndTime-offsetMillis, isAllDay))
                    }
                    else {
                        events.add(CalendarEvent(eventTitle, eventStartTime, eventEndTime, isAllDay))
                    }
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
        fun getAllCalendars(context: Context): List<CalendarInfo> {
            val calendars = mutableListOf<CalendarInfo>()

            // Define the projection (columns to retrieve)
            val projection = arrayOf(
                CalendarContract.Calendars._ID,        // Calendar ID
                CalendarContract.Calendars.CALENDAR_DISPLAY_NAME  // Calendar display name
            )

            // Query the Calendars content provider
            val cursor: Cursor? = context.contentResolver.query(
                CalendarContract.Calendars.CONTENT_URI,
                projection,
                null,    // No selection filter (retrieves all calendars)
                null,    // No selection arguments
                null     // No sort order
            )

            // Iterate over the cursor and extract calendar information
            cursor?.use {
                val idIndex = it.getColumnIndex(CalendarContract.Calendars._ID)
                val displayNameIndex = it.getColumnIndex(CalendarContract.Calendars.CALENDAR_DISPLAY_NAME)

                while (it.moveToNext()) {
                    val calendarId = it.getLong(idIndex)
                    val displayName = it.getString(displayNameIndex)
                    calendars.add(CalendarInfo(calendarId, displayName))
                }
            }

            return calendars
        }

        fun Context.arePermissionsGranted(vararg permissions: String): Boolean {
            return permissions.all { permission ->
                ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
            }
        }
    }
}