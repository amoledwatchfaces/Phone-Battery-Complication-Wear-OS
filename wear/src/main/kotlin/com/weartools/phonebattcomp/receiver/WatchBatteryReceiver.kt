package com.weartools.phonebattcomp.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_BATTERY_CHANGED
import android.content.Intent.ACTION_POWER_CONNECTED
import android.content.Intent.ACTION_POWER_DISCONNECTED
import android.content.IntentFilter
import android.os.BatteryManager
import android.util.Log
import com.weartools.phonebattcomp.complication.WatchBatteryComplicationService
import com.weartools.phonebattcomp.data.DataStoreRepository
import com.weartools.phonebattcomp.utils.updateComplication
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

@AndroidEntryPoint
class WatchBatteryReceiver : BroadcastReceiver() {

    @Inject
    lateinit var dataRepository: DataStoreRepository

    private var batteryLevelSaved: Int? = null
    private var isChargingSaved: Boolean? = null

    var batteryLevel: Int? = null
    var isCharging: Boolean? = null

    @OptIn(DelicateCoroutinesApi::class)
    override fun onReceive(context: Context, intent: Intent) {
        //Log.i("BSBR", "Received $intent")
        try {
            when (intent.action)
            {
                ACTION_BATTERY_CHANGED -> {
                    batteryLevel = intent.getBatteryLevelPercent()
                    isCharging = intent.getBatteryChargingStatus()
                }
                ACTION_POWER_CONNECTED -> {
                    batteryLevel = getCurrentBatteryLevel(context)
                    isCharging = true
                }
                ACTION_POWER_DISCONNECTED -> {
                    batteryLevel = getCurrentBatteryLevel(context)
                    isCharging = false
                }
            }

            GlobalScope.launch(Dispatchers.IO) {
                try {
                    if (batteryLevel != batteryLevelSaved || isCharging != isChargingSaved) {

                        val batteryLevelSafe = batteryLevel ?: batteryLevelSaved ?: 0
                        val isChargingSafe = isCharging ?: isChargingSaved ?: false

                        batteryLevelSaved = batteryLevel
                        isChargingSaved = isCharging

                        //Log.i("BSBR", "level: $batteryLevel")
                        //Log.i("BSBR", "isCharging: $isCharging")

                        dataRepository.apply {
                            storeWatchBatteryLevel(batteryLevelSafe)
                            setWatchChargingState(isChargingSafe)
                        }
                        context.updateComplication(WatchBatteryComplicationService::class.java)

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
        var isSubscribed = false
        private val receiver = WatchBatteryReceiver()

        fun subscribeToUpdates(context: Context) {
            if (!isSubscribed) {
                unsubscribeFromUpdates(context)

                val intentFilter = IntentFilter().apply {
                    addAction(ACTION_BATTERY_CHANGED)
                    addAction(ACTION_POWER_CONNECTED)
                    addAction(ACTION_POWER_DISCONNECTED)
                }
                context.registerReceiver(receiver, intentFilter)
            }
            isSubscribed = true
        }

        fun unsubscribeFromUpdates(context: Context) {
            try {
                context.unregisterReceiver(receiver)
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