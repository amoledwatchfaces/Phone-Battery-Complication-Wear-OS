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
import android.text.format.DateUtils
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
import com.weartools.phonebattcomp.MobileListener
import com.weartools.phonebattcomp.R
import com.weartools.phonebattcomp.R.drawable
import com.weartools.phonebattcomp.data.CalendarEvent
import com.weartools.phonebattcomp.data.DataStoreRepository
import com.weartools.phonebattcomp.utils.updateComplication
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject

@AndroidEntryPoint
class UpcomingEventComplicationService : SuspendingComplicationDataSourceService() {

    @Inject lateinit var repository: DataStoreRepository
    @Inject lateinit var dataClient: DataClient

    var icon = drawable.ic_calendar_today
    var eventIsAllDay = false
    var eventIsOngoing = false
    var eventIsToday = true
    var eventUpdateDelay = 0L

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
                    text = PlainComplicationText.Builder(text = getString(R.string.preview_coffee_chat)).build(),
                    contentDescription = ComplicationText.EMPTY)
                    .setMonochromaticImage(MonochromaticImage.Builder(image = Icon.createWithResource(this, drawable.ic_event_upcoming_2)).build())
                    .setTitle(PlainComplicationText.Builder(text = "09:00").build())
                    .build()
            }

            else -> {null}
        }
    }

    fun convertUtcToLocalTime(utcTime: Long, is24h: Boolean): String {
        val fmt = if (is24h) "HH:mm" else "h:mm a" // Add "a" for AM/PM in 12-hour format
        return SimpleDateFormat(fmt, Locale.getDefault()).apply {
            timeZone = TimeZone.getDefault()
        }.format(Date(utcTime))
    }
    fun getTodayIcon(): Int {
        return when (LocalDate.now().dayOfMonth){
            1 -> drawable.ic_cal_01
            2 -> drawable.ic_cal_02
            3 -> drawable.ic_cal_03
            4 -> drawable.ic_cal_04
            5 -> drawable.ic_cal_05
            6 -> drawable.ic_cal_06
            7 -> drawable.ic_cal_07
            8 -> drawable.ic_cal_08
            9 -> drawable.ic_cal_09
            10 -> drawable.ic_cal_10
            11 -> drawable.ic_cal_11
            12 -> drawable.ic_cal_12
            13 -> drawable.ic_cal_13
            14 -> drawable.ic_cal_14
            15 -> drawable.ic_cal_15
            16 -> drawable.ic_cal_16
            17 -> drawable.ic_cal_17
            18 -> drawable.ic_cal_18
            19 -> drawable.ic_cal_19
            20 -> drawable.ic_cal_20
            21 -> drawable.ic_cal_21
            22 -> drawable.ic_cal_22
            23 -> drawable.ic_cal_23
            24 -> drawable.ic_cal_24
            25 -> drawable.ic_cal_25
            26 -> drawable.ic_cal_26
            27 -> drawable.ic_cal_27
            28 -> drawable.ic_cal_28
            29 -> drawable.ic_cal_29
            30 -> drawable.ic_cal_30
            else -> drawable.ic_cal_31
        }
    }
    fun findClosestEventWithTime(events: List<CalendarEvent>, currentTime: Long): Pair<String, Long>? {
        var closestEvent: CalendarEvent? = null
        var closestEventTime: Long? = null
        var closestEventTimeDiff = Long.MAX_VALUE

        events.forEach { event ->
            // Consider both start and end times for future or ongoing events
            val relevantTimes = listOf(event.startTime, event.endTime).filter { it >= currentTime }

            relevantTimes.forEach { time ->
                val timeDiff = time - currentTime
                if (timeDiff < closestEventTimeDiff) {
                    closestEventTimeDiff = timeDiff
                    closestEvent = event
                    closestEventTime = time
                }
            }
        }

        closestEvent?.let { event ->
            eventIsOngoing = currentTime in event.startTime..event.endTime
            eventIsAllDay = event.allDay == 1
            eventIsToday = DateUtils.isToday(event.startTime)

            //Log.i("CalendarEventTimerComplication", "Event title: ${event.title}")
            //Log.i("CalendarEventTimerComplication", "Event isOngoing: $eventIsOngoing")
            //Log.i("CalendarEventTimerComplication", "Event isAllDay: $eventIsAllDay")
            //Log.i("CalendarEventTimerComplication", "Event startTime: ${event.startTime}")
            //Log.i("CalendarEventTimerComplication", "Event endTime: ${event.endTime}")
            //Log.i("CalendarEventTimerComplication", "Event isToday: $eventIsToday")

            icon = when {
                eventIsAllDay -> getTodayIcon()
                eventIsOngoing -> drawable.ic_today
                else -> drawable.ic_event_upcoming_2
            }
        }

        return closestEvent?.let { it.title to (closestEventTime ?: 0L) }
    }

    override suspend fun onComplicationRequest(request: ComplicationRequest): ComplicationData? {

        val currentTime = System.currentTimeMillis()
        val is24h = DateFormat.is24HourFormat(this)
        val events = repository.getEvents().first()

        val closestEvent = findClosestEventWithTime(events, currentTime)
        val (closestEventName, closestEventTime) = closestEvent?.let { it.first to it.second } ?: (getString(R.string.event_no_upcoming_events) to 0L)
        //Log.i("CalendarEventTimerComplication", "Nearest or current event: $closestEventName")

        /** Schedule event update when finished / started, only when delay is in future to avoid loop **/
        closestEvent?.let {
            eventUpdateDelay = closestEventTime - currentTime
            if (eventUpdateDelay > 0L) {
                WorkManager.getInstance(this).enqueueUniqueWork(
                    "upcoming_event_work",
                    ExistingWorkPolicy.REPLACE,
                    OneTimeWorkRequestBuilder<UpcomingEventComplicationUpdateWorker>()
                        .setInitialDelay(Duration.ofMillis(eventUpdateDelay))
                        .build()
                )
            }
        } ?: run {
            // When there are no close events, check for new events
            MobileListener.sendCalendarRequest(currentTime, dataClient)
            icon = drawable.ic_no_upcoming_event
        }

        return when (request.complicationType) {

            ComplicationType.LONG_TEXT -> {
                LongTextComplicationData.Builder(
                    text = PlainComplicationText.Builder(
                        text = if (eventIsAllDay && eventIsToday.not()) getString(R.string.event_no_events_today)
                        else closestEventName
                    ).build(),
                    contentDescription = ComplicationText.EMPTY)
                    .setMonochromaticImage(MonochromaticImage.Builder(image = Icon.createWithResource(this, icon)).build())
                    .setTitle(
                        when {
                            /** Do not show Title when event is all day and today **/
                            closestEvent == null -> null
                            /** Do not show Title when closest event is all day but not today **/
                            eventIsAllDay && eventIsToday.not() -> null
                            /** Show 'Today' if event is all day and ongoing **/
                            eventIsAllDay && eventIsOngoing -> PlainComplicationText.Builder(text = getString(R.string.event_today)).build()
                            /** Show Localized 'Now' when event is ongoing **/
                            eventIsOngoing -> TimeDifferenceComplicationText.Builder(TimeDifferenceStyle.SHORT_SINGLE_UNIT, CountDownTimeReference(Instant.now()))
                                .build()
                            /** Show Localized 'in x minutes' when event start time is under 2 hours but today **/
                            eventIsToday && (eventUpdateDelay <= 7200000) -> TimeDifferenceComplicationText.Builder(TimeDifferenceStyle.SHORT_DUAL_UNIT, CountDownTimeReference(Instant.ofEpochMilli(closestEventTime)))
                                .setDisplayAsNow(false)
                                .setText("${getString(R.string.event_in)} ^1")
                                .build()
                            /** Show normal event start time (HH:mm) when start time is above 2 hours but today  **/
                            eventIsToday -> PlainComplicationText.Builder(text = convertUtcToLocalTime(closestEventTime, is24h)).build()
                            /** Show Localized 'in x days' when event start time is not today  **/
                            else -> TimeDifferenceComplicationText.Builder(TimeDifferenceStyle.WORDS_SINGLE_UNIT, CountDownTimeReference(Instant.ofEpochMilli(closestEventTime)))
                                .setText("${getString(R.string.event_in)} ^1")
                                .build()
                        }
                    )
                    .setTapAction(openScreen())
                    .build()
            }

            else -> {null}
        }
    }
}
class UpcomingEventComplicationUpdateWorker(private val appContext: Context, workerParams: WorkerParameters) : CoroutineWorker(appContext, workerParams) {
    override suspend fun doWork(): Result {
        //Log.i("CalendarEventComplicationUpdateWorker", "Updating Calendar Event Complication")
        appContext.updateComplication(UpcomingEventComplicationService::class.java)
        return Result.success()
    }
}

