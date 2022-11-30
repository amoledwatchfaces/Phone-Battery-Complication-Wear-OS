package com.weartools.phonebattcomp

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class TempVoltageTapBroadcastReceiver : BroadcastReceiver() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    override fun onReceive(context: Context, intent: Intent) {

        val result = goAsync()

        scope.launch {
            try {
                Log.d(TAG, "UPDATING TEMP & VOLTAGE COMPLICATION")
                WatchTempComplicationService.updateComplication(context = context)
                WatchVoltageComplicationService.updateComplication(context = context)
            } finally {
                // Always call finish, even if cancelled
                result.finish()
            }
        }
    }

    companion object {
        private val TAG = TempVoltageTapBroadcastReceiver::class.java.simpleName
        private const val EXTRA_DATA_SOURCE_COMPONENT = "com.example.android.wearable.complicationsdatasource.action.DATA_SOURCE_COMPONENT"
        private const val EXTRA_COMPLICATION_ID = "com.example.android.wearable.complicationsdatasource.action.COMPLICATION_ID"

        fun getToggleIntent(context: Context, dataSource: ComponentName, complicationId: Int): PendingIntent {
            val intent = Intent(context, TempVoltageTapBroadcastReceiver::class.java)
            intent.putExtra(EXTRA_DATA_SOURCE_COMPONENT, dataSource)
            intent.putExtra(EXTRA_COMPLICATION_ID, complicationId)

            return PendingIntent.getBroadcast(
                context,
                complicationId,
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
        }
    }
}
