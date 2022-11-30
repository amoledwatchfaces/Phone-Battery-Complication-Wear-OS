package com.weartools.phonebattcomp

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.preference.PreferenceManager
import androidx.wear.remote.interactions.RemoteActivityHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class MobileBatteryComplicationTapBroadcastReceiver : BroadcastReceiver() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    override fun onReceive(context: Context, intent: Intent) {

        val result = goAsync()
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        val hasMobileApp = preferences.getBoolean(context.getString(R.string.key_pref_has_mobile_app), false)

        scope.launch {
            try {
                Log.d(TAG, "UPDATING BATTERY COMPLICATION")
                MobileBatteryComplicationService.updateBatteryComplication(context = context)
                        if (!hasMobileApp) {
                            openAppStoreOnPhone(context = context)
                            Log.d(TAG, "OPENING APP ON PHONE IF NEEDED")
                            Toast.makeText(
                                context,
                                context.getString(R.string.install_companion),
                                Toast.LENGTH_LONG
                            ).show()
                            }
            } finally {
                result.finish()
            }
        }
    }

    fun openAppStoreOnPhone(context: Context) {
        val remoteActivityHelper = RemoteActivityHelper(context)
            val intentAndroid = Intent(Intent.ACTION_VIEW)
                .addCategory(Intent.CATEGORY_BROWSABLE)
                .setData(Uri.parse(MainActivity.PLAY_STORE_APP_URI))
            remoteActivityHelper.startRemoteActivity(intentAndroid,targetNodeId = null)
    }

    companion object {
        private val TAG = MobileBatteryComplicationTapBroadcastReceiver::class.java.simpleName
        private const val EXTRA_DATA_SOURCE_COMPONENT = "com.example.android.wearable.complicationsdatasource.action.DATA_SOURCE_COMPONENT"
        private const val EXTRA_COMPLICATION_ID = "com.example.android.wearable.complicationsdatasource.action.COMPLICATION_ID"

        fun getToggleIntent(context: Context, dataSource: ComponentName, complicationId: Int): PendingIntent {
            val intent = Intent(context, MobileBatteryComplicationTapBroadcastReceiver::class.java)
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
