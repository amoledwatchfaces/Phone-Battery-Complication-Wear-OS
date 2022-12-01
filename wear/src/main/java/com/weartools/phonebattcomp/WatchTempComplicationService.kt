package com.weartools.phonebattcomp

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.drawable.Icon
import android.os.BatteryManager
import android.util.Log
import androidx.preference.PreferenceManager
import androidx.wear.watchface.complications.data.*
import androidx.wear.watchface.complications.datasource.ComplicationDataSourceUpdateRequester
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import androidx.wear.watchface.complications.datasource.SuspendingComplicationDataSourceService

class WatchTempComplicationService : SuspendingComplicationDataSourceService() {

    override fun onComplicationActivated(
        complicationInstanceId: Int,
        type: ComplicationType)
    {
        super.onComplicationActivated(complicationInstanceId, type)
        Log.d(TAG, "activated: $complicationInstanceId")
    }

    override fun getPreviewData(type: ComplicationType): ComplicationData {
        return ShortTextComplicationData.Builder(
            text = PlainComplicationText.Builder(text = "35°").build(),
            contentDescription = PlainComplicationText.Builder(text = getString(R.string.temp_battery_text)).build())
            .setMonochromaticImage(MonochromaticImage.Builder(image = Icon.createWithResource(this, R.drawable.ic_temp)).build())
            .setTapAction(null)
            .build()
    }

    override suspend fun onComplicationRequest(request: ComplicationRequest): ComplicationData {
        Log.d(TAG, "Update: ${request.complicationInstanceId}")

        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        val tempunit = preferences.getBoolean(getString(R.string.temp_unit), true)
        val temp = (this.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))!!.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0)) / 10

        val complicationPendingIntent = TempVoltageTapBroadcastReceiver.getToggleIntent(
            this,
            ComponentName(this,javaClass),
            request.complicationInstanceId
        )

        val level = if (tempunit) temp else temp*9/5+32
        val unit = if (tempunit) "°C" else "°F"


        return when (request.complicationType) {

            ComplicationType.SHORT_TEXT -> ShortTextComplicationData.Builder(
                text = PlainComplicationText.Builder(text = "$level$unit").build(),
                contentDescription = PlainComplicationText.Builder(text = getString(R.string.temp_battery_at)+level+unit).build())
                .setMonochromaticImage(MonochromaticImage.Builder(image = Icon.createWithResource(this, R.drawable.ic_temp)).build())
                .setTapAction(complicationPendingIntent)
                .build()

            else -> {throw IllegalStateException("Unexpected value: ${request.complicationType}") }
        }
    }

    override fun onComplicationDeactivated(complicationInstanceId: Int) {
        super.onComplicationDeactivated(complicationInstanceId)
        Log.d(TAG, "Deactivated: $complicationInstanceId")
    }

    companion object {
        private val TAG = WatchTempComplicationService::class.java.simpleName
        @JvmStatic
        fun updateComplication(context: Context?) {
            Log.d(TAG, "Updating Watch Battery Temperature Complication")
            val componentName = ComponentName(context!!, WatchTempComplicationService::class.java)
            val req = ComplicationDataSourceUpdateRequester.create(context,componentName)
            req.requestUpdateAll()
        }
    }
}