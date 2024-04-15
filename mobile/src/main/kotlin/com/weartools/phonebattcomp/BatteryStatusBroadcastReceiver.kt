package com.weartools.phonebattcomp

import android.content.BroadcastReceiver
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_BATTERY_CHANGED
import android.content.Intent.ACTION_POWER_CONNECTED
import android.content.Intent.ACTION_POWER_DISCONNECTED
import android.content.IntentFilter
import android.os.BatteryManager
import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.android.gms.wearable.DataItem
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

class BatteryStatusBroadcastReceiver : BroadcastReceiver() {

    private var lastBatteryLevelPercentSent: Int? = null

    @OptIn(DelicateCoroutinesApi::class)
    override fun onReceive(context: Context, intent: Intent) {
        try {
            val maybeBatteryPercentage = when(intent.action) {
                ACTION_BATTERY_CHANGED -> intent.getBatteryLevelPercent()
                ACTION_POWER_CONNECTED, ACTION_POWER_DISCONNECTED -> 0
                else -> null
            }

            if (maybeBatteryPercentage == null) {
                Log.w("BatteryStatusBroadcastReceiver", "Unable to extract battery level")
                return
            }

            GlobalScope.launch {
                try {
                    if (maybeBatteryPercentage != lastBatteryLevelPercentSent) {

                        val levelToSend =  if (maybeBatteryPercentage == 0) lastBatteryLevelPercentSent?:0 else maybeBatteryPercentage

                        val chargingStatus = when(intent.action){
                            ACTION_BATTERY_CHANGED -> intent.getBatteryChargingStatus()
                                else -> getCurrentBatteryChargingStatus(context)
                        }

                        Log.i(TAG,"Active Sync: Sending Battery Level: $maybeBatteryPercentage")
                        Log.i(TAG,"Active Sync: Is Charging?: $chargingStatus")

                        val request = PutDataMapRequest.create(BATTERY_PATH).apply{
                            dataMap.putInt(BATTERY_KEY, levelToSend)
                            dataMap.putBoolean(IS_CHARGING_KEY, chargingStatus)
                            }
                            .asPutDataRequest()
                            .setUrgent()

                        val dataItemTask: Task<DataItem> = Wearable.getDataClient(context).putDataItem(request)
                        dataItemTask
                            .addOnSuccessListener { dataItem -> Log.d(TAG,"BSBR: Sending Phone Battery request was successful: $dataItem") }
                            .addOnFailureListener { e -> Log.e(TAG,"BSBR: request task failed!: $e") }
                            .addOnCompleteListener{task -> Log.d(TAG,"BSBR: request Task complete!: $task")}

                        lastBatteryLevelPercentSent = maybeBatteryPercentage
                    }

                } catch (t: Throwable) {
                    if (t is CancellationException) {
                        throw t
                    }

                    Log.e("BatteryStatusBroadcastReceiver", "Error sending battery level", t)
                }
            }
        } catch (t: Throwable) {
            Log.e("BatteryStatusBroadcastReceiver", "Error computing battery level", t)
        }
    }

    companion object {
        private var isSubscribed = false
        private val receiver = BatteryStatusBroadcastReceiver()

        fun subscribeToUpdates(context: Context) {
            if (!isSubscribed) {
                unsubscribeFromUpdates(context)

                val intentFilter = IntentFilter().apply {
                    addAction(ACTION_BATTERY_CHANGED)
                    addAction(ACTION_POWER_CONNECTED)
                    addAction(ACTION_POWER_DISCONNECTED)
                }
                context.applicationContext.registerReceiver(receiver, intentFilter)
            }
            isSubscribed = true
        }

        fun unsubscribeFromUpdates(context: Context) {
            try {
                context.applicationContext.unregisterReceiver(receiver)
            } catch (e: IllegalArgumentException) {
                // Receiver not registered, ignoring
            }

            isSubscribed = false
        }

        fun getCurrentBatteryLevel(context: Context): Int {
            val batteryStatus: Intent = context.registerReceiver(null, IntentFilter(ACTION_BATTERY_CHANGED))
                ?: throw RuntimeException("Unable to get battery status, null intent")

            val level = batteryStatus.getBatteryLevelPercent()
            Log.i(TAG, "BSBR: Current Battery Level: $level")

            return level
        }

        fun getCurrentBatteryChargingStatus(context: Context): Boolean {
            val batteryStatus: Intent? = IntentFilter(ACTION_BATTERY_CHANGED).let { ifilter ->
                context.registerReceiver(null, ifilter)
            }

            val status: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
            val isCharging: Boolean = status == BatteryManager.BATTERY_STATUS_CHARGING
                    || status == BatteryManager.BATTERY_STATUS_FULL

            Log.i(TAG, "BSBR: Battery Charging?: $isCharging")

            return isCharging
        }
    }
}

private fun Intent.getBatteryLevelPercent(): Int {
    val level: Int = getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
    val scale: Int = getIntExtra(BatteryManager.EXTRA_SCALE, -1)
    return level * 100 / scale
}
private fun Intent.getBatteryChargingStatus(): Boolean {
    val status: Int = getIntExtra(BatteryManager.EXTRA_STATUS, -1)
    val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL
    Log.i(TAG, "BSBR: Battery Charging?: $isCharging")
    return isCharging
}