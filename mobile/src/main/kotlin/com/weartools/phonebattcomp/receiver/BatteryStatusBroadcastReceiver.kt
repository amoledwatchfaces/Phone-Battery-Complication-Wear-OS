package com.weartools.phonebattcomp.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.util.Log
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.PutDataMapRequest
import com.weartools.phonebattcomp.BATTERY_KEY
import com.weartools.phonebattcomp.BATTERY_PATH
import com.weartools.phonebattcomp.IS_CHARGING_KEY
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

@AndroidEntryPoint
class BatteryStatusBroadcastReceiver : BroadcastReceiver() {

    @Inject
    lateinit var dataClient: DataClient

    private var lastBatteryLevelPercentSent: Int? = null
    private var lastChargingStatus: Boolean? = null

    var batteryLevel: Int? = null
    var isCharging: Boolean? = null

    @OptIn(DelicateCoroutinesApi::class)
    override fun onReceive(context: Context, intent: Intent) {
        //Log.i("BSBR", "Received $intent")
        try {
            when (intent.action)
            {
                Intent.ACTION_BATTERY_CHANGED -> {
                    batteryLevel = intent.getBatteryLevelPercent()
                    isCharging = intent.getBatteryChargingStatus()
                }
                Intent.ACTION_POWER_CONNECTED -> {
                    batteryLevel = getCurrentBatteryLevel(context)
                    isCharging = true
                }
                Intent.ACTION_POWER_DISCONNECTED -> {
                    batteryLevel = getCurrentBatteryLevel(context)
                    isCharging = false
                }
            }

            GlobalScope.launch(Dispatchers.IO) {
                try {
                    if (batteryLevel != lastBatteryLevelPercentSent || isCharging != lastChargingStatus) {

                        val batteryLevelSafe = batteryLevel ?: lastBatteryLevelPercentSent ?: 0
                        val isChargingSafe = isCharging ?: lastChargingStatus ?: false

                        //Log.i("BSBR","Active Sync: Sending Battery Level: $batteryLevel")
                        //Log.i("BSBR","Active Sync: Is Charging?: $isCharging")

                        val request = PutDataMapRequest.create(BATTERY_PATH).apply{
                            dataMap.putInt(BATTERY_KEY, batteryLevelSafe)
                            dataMap.putBoolean(IS_CHARGING_KEY, isChargingSafe)
                            }
                            .asPutDataRequest()
                            .setUrgent()

                        lastBatteryLevelPercentSent = batteryLevel
                        lastChargingStatus = isCharging

                        dataClient.putDataItem(request)
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
                    addAction(Intent.ACTION_BATTERY_CHANGED)
                    addAction(Intent.ACTION_POWER_CONNECTED)
                    addAction(Intent.ACTION_POWER_DISCONNECTED)
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
            val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
            return batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
        }
        fun getCurrentBatteryChargingStatus(context: Context): Boolean {
            val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
            return batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_STATUS) == BatteryManager.BATTERY_STATUS_CHARGING
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