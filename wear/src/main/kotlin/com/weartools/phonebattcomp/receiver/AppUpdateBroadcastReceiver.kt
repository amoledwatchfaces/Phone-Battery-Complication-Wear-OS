package com.weartools.phonebattcomp.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.weartools.phonebattcomp.utils.updateBatteriesComplications

class AppUpdateBroadcastReceiver : BroadcastReceiver(){

    override fun onReceive(context: Context, intent: Intent?) {
        Log.i("AppUpdateBroadcastReceiver", "onReceive")

        if( intent?.action != "android.intent.action.MY_PACKAGE_REPLACED" ) {
            return
        }
        context.updateBatteriesComplications()
    }
}