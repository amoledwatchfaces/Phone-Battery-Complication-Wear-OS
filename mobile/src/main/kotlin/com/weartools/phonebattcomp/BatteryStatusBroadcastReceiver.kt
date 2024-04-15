package com.weartools.phonebattcomp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_BATTERY_CHANGED
import android.content.Intent.ACTION_POWER_CONNECTED
import android.content.Intent.ACTION_POWER_DISCONNECTED
import android.content.IntentFilter
import android.os.BatteryManager
import android.util.Log
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

class BatteryStatusBroadcastReceiver : BroadcastReceiver() {

    private var lastBatteryLevelPercentSent: Int? = null
    private var lastChargingStatus: Boolean? = null

    @OptIn(DelicateCoroutinesApi::class)
    override fun onReceive(context: Context, intent: Intent) {
        //Log.i("BSBR", "Received $intent")
        try {
            var batteryLevel: Int? = null
            var isCharging: Boolean? = null
            when (intent.action)
            {
                ACTION_BATTERY_CHANGED -> {
                    batteryLevel = intent.getBatteryLevelPercent()
                    isCharging = intent.getBatteryChargingStatus()
                }
                ACTION_POWER_CONNECTED -> {
                    batteryLevel = context.registerReceiver(null, IntentFilter(ACTION_BATTERY_CHANGED))?.getBatteryLevelPercent()
                    isCharging = true
                }
                ACTION_POWER_DISCONNECTED -> {
                    batteryLevel = context.registerReceiver(null, IntentFilter(ACTION_BATTERY_CHANGED))?.getBatteryLevelPercent()
                    isCharging = false }
            }

            /** Won't be using this as I'm already handling null values with -- & isCharging false **/
            /*
            if (batteryLevel == null && isCharging == null) {
                Log.w("BSBR", "Unable to extract battery status")
                return
            }
            */

            GlobalScope.launch {
                try {
                    if (batteryLevel != lastBatteryLevelPercentSent || isCharging != lastChargingStatus) {

                        //Log.i("BSBR","Active Sync: Sending Battery Level: $batteryLevel")
                        //Log.i("BSBR","Active Sync: Is Charging?: $isCharging")

                        val request = PutDataMapRequest.create(BATTERY_PATH).apply{
                            dataMap.putInt(BATTERY_KEY, batteryLevel?: lastBatteryLevelPercentSent?:0)
                            dataMap.putBoolean(IS_CHARGING_KEY, isCharging?: lastChargingStatus?: false)
                            }
                            .asPutDataRequest()
                            .setUrgent()

                        lastBatteryLevelPercentSent = batteryLevel
                        lastChargingStatus = isCharging

                        Wearable.getDataClient(context).putDataItem(request)
                        /** We don't need to listen on completion **/
                        /*
                        val dataItemTask = Wearable.getDataClient(context).putDataItem(request)
                        dataItemTask
                            .addOnSuccessListener { dataItem -> Log.d("BSBR","Sending Phone Battery request was successful: $dataItem") }
                            .addOnFailureListener { e -> Log.e("BSBR","Request task failed!: $e") }
                            .addOnCompleteListener{task -> Log.d("BSBR",Request Task complete!: $task")}

                        */
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
            return batteryStatus.getBatteryLevelPercent()
        }

        fun getCurrentBatteryChargingStatus(context: Context): Boolean {
            val batteryStatus: Intent? = IntentFilter(ACTION_BATTERY_CHANGED).let { ifilter ->
                context.registerReceiver(null, ifilter)
            }
            val status: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
            return status == BatteryManager.BATTERY_STATUS_CHARGING
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
    return status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL
}