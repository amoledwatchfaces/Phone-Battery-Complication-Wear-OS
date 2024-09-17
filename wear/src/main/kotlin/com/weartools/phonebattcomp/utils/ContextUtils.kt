package com.weartools.phonebattcomp.utils

import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.wear.remote.interactions.RemoteActivityHelper
import androidx.wear.watchface.complications.datasource.ComplicationDataSourceUpdateRequester
import androidx.wear.watchface.complications.datasource.SuspendingComplicationDataSourceService
import com.weartools.phonebattcomp.BuildConfig
import com.weartools.phonebattcomp.R
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

fun Context.openAppStoreOnPhone() {
    val remoteActivityHelper = RemoteActivityHelper(this)
    val intentAndroid = Intent(Intent.ACTION_VIEW)
        .addCategory(Intent.CATEGORY_BROWSABLE)
        .setData(Uri.parse(BuildConfig.PLAY_STORE_APP_URI))
    remoteActivityHelper.startRemoteActivity(intentAndroid,targetNodeId = null)
    Toast.makeText(this, this.getString(R.string.check_phone), Toast.LENGTH_LONG).show()
}


