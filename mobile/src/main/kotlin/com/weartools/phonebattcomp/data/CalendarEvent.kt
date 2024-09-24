package com.weartools.phonebattcomp.data

import com.google.android.gms.wearable.DataMap

data class CalendarEvent(
    val title: String = "",
    val startTime: Long = 0L,
    val endTime: Long = 0L,
    val allDay: Int = 0
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