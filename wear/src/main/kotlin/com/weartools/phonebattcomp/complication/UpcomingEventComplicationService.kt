/*
 * Copyright 2022-2026 amoledwatchfaces™
 * support@amoledwatchfaces.com
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.weartools.phonebattcomp.complication

import android.app.PendingIntent
import android.content.Intent
import android.graphics.drawable.Icon
import android.text.format.DateFormat
import androidx.datastore.core.DataStore
import androidx.wear.watchface.complications.data.ComplicationData
import androidx.wear.watchface.complications.data.ComplicationText
import androidx.wear.watchface.complications.data.ComplicationType
import androidx.wear.watchface.complications.data.CountDownTimeReference
import androidx.wear.watchface.complications.data.LongTextComplicationData
import androidx.wear.watchface.complications.data.MonochromaticImage
import androidx.wear.watchface.complications.data.NoDataComplicationData
import androidx.wear.watchface.complications.data.PlainComplicationText
import androidx.wear.watchface.complications.data.ShortTextComplicationData
import androidx.wear.watchface.complications.data.TimeDifferenceComplicationText
import androidx.wear.watchface.complications.data.TimeDifferenceStyle
import androidx.wear.watchface.complications.datasource.ComplicationDataTimeline
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import androidx.wear.watchface.complications.datasource.SuspendingTimelineComplicationDataSourceService
import androidx.wear.watchface.complications.datasource.TimeInterval
import androidx.wear.watchface.complications.datasource.TimelineEntry
import com.google.android.gms.wearable.DataClient
import com.weartools.phonebattcomp.MobileListener
import com.weartools.phonebattcomp.R
import com.weartools.phonebattcomp.R.drawable
import com.weartools.phonebattcomp.data.CalendarEvent
import com.weartools.phonebattcomp.data.UserPreferences
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject

@AndroidEntryPoint
class UpcomingEventComplicationService : SuspendingTimelineComplicationDataSourceService() {

    @Inject
    lateinit var dataStore: DataStore<UserPreferences>

    @Inject
    lateinit var dataClient: DataClient

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
                    text = PlainComplicationText.Builder(text = getString(R.string.next_event_long_text_preview)).build(),
                    contentDescription = ComplicationText.EMPTY
                )
                    .setMonochromaticImage(MonochromaticImage.Builder(image = Icon.createWithResource(this, drawable.ic_event_upcoming_2)).build())
                    .setTitle(PlainComplicationText.Builder(text = "09:00").build())
                    .build()
            }
            ComplicationType.SHORT_TEXT -> {
                ShortTextComplicationData.Builder(
                    text = PlainComplicationText.Builder(text = "09:00").build(),
                    contentDescription = ComplicationText.EMPTY
                )
                    .setMonochromaticImage(MonochromaticImage.Builder(image = Icon.createWithResource(this, drawable.ic_event_upcoming_2)).build())
                    .build()
            }
            else -> null
        }
    }

    private fun convertUtcToLocalTime(utcTime: Long, is24h: Boolean): String {
        val fmt = if (is24h) "HH:mm" else "h:mm a"
        return SimpleDateFormat(fmt, Locale.getDefault()).apply {
            timeZone = TimeZone.getDefault()
        }.format(Date(utcTime))
    }

    private fun getCalendarIcon(time: Long): Int {
        val day = Calendar.getInstance().apply { timeInMillis = time }.get(Calendar.DAY_OF_MONTH)
        return when (day) {
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

    private fun getIsTomorrow(eventTime: Long, currentTime: Long): Boolean {
        val localCalendar = Calendar.getInstance().apply {
            timeInMillis = currentTime
            add(Calendar.DAY_OF_YEAR, 1)
        }
        val eventLocalCalendar = Calendar.getInstance().apply {
            timeInMillis = eventTime
        }
        return eventLocalCalendar.get(Calendar.YEAR) == localCalendar.get(Calendar.YEAR) &&
                eventLocalCalendar.get(Calendar.DAY_OF_YEAR) == localCalendar.get(Calendar.DAY_OF_YEAR)
    }

    private fun getIsToday(eventTime: Long, currentTime: Long): Boolean {
        val currentCal = Calendar.getInstance().apply { timeInMillis = currentTime }
        val eventCal = Calendar.getInstance().apply { timeInMillis = eventTime }
        return currentCal.get(Calendar.YEAR) == eventCal.get(Calendar.YEAR) &&
                currentCal.get(Calendar.DAY_OF_YEAR) == eventCal.get(Calendar.DAY_OF_YEAR)
    }

    private fun getStartOfDay(time: Long): Long {
        return Calendar.getInstance().apply {
            timeInMillis = time
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    private fun buildComplicationDataForEvent(
        type: ComplicationType,
        event: CalendarEvent?,
        evalTime: Long,
        is24h: Boolean
    ): ComplicationData? {
        if (event == null) {
            val icon = MonochromaticImage.Builder(image = Icon.createWithResource(this, drawable.ic_no_upcoming_event)).build()
            return when (type) {
                ComplicationType.LONG_TEXT -> {
                    LongTextComplicationData.Builder(
                        text = PlainComplicationText.Builder(text = getString(R.string.no_upcoming_events)).build(),
                        contentDescription = ComplicationText.EMPTY
                    )
                        .setMonochromaticImage(icon)
                        .setTapAction(openScreen())
                        .build()
                }
                ComplicationType.SHORT_TEXT -> {
                    ShortTextComplicationData.Builder(
                        text = PlainComplicationText.Builder(text = getString(R.string.no_upcoming_events_short_text)).build(),
                        contentDescription = ComplicationText.EMPTY
                    )
                        .setMonochromaticImage(icon)
                        .setTapAction(openScreen())
                        .build()
                }
                else -> null
            }
        }

        val eventIsOngoing = evalTime in event.startTime..event.endTime
        val eventIsAllDay = event.allDay == 1
        val eventIsToday = getIsToday(event.startTime, evalTime)
        val eventIsTomorrow = if (eventIsToday) false else getIsTomorrow(event.startTime, evalTime)
        val closestEventTime = if (eventIsOngoing) event.endTime else event.startTime
        val eventUpdateDelay = closestEventTime - evalTime

        val iconRes = when {
            eventIsOngoing -> drawable.ic_today
            eventIsToday && eventIsAllDay -> drawable.ic_calendar_today
            eventIsToday || eventIsTomorrow && eventIsAllDay.not() -> drawable.ic_event_upcoming_2
            else -> getCalendarIcon(event.startTime)
        }
        val icon = MonochromaticImage.Builder(image = Icon.createWithResource(this, iconRes)).build()

        return when (type) {
            ComplicationType.LONG_TEXT -> {
                val titleText = when {
                    eventIsAllDay && eventIsOngoing -> PlainComplicationText.Builder(text = getString(R.string.today)).build()
                    eventIsOngoing -> TimeDifferenceComplicationText.Builder(TimeDifferenceStyle.SHORT_SINGLE_UNIT, CountDownTimeReference(Instant.now())).build()
                    eventIsToday && (eventUpdateDelay <= 7200000) -> TimeDifferenceComplicationText.Builder(TimeDifferenceStyle.SHORT_DUAL_UNIT, CountDownTimeReference(Instant.ofEpochMilli(closestEventTime)))
                        .setDisplayAsNow(false)
                        .setText(String.format(getString(R.string.countdown_text), "^1"))
                        .build()
                    eventIsToday -> PlainComplicationText.Builder(text = convertUtcToLocalTime(closestEventTime, is24h)).build()
                    eventIsTomorrow && ((eventUpdateDelay >= 43200000) || eventIsAllDay) -> PlainComplicationText.Builder(text = getString(R.string.tomorrow)).build()
                    else -> TimeDifferenceComplicationText.Builder(TimeDifferenceStyle.SHORT_WORDS_SINGLE_UNIT, CountDownTimeReference(Instant.ofEpochMilli(closestEventTime)))
                        .setText(String.format(getString(R.string.countdown_text), "^1"))
                        .build()
                }

                LongTextComplicationData.Builder(
                    text = PlainComplicationText.Builder(text = event.title).build(),
                    contentDescription = ComplicationText.EMPTY
                )
                    .setMonochromaticImage(icon)
                    .setTitle(titleText)
                    .setTapAction(openScreen())
                    .build()
            }
            ComplicationType.SHORT_TEXT -> {
                val mainText = when {
                    eventIsAllDay && eventIsToday -> PlainComplicationText.Builder(text = getString(R.string.no_upcoming_events_short_text)).build()
                    eventIsToday -> PlainComplicationText.Builder(text = convertUtcToLocalTime(closestEventTime, is24h)).build()
                    else -> TimeDifferenceComplicationText.Builder(TimeDifferenceStyle.SHORT_WORDS_SINGLE_UNIT, CountDownTimeReference(Instant.ofEpochMilli(closestEventTime)))
                        .setText(String.format(getString(R.string.countdown_text), "^1"))
                        .build()
                }

                ShortTextComplicationData.Builder(
                    text = mainText,
                    contentDescription = ComplicationText.EMPTY
                )
                    .setMonochromaticImage(icon)
                    .setTapAction(openScreen())
                    .build()
            }
            else -> null
        }
    }

    override suspend fun onComplicationRequest(request: ComplicationRequest): ComplicationDataTimeline {
        val currentTime = System.currentTimeMillis()
        val is24h = DateFormat.is24HourFormat(this)
        val repository = dataStore.data.first()
        val events = repository.calendarEvents
            .filter { (it.allDay == 1 && it.startTime >= getStartOfDay(currentTime)) || (it.allDay == 0 && it.endTime >= currentTime) }
            .sortedBy { it.startTime }

        val defaultData = buildComplicationDataForEvent(
            type = request.complicationType,
            event = null,
            evalTime = currentTime,
            is24h = is24h
        ) ?: NoDataComplicationData()

        if (events.isEmpty()) {
            MobileListener.sendCalendarRequest(currentTime, dataClient)
            return ComplicationDataTimeline(
                defaultComplicationData = defaultData,
                timelineEntries = emptyList()
            )
        }

        val timelineEntries = mutableListOf<TimelineEntry>()
        var lastEndTime = currentTime

        for (event in events) {
            val eventStart = event.startTime
            val eventEnd = if (event.allDay == 1) getStartOfDay(eventStart) + 86400000L else event.endTime

            if (eventEnd <= lastEndTime) continue

            val startOfEventDay = getStartOfDay(eventStart)
            val startOfTomorrow = getStartOfDay(eventStart - 86400000L)
            val countdownStart = maxOf(startOfEventDay, eventStart - 7200000L)

            val boundaries = mutableListOf<Long>()
            boundaries.add(lastEndTime)
            if (startOfTomorrow in (lastEndTime + 1)..<eventStart) boundaries.add(startOfTomorrow)
            if (startOfEventDay in (lastEndTime + 1)..<eventStart) boundaries.add(startOfEventDay)
            if (countdownStart in (lastEndTime + 1)..<eventStart) boundaries.add(countdownStart)
            boundaries.add(eventStart)
            if (event.allDay == 0 && eventEnd > eventStart) boundaries.add(eventEnd)

            val sortedBoundaries = boundaries.distinct().sorted()

            for (i in 0 until sortedBoundaries.size - 1) {
                val intervalStart = sortedBoundaries[i]
                val intervalEnd = sortedBoundaries[i + 1]
                if (intervalStart >= intervalEnd) continue

                val evalTime = intervalStart + 1L
                val data = buildComplicationDataForEvent(
                    type = request.complicationType,
                    event = event,
                    evalTime = evalTime,
                    is24h = is24h
                )
                if (data != null) {
                    timelineEntries.add(
                        TimelineEntry(
                            validity = TimeInterval(
                                start = Instant.ofEpochMilli(intervalStart),
                                end = Instant.ofEpochMilli(intervalEnd)
                            ),
                            complicationData = data
                        )
                    )
                }
            }

            lastEndTime = maxOf(lastEndTime, eventEnd)
        }

        return ComplicationDataTimeline(
            defaultComplicationData = defaultData,
            timelineEntries = timelineEntries
        )
    }
}
