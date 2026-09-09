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
import com.weartools.phonebattcomp.data.UserPreferences
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import java.time.Instant
import javax.inject.Inject

@AndroidEntryPoint
class EventTimerComplicationService : SuspendingTimelineComplicationDataSourceService() {

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
                    text = PlainComplicationText.Builder(text = getString(R.string.preview_meeting)).build(),
                    contentDescription = ComplicationText.EMPTY
                )
                    .setMonochromaticImage(MonochromaticImage.Builder(image = Icon.createWithResource(this, drawable.ic_event_upcoming_2)).build())
                    .setTitle(PlainComplicationText.Builder(text = "1h 30m").build())
                    .build()
            }
            ComplicationType.SHORT_TEXT -> {
                ShortTextComplicationData.Builder(
                    text = PlainComplicationText.Builder(text = getString(R.string.preview_meeting)).build(),
                    contentDescription = ComplicationText.EMPTY
                )
                    .setMonochromaticImage(MonochromaticImage.Builder(image = Icon.createWithResource(this, drawable.ic_event_upcoming_2)).build())
                    .setTitle(PlainComplicationText.Builder(text = "1h 30m").build())
                    .build()
            }
            else -> null
        }
    }

    private fun buildComplicationData(
        type: ComplicationType,
        eventName: String,
        targetTime: Long?,
        iconRes: Int
    ): ComplicationData? {
        val icon = MonochromaticImage.Builder(image = Icon.createWithResource(this, iconRes)).build()
        val countdownText = targetTime?.let {
            TimeDifferenceComplicationText.Builder(
                TimeDifferenceStyle.SHORT_DUAL_UNIT,
                CountDownTimeReference(Instant.ofEpochMilli(it))
            ).build()
        }

        return when (type) {
            ComplicationType.LONG_TEXT -> {
                LongTextComplicationData.Builder(
                    text = PlainComplicationText.Builder(text = eventName).build(),
                    contentDescription = ComplicationText.EMPTY
                )
                    .setMonochromaticImage(icon)
                    .setTitle(countdownText)
                    .setTapAction(openScreen())
                    .build()
            }
            ComplicationType.SHORT_TEXT -> {
                ShortTextComplicationData.Builder(
                    text = PlainComplicationText.Builder(text = eventName).build(),
                    contentDescription = ComplicationText.EMPTY
                )
                    .setMonochromaticImage(icon)
                    .setTitle(countdownText)
                    .setTapAction(openScreen())
                    .build()
            }
            else -> null
        }
    }

    override suspend fun onComplicationRequest(request: ComplicationRequest): ComplicationDataTimeline {
        val currentTime = System.currentTimeMillis()
        val repository = dataStore.data.first()
        val events = repository.calendarEvents
            .filter { it.allDay == 0 && it.endTime >= currentTime }
            .sortedBy { it.startTime }

        val defaultData = buildComplicationData(
            type = request.complicationType,
            eventName = if (request.complicationType == ComplicationType.SHORT_TEXT) {
                getString(R.string.no_upcoming_events_short_text)
            } else {
                getString(R.string.no_upcoming_events)
            },
            targetTime = null,
            iconRes = drawable.ic_no_upcoming_event
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
            // 1. Upcoming phase (before event start)
            if (event.startTime > lastEndTime) {
                val upcomingData = buildComplicationData(
                    type = request.complicationType,
                    eventName = event.title,
                    targetTime = event.startTime,
                    iconRes = drawable.ic_event_upcoming_2
                )
                if (upcomingData != null) {
                    timelineEntries.add(
                        TimelineEntry(
                            validity = TimeInterval(
                                start = Instant.ofEpochMilli(lastEndTime),
                                end = Instant.ofEpochMilli(event.startTime)
                            ),
                            complicationData = upcomingData
                        )
                    )
                }
            }

            // 2. Ongoing phase (during event)
            if (event.endTime > event.startTime) {
                val ongoingStart = maxOf(currentTime, event.startTime)
                if (event.endTime > ongoingStart) {
                    val ongoingData = buildComplicationData(
                        type = request.complicationType,
                        eventName = event.title,
                        targetTime = event.endTime,
                        iconRes = drawable.ic_pending_1
                    )
                    if (ongoingData != null) {
                        timelineEntries.add(
                            TimelineEntry(
                                validity = TimeInterval(
                                    start = Instant.ofEpochMilli(ongoingStart),
                                    end = Instant.ofEpochMilli(event.endTime)
                                ),
                                complicationData = ongoingData
                            )
                        )
                    }
                }
            }
            lastEndTime = maxOf(lastEndTime, event.endTime)
        }

        return ComplicationDataTimeline(
            defaultComplicationData = defaultData,
            timelineEntries = timelineEntries
        )
    }
}
