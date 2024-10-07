package com.weartools.phonebattcomp.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.google.android.gms.wearable.DataClient
import com.weartools.phonebattcomp.MobileListener
import javax.inject.Inject

class BootCompleteBroadcastReceiver : BroadcastReceiver() {

    @Inject
    lateinit var dataClient: DataClient

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED || intent.action == "android.intent.action.QUICKBOOT_POWERON") {
            MobileListener.sendNotificationsRequest(dataClient)
        }
    }
}