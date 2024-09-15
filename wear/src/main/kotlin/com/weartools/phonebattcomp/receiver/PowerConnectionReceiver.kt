package com.weartools.phonebattcomp.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_POWER_CONNECTED
import android.content.Intent.ACTION_POWER_DISCONNECTED
import android.os.BatteryManager
import android.util.Log
import com.weartools.phonebattcomp.complication.watchIsCharging
import com.weartools.phonebattcomp.utils.updateBatteriesComplications
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PowerConnectionReceiver : BroadcastReceiver(){

    override fun onReceive(context: Context, intent: Intent) {
        Log.i("PowerConnectionReceiver", "onReceive")
        when (intent.action)
        {
            ACTION_POWER_CONNECTED -> {
                watchIsCharging = true
            }
            ACTION_POWER_DISCONNECTED -> {
                watchIsCharging = false
            }
        }
        context.updateBatteriesComplications()
    }
}

fun getCurrentBatteryChargingStatus(context: Context): Boolean {
    val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
    return batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_STATUS) == BatteryManager.BATTERY_STATUS_CHARGING
}