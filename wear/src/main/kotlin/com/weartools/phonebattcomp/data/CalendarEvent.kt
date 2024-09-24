package com.weartools.phonebattcomp.data

import com.google.android.gms.wearable.DataMap
import kotlinx.serialization.Serializable

@Serializable
data class CalendarEvent(
    val title: String,
    val startTime: Long,
    val endTime: Long,
    val allDay: Int
){
    companion object {
        fun fromDataMap(dataMap: DataMap): CalendarEvent {
            return CalendarEvent(
                title = dataMap.getString("title") ?: "",
                startTime = dataMap.getLong("startTime"),
                endTime = dataMap.getLong("endTime"),
                allDay = dataMap.getInt("allDay")
            )
        }
    }
}