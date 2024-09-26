package com.weartools.phonebattcomp.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.weartools.phonebattcomp.utils.updateCalendarComplications

class DateChangedBroadcastReceiver : BroadcastReceiver(){

    override fun onReceive(context: Context, intent: Intent) {
        Log.i("DateChangedBroadcastReceiver", "onReceive")

        if (intent.action in listOf(Intent.ACTION_TIME_CHANGED,Intent.ACTION_TIMEZONE_CHANGED, Intent.ACTION_DATE_CHANGED))
            context.updateCalendarComplications()
        else return
    }
}