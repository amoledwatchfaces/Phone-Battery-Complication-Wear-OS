package com.weartools.phonebattcomp

import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.util.Log
import com.google.android.gms.wearable.CapabilityInfo
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService
import com.weartools.phonebattcomp.SendMessageToWearService.Companion.sndMSGWear

class WearListener : WearableListenerService() {
    override fun onMessageReceived(messageEvent: MessageEvent) {
        val path = messageEvent.path
        Log.d("WearListener", path)
        if (path.contains("/request_battery")) {
            val level = batteryLevel
            sndMSGWear(this, "/battery_level/$level")
        }
    }

    private val batteryLevel: Int
        get() {
            val batteryStatus = registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
            val scale = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
            return 100 * level / scale
        }

    override fun onDataChanged(dataEvents: DataEventBuffer) {}
    override fun onCapabilityChanged(capabilityInfo: CapabilityInfo) {
        super.onCapabilityChanged(capabilityInfo)
        Log.e("capability", capabilityInfo.name)
        if (capabilityInfo.nodes.size == 0) {
            Log.e("WearListener", "No devices")
        }
        for (node in capabilityInfo.nodes) {
            Log.e("WearListener", node.displayName + " : " + node.isNearby)
        }
    }
}