package com.weartools.phonebattcomp.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_POWER_CONNECTED
import android.content.Intent.ACTION_POWER_DISCONNECTED
import android.os.BatteryManager
import android.util.Log
import com.google.android.gms.wearable.DataClient
import com.weartools.phonebattcomp.WearListener
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class PowerConnectionReceiver : BroadcastReceiver(){

    @Inject lateinit var batteryManager: BatteryManager
    @Inject lateinit var dataClient: DataClient

    override fun onReceive(context: Context, intent: Intent) {
        Log.i("PowerConnectionReceiver", "onReceive")
        when (intent.action)
        {
            ACTION_POWER_CONNECTED -> {
                WearListener.sendBatteryInfoToWatch(
                    level = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY),
                    isCharging = true,
                    forceUpdate = true,
                    dataClient = dataClient
                )
            }
            ACTION_POWER_DISCONNECTED -> {
                WearListener.sendBatteryInfoToWatch(
                    level = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY),
                    isCharging = false,
                    forceUpdate = true,
                    dataClient = dataClient
                )
            }
        }
    }
}