package com.weartools.phonebattcomp.utils

import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.wear.watchface.complications.datasource.ComplicationDataSourceUpdateRequester
import androidx.wear.watchface.complications.datasource.SuspendingComplicationDataSourceService
import com.weartools.phonebattcomp.complication.MobileBatteryComplicationService
import com.weartools.phonebattcomp.complication.WatchBatteryComplicationService

fun Context.openPlayStore() {
    try {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName")))
    } catch (e: ActivityNotFoundException) {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$packageName")))
    }
}

fun Context.updateComplication(service: Class<out SuspendingComplicationDataSourceService>) {
    ComplicationDataSourceUpdateRequester.create(this, ComponentName(this, service))
        .run { requestUpdateAll() }
}

fun Context.updateBatteriesComplications() {
    this.updateComplication(MobileBatteryComplicationService::class.java)
    this.updateComplication(WatchBatteryComplicationService::class.java)
}



