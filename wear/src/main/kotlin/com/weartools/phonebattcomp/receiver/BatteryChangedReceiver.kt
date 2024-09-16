package com.weartools.phonebattcomp.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_BATTERY_CHANGED
import android.os.BatteryManager
import com.weartools.phonebattcomp.complication.WatchBatteryComplicationService
import com.weartools.phonebattcomp.complication.watchBatteryLevel
import com.weartools.phonebattcomp.complication.watchBatteryLevelSaved
import com.weartools.phonebattcomp.utils.updateComplication

class BatteryChangedReceiver : BroadcastReceiver(){
    override fun onReceive(context: Context, intent: Intent) {
        //Log.i("WatchBatteryReceiver", "onReceive")
        if (intent.action != ACTION_BATTERY_CHANGED) return
        watchBatteryLevel = intent.getBatteryLevelPercent()
        if (watchBatteryLevel != watchBatteryLevelSaved) {
            //Log.i("WatchBatteryReceiver", "level: $batteryLevel")
            watchBatteryLevelSaved = watchBatteryLevel
            context.updateComplication(WatchBatteryComplicationService::class.java)
        }
    }
}
fun Intent.getBatteryLevelPercent(): Int {
    val level: Int = getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
    val scale: Int = getIntExtra(BatteryManager.EXTRA_SCALE, -1)
    return level * 100 / scale
}
fun getCurrentBatteryLevel(context: Context): Int {
    val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
    return batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
}