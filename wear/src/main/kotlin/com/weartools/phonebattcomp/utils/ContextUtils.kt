package com.weartools.phonebattcomp.utils

import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.core.net.toUri
import androidx.wear.watchface.complications.datasource.ComplicationDataSourceUpdateRequester
import androidx.wear.watchface.complications.datasource.SuspendingComplicationDataSourceService
import com.weartools.phonebattcomp.complication.EventTimerComplicationService
import com.weartools.phonebattcomp.complication.MobileBatteryComplicationService
import com.weartools.phonebattcomp.complication.NotificationsIcons4ComplicationService
import com.weartools.phonebattcomp.complication.NotificationsIconsComplicationService
import com.weartools.phonebattcomp.complication.NotificationsPreviewComplicationService
import com.weartools.phonebattcomp.complication.UpcomingEventComplicationService
import com.weartools.phonebattcomp.complication.WatchBatteryComplicationService

fun Context.openPlayStore() {
    try {
        startActivity(Intent(Intent.ACTION_VIEW, "market://details?id=$packageName".toUri()))
    } catch (_: ActivityNotFoundException) {
        startActivity(Intent(Intent.ACTION_VIEW, "https://play.google.com/store/apps/details?id=$packageName".toUri()))
    }
}

fun Context.updateComplication(service: Class<out SuspendingComplicationDataSourceService>) {
    ComplicationDataSourceUpdateRequester.create(this, ComponentName(this, service))
        .run { requestUpdateAll() }
}

fun Context.updateBatteriesComplications() {
    updateComplication(MobileBatteryComplicationService::class.java)
    updateComplication(WatchBatteryComplicationService::class.java)
}

fun Context.updateCalendarComplications() {
    updateComplication(EventTimerComplicationService::class.java)
    updateComplication(UpcomingEventComplicationService::class.java)
}
fun Context.updateNotificationComplications() {
    updateComplication(NotificationsIconsComplicationService::class.java)
    updateComplication(NotificationsIcons4ComplicationService::class.java)
    updateComplication(NotificationsPreviewComplicationService::class.java)
}


