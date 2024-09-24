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
import java.time.Duration
import java.time.Instant
import javax.inject.Inject

@AndroidEntryPoint
class EventsTimerComplicationService : SuspendingComplicationDataSourceService() {

    @Inject lateinit var repository: DataStoreRepository
    @Inject lateinit var dataClient: DataClient

    var icon = drawable.ic_event_upcoming_2

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
                    text = PlainComplicationText.Builder(text = "Meeting").build(),
                    contentDescription = ComplicationText.EMPTY)
                    .setMonochromaticImage(MonochromaticImage.Builder(image = Icon.createWithResource(this, drawable.ic_event_upcoming_2)).build())
                    .setTitle(PlainComplicationText.Builder(text = "1h 30m").build())
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

    fun findClosestEventWithTime(events: List<CalendarEvent>, currentTime: Long): Pair<String, Long>? {

        val closestEventTime = events
            .filter { event -> event.allDay == 0 }
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

        if (events.any { event -> event.endTime == closestEventTime?.second }){
            icon = drawable.ic_pending_1
        }

        return closestEventTime
    }

    override suspend fun onComplicationRequest(request: ComplicationRequest): ComplicationData? {

        val currentTime = System.currentTimeMillis()
        val events = repository.getEvents().first()

        /** When currently some event is running, show countdown to event endTime
         *  else, show countdown to next event startTime
         *  else show 'no upcoming events'
         */

        val closestEvent = findClosestEventWithTime(events, currentTime)
        val closestEventName = closestEvent?.first ?: "No upcoming events"
        val closestEventTime = closestEvent?.second ?: 0L
        //Log.i("CalendarEventTimerComplication", "Nearest or current event: $closestEventName")

        /** Schedule event update when finished / started **/
        if (closestEvent != null){
            val delay = closestEventTime - currentTime
           // Log.i("CalendarEventTimerComplication", "Scheduling complication update with delay: ${delay/60000}")
            WorkManager.getInstance(this).enqueueUniqueWork(
                "events_timer_work",
                ExistingWorkPolicy.REPLACE,
                OneTimeWorkRequestBuilder<EventsTimerComplicationUpdateWorker>()
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
                        if (closestEvent == null) { null }
                        else { TimeDifferenceComplicationText.Builder(TimeDifferenceStyle.SHORT_DUAL_UNIT, CountDownTimeReference(Instant.ofEpochMilli(closestEventTime))).build() })
                    .setTapAction(openScreen())
                    .build()
            }

            else -> {null}
        }
    }
}
class EventsTimerComplicationUpdateWorker(private val appContext: Context, workerParams: WorkerParameters) : CoroutineWorker(appContext, workerParams) {
    override suspend fun doWork(): Result {
        //Log.i("CalendarEventComplicationUpdateWorker", "Updating Calendar Event Complication")
        appContext.updateComplication(EventsTimerComplicationService::class.java)
        return Result.success()
    }
}

