package com.weartools.phonebattcomp.data

import com.google.android.gms.wearable.DataMap
import kotlinx.serialization.Serializable

data class CalendarEvent(
    val title: String = "",
    val startTime: Long = 0L,
    val endTime: Long = 0L,
    val allDay: Int = 0,
    /*
    val dtStart: Long = 0L,
    val dtEnd: Long = 0L,
    val startDay: Int = 0,
    val startMinute: Int = 0,
    val endDay: Int = 0,
    val endMinute: Int = 0,
    val timeZone: String = ""

     */
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
// Data class to store calendar information
@Serializable
data class CalendarInfo(
    val calendarId: Long,
    val displayName: String
)