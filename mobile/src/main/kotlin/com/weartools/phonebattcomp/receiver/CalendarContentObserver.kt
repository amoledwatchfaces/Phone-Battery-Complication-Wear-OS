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
import com.weartools.phonebattcomp.utils.unregisterCalendarObserver
import dagger.hilt.android.qualifiers.ApplicationContext
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
import javax.inject.Inject

class CalendarContentObserver @Inject constructor(
    handler: Handler,
    @ApplicationContext private val context: Context,
    private val dataRepository: DataStoreRepository
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
                context.unregisterCalendarObserver(this@CalendarContentObserver)
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
            ContentUris.appendId(builder,currentTime + TimeUnit.DAYS.toMillis(31)) // generate 31 days into future

            // Query for all future events
            val cursor = context.contentResolver.query(
                builder.build(),
                arrayOf(
                    CalendarContract.Instances.TITLE,
                    CalendarContract.Instances.BEGIN,
                    CalendarContract.Instances.END,
                    CalendarContract.Instances.ALL_DAY,
/*
                    CalendarContract.Instances.DTEND,
                    CalendarContract.Instances.DTSTART,
                    CalendarContract.Instances.START_DAY,
                    CalendarContract.Instances.START_MINUTE,
                    CalendarContract.Instances.END_DAY,
                    CalendarContract.Instances.END_MINUTE,
                    CalendarContract.Instances.EVENT_TIMEZONE,

 */
                ),
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
/*
                    val eventDTSTART = it.getLong(it.getColumnIndexOrThrow(CalendarContract.Instances.DTSTART))
                    val eventDTEND = it.getLong(it.getColumnIndexOrThrow(CalendarContract.Instances.DTEND))
                    val eventStartDay = it.getInt(it.getColumnIndexOrThrow(CalendarContract.Instances.START_DAY))
                    val eventStartMinute = it.getInt(it.getColumnIndexOrThrow(CalendarContract.Instances.START_MINUTE))
                    val eventEndDay = it.getInt(it.getColumnIndexOrThrow(CalendarContract.Instances.END_DAY))
                    val eventEndMinute = it.getInt(it.getColumnIndexOrThrow(CalendarContract.Instances.END_MINUTE))
                    val eventTimeZone = it.getString(it.getColumnIndexOrThrow(CalendarContract.Instances.EVENT_TIMEZONE))


 */
                    if (isAllDay == 1){
                        // Calendar sets all-day events to start at 00:00 AM UTC+0 which is not corrected by offset
                        // We need to subtract offset so UTC+0 really defines local day midnight
                        events.add(
                            CalendarEvent(
                                title = eventTitle,
                                startTime = eventStartTime-offsetMillis,
                                endTime = eventEndTime-offsetMillis,
                                allDay = isAllDay,
            /*
                                dtStart = eventDTSTART,
                                dtEnd = eventDTEND,
                                startDay = eventStartDay,
                                startMinute = eventStartMinute,
                                endDay = eventEndDay,
                                endMinute = eventEndMinute,
                                timeZone = eventTimeZone

             */
                            )
                        )
                    }
                    else {
                        events.add(
                            CalendarEvent(
                                title = eventTitle,
                                startTime = eventStartTime,
                                endTime = eventEndTime,
                                allDay = isAllDay,
/*
                                dtStart = eventDTSTART,
                                dtEnd = eventDTEND,
                                startDay = eventStartDay,
                                startMinute = eventStartMinute,
                                endDay = eventEndDay,
                                endMinute = eventEndMinute,
                                timeZone = eventTimeZone

 */
                            )
                        )
                    }
                }
            }

            // Log the number of events found and return the list of events
            /*
                        if (events.isNotEmpty()){
                            for (event in events){
                                Log.i("queryAllFutureEvents", "eventTitle: ${event.title}")
                                Log.i("queryAllFutureEvents", "eventStart: ${event.startTime}")
                                Log.i("queryAllFutureEvents", "eventEnd: ${event.endTime}")
                                Log.i("queryAllFutureEvents", "-------------------------")
                            }



                            Log.i("queryAllFutureEvents", "startTime: ${events[0].startTime}")
                            Log.i("queryAllFutureEvents", "endTime: ${events[0].endTime}")
                            Log.i("queryAllFutureEvents", "allDay: ${events[0].allDay}")
                            Log.i("queryAllFutureEvents", "dtStart: ${events[0].dtStart}")
                            Log.i("queryAllFutureEvents", "dtEnd: ${events[0].dtEnd}")
                            Log.i("queryAllFutureEvents", "startDay: ${events[0].startDay}")
                            Log.i("queryAllFutureEvents", "startMinute: ${events[0].startMinute}")
                            Log.i("queryAllFutureEvents", "endDay: ${events[0].endDay}")
                            Log.i("queryAllFutureEvents", "endMinute: ${events[0].endMinute}")
                            Log.i("queryAllFutureEvents", "timeZone: ${events[0].timeZone}")

                            val julianStartToUTC = julianDayToUtcMillis(events[0].startDay,events[0].startMinute)
                            val julianEndToUTC = julianDayToUtcMillis(events[0].endDay,events[0].endMinute)

                            Log.i("queryAllFutureEvents", "julianDayToUtcStartTime: $julianStartToUTC")
                            Log.i("queryAllFutureEvents", "julianDayToUtcEndTime: $julianEndToUTC")


            }
             */




            val dataMapList = events.map { it.toDataMap() }
            val putDataMapReq = PutDataMapRequest.create("/calendar-events")
            putDataMapReq.dataMap.putDataMapArrayList("events", ArrayList(dataMapList))
            putDataMapReq.setUrgent()
            dataClient.putDataItem(putDataMapReq.asPutDataRequest())
        }
        /*
        fun julianDayToUtcMillis(julianDay: Int, minutesFromMidnight: Int): Long {
            return (julianDay - 2440588L) * 86400000L + minutesFromMidnight * 60000L
        }

         */
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