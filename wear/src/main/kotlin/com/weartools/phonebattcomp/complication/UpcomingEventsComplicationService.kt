/*
 * Copyright 2022 amoledwatchfaces™
 * support@amoledwatchfaces.com
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
package com.weartools.phonebattcomp.complication

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Icon
import android.text.format.DateFormat
import androidx.wear.watchface.complications.data.ComplicationData
import androidx.wear.watchface.complications.data.ComplicationText
import androidx.wear.watchface.complications.data.ComplicationType
import androidx.wear.watchface.complications.data.CountDownTimeReference
import androidx.wear.watchface.complications.data.LongTextComplicationData
import androidx.wear.watchface.complications.data.MonochromaticImage
import androidx.wear.watchface.complications.data.PlainComplicationText
import androidx.wear.watchface.complications.data.TimeDifferenceComplicationText
import androidx.wear.watchface.complications.data.TimeDifferenceStyle
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import androidx.wear.watchface.complications.datasource.SuspendingComplicationDataSourceService
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.PutDataMapRequest
import com.weartools.phonebattcomp.CALENDAR_REQUEST_PATH
import com.weartools.phonebattcomp.R.drawable
import com.weartools.phonebattcomp.data.CalendarEvent
import com.weartools.phonebattcomp.data.DataStoreRepository
import com.weartools.phonebattcomp.lastCalendarRequestTime
import com.weartools.phonebattcomp.utils.updateComplication
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.time.Duration
import java.time.Instant
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject

@AndroidEntryPoint
class UpcomingEventsComplicationService : SuspendingComplicationDataSourceService() {

    @Inject lateinit var repository: DataStoreRepository
    @Inject lateinit var dataClient: DataClient

    var icon = drawable.ic_event_upcoming_2
    var eventIsAllDay = false
    var eventIsOngoing = false
    var eventIsToday = true

    private fun openScreen(): PendingIntent? {

        val calendarIntent = Intent()
        calendarIntent.action = Intent.ACTION_MAIN
        calendarIntent.addCategory(Intent.CATEGORY_APP_CALENDAR)

        return PendingIntent.getActivity(
            this, 0, calendarIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    override fun getPreviewData(type: ComplicationType): ComplicationData? {
        return when (type) {

            ComplicationType.LONG_TEXT -> {
                LongTextComplicationData.Builder(
                    text = PlainComplicationText.Builder(text = "Coffee Chat").build(),
                    contentDescription = ComplicationText.EMPTY)
                    .setMonochromaticImage(MonochromaticImage.Builder(image = Icon.createWithResource(this, drawable.ic_event_upcoming_2)).build())
                    .setTitle(PlainComplicationText.Builder(text = "09:00").build())
                    .build()
            }

            else -> {null}
        }
    }

    fun sendCalendarRequest (currentTime: Long, dataClient: DataClient) {
        // Send request only every 30 minutes to avoid looping requests
        if (currentTime - lastCalendarRequestTime > 1800000 ){
            lastCalendarRequestTime = currentTime
            val request = PutDataMapRequest.create(CALENDAR_REQUEST_PATH).apply {
                dataMap.putLong("calendarUpdate", currentTime) }
                .asPutDataRequest()
                .setUrgent()

            dataClient.putDataItem(request)
        }
    }
    fun convertUtcToLocalTime(utcTime: Long, is24h: Boolean): String {
        // Define the time format based on is24h
        val fmt = if (is24h) "HH:mm" else "h:mm a" // Add "a" for AM/PM in 12-hour format

        // Create a SimpleDateFormat with the chosen format
        val dateFormat = SimpleDateFormat(fmt, Locale.getDefault())

        // Set the time zone to the device's default time zone
        dateFormat.timeZone = TimeZone.getDefault()

        // Format the UTC time to the local time
        return dateFormat.format(Date(utcTime))
    }
    fun checkIfEventIsToday(currentTime: Long, startTime: Long) {
        val todayStartMillis = currentTime - (currentTime % 86400000) // 86400000 milliseconds in a day
        eventIsToday = startTime >= todayStartMillis && startTime < todayStartMillis + 86400000
    }

    fun findClosestEventWithTime(events: List<CalendarEvent>, currentTime: Long): Pair<String, Long>? {

        val closestEventTime = events
            .flatMap { event ->
            // Use 0L if startTime or endTime is in the past, otherwise use the actual time
            listOf(
                event.title to event.startTime,
                event.title to event.endTime
            )
        }.filter { (_, time) ->
            time >= currentTime // Only consider non-negative times (future or present)
        }.minByOrNull { (_, time) ->
            (time - currentTime) // Find the closest time to currentTime
        }

        // check if event is running and if it is all day event
        val ongoingEvent = events.firstOrNull { event -> event.endTime == closestEventTime?.second }
        if (ongoingEvent != null) {
            // set event as ongoing
            eventIsOngoing = true
            if (ongoingEvent.allDay == 1) {
                eventIsAllDay = true // mark ongoing event as allDay
                icon = drawable.ic_calendar_today // set allDay Icon
            } else {
                icon = drawable.ic_today // or update icon for non-allDay ongoing event
            }
        }

        // if closestEventTime is not null and ongoingEvent is null, that means that
        // closestEventTime must be the start time of an event, therefore, we can check if the
        // start of the event is Today
        if (closestEventTime != null && ongoingEvent == null) {
            checkIfEventIsToday(currentTime, closestEventTime.second)
        }

        return closestEventTime
    }

    override suspend fun onComplicationRequest(request: ComplicationRequest): ComplicationData? {

        val currentTime = System.currentTimeMillis()
        val is24h = DateFormat.is24HourFormat(this)
        val events = repository.getEvents().first()

        val closestEvent = findClosestEventWithTime(events, currentTime)
        val closestEventName = closestEvent?.first ?: "No upcoming events"
        val closestEventTime = closestEvent?.second ?: 0L
        //Log.i("CalendarEventTimerComplication", "Nearest or current event: $closestEventName")

        /** Schedule event update when finished / started **/
        if (closestEvent != null){
            val delay = closestEventTime - currentTime
           // Log.i("CalendarEventTimerComplication", "Scheduling complication update with delay: ${delay/60000}")
            WorkManager.getInstance(this).enqueueUniqueWork(
                "upcoming_event_work",
                ExistingWorkPolicy.REPLACE,
                OneTimeWorkRequestBuilder<UpcomingEventsComplicationUpdateWorker>()
                    .setInitialDelay(Duration.ofMillis(delay))
                    .build()
            )
        }
        else {
            // when there is no close event, we want to check phone for new events
            sendCalendarRequest(currentTime,dataClient)
            icon = drawable.ic_no_upcoming_event
        }

        return when (request.complicationType) {

            ComplicationType.LONG_TEXT -> {
                LongTextComplicationData.Builder(
                    text = PlainComplicationText.Builder(text = closestEventName).build(),
                    contentDescription = ComplicationText.EMPTY)
                    .setMonochromaticImage(MonochromaticImage.Builder(image = Icon.createWithResource(this, icon)).build())
                    .setTitle(
                        if (closestEvent == null || eventIsAllDay) { null } // Do not show title if no event or allDay event
                        else if (eventIsOngoing) { PlainComplicationText.Builder(text = "Now").build()} // Show "Now" if event is ongoing
                        else if (eventIsToday) { PlainComplicationText.Builder(text = convertUtcToLocalTime(closestEventTime,is24h)).build()} // Show  event local time if event is today
                        else { TimeDifferenceComplicationText.Builder(TimeDifferenceStyle.SHORT_DUAL_UNIT, CountDownTimeReference(Instant.ofEpochMilli(closestEventTime))).build() } // Show countdown to event in future
                    )
                    .setTapAction(openScreen())
                    .build()
            }

            else -> {null}
        }
    }
}
class UpcomingEventsComplicationUpdateWorker(private val appContext: Context, workerParams: WorkerParameters) : CoroutineWorker(appContext, workerParams) {
    override suspend fun doWork(): Result {
        //Log.i("CalendarEventComplicationUpdateWorker", "Updating Calendar Event Complication")
        appContext.updateComplication(UpcomingEventsComplicationService::class.java)
        return Result.success()
    }
}

