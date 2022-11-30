package com.weartools.phonebattcomp

import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.graphics.drawable.Icon
import android.os.BatteryManager
import android.provider.Settings
import android.util.Log
import androidx.wear.watchface.complications.data.*
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import androidx.wear.watchface.complications.datasource.SuspendingComplicationDataSourceService

class WatchBatteryComplicationService : SuspendingComplicationDataSourceService() {

    override fun onComplicationActivated(
        complicationInstanceId: Int,
        type: ComplicationType)
    {
        super.onComplicationActivated(complicationInstanceId, type)
        Log.d(TAG, "activated: $complicationInstanceId")

    }
    private fun openScreen(): PendingIntent? {
        val batteryIntent = Intent(Settings.ACTION_BATTERY_SAVER_SETTINGS)
        batteryIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        return PendingIntent.getActivity(this, 0, batteryIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }

    override fun getPreviewData(type: ComplicationType): ComplicationData {
        return ShortTextComplicationData.Builder(
            text = PlainComplicationText.Builder(text = "35%").build(),
            contentDescription = PlainComplicationText.Builder(text = getString(R.string.watch_battery_text)).build())
            .setMonochromaticImage(MonochromaticImage.Builder(image = Icon.createWithResource(this, R.drawable.ic_watch)).build())
            .setTapAction(null)
            .build()
    }

    override suspend fun onComplicationRequest(request: ComplicationRequest): ComplicationData {
        Log.d(TAG, "Update: ${request.complicationInstanceId}")

        val level = this.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))!!.getIntExtra(BatteryManager.EXTRA_LEVEL, 0)

        return when (request.complicationType) {

            ComplicationType.RANGED_VALUE -> RangedValueComplicationData.Builder(
                value = level.toFloat(),
                min = 0f,
                max = 100f,
                contentDescription = PlainComplicationText.Builder(text = getString(R.string.watch_battery_at)+"$level%").build())
                .setText(PlainComplicationText.Builder(text = "$level%").build())
                .setMonochromaticImage(MonochromaticImage.Builder(image = Icon.createWithResource(this, R.drawable.ic_watch)).build())
                .setTapAction(openScreen())
                .build()

            ComplicationType.SHORT_TEXT -> ShortTextComplicationData.Builder (
                text = PlainComplicationText.Builder(text = "$level%").build(),
                contentDescription = PlainComplicationText.Builder(text = getString(R.string.watch_battery_at)+"$level%").build())
                .setMonochromaticImage(MonochromaticImage.Builder(image = Icon.createWithResource(this, R.drawable.ic_watch)).build())
                .setTapAction(openScreen())
                .build()

            else -> {throw IllegalStateException("Unexpected value: ${request.complicationType}") }
        }
    }

    override fun onComplicationDeactivated(complicationInstanceId: Int) {
        super.onComplicationDeactivated(complicationInstanceId)
        Log.d(TAG, "Deactivated: $complicationInstanceId")
    }

    companion object {
        private val TAG = WatchBatteryComplicationService::class.java.simpleName
    }
}


