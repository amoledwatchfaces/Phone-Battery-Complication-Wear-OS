package com.weartools.phonebattcomp.utils

import android.content.ActivityNotFoundException
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.CalendarContract
import android.provider.Settings
import android.util.Log
import com.weartools.phonebattcomp.R
import com.weartools.phonebattcomp.receiver.CalendarContentObserver

fun Context.openPlayStore() {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName")))
        } catch (e: ActivityNotFoundException) {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$packageName")))
        }
    }

fun Context.openPlayStorePortfolio() {
    try {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://dev?id=5591589606735981545")))
    } catch (e: ActivityNotFoundException) {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/dev?id=5591589606735981545")))
    }
}
fun Context.openFacebookSocialLink() {
    try {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.social_facebook))))
    } catch (e: ActivityNotFoundException) {
        Log.d(TAG,"No Browser available")
    }
}
fun Context.openTelegramSocialLink() {
    try {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.social_telegram))))
    } catch (e: ActivityNotFoundException) {
        Log.d(TAG,"No Browser available")
    }
}

fun Context.openGithubSocialLink() {
    try {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.social_github))))
    } catch (e: ActivityNotFoundException) {
        Log.d(TAG,"No Browser available")
    }
}

fun Context.openBuyMeACoffeeSocialLink() {
    try {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.social_coffee))))
    } catch (e: ActivityNotFoundException) {
        Log.d(TAG,"No Browser available")
    }
}

fun Context.openPrivacyPolicyLink() {
    try {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.link_privacy))))
    } catch (e: ActivityNotFoundException) {
        Log.d(TAG,"No Browser available")
    }
}

fun Context.sendFeedbackEmail() {
    val emailIntent = Intent(Intent.ACTION_SENDTO,
        Uri.fromParts("mailto", getString(R.string.support), null))
    startActivity(Intent.createChooser(emailIntent, "Send email..."))
}

fun Context.openAmoledWebPage() {
    try {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.link_website))))
    } catch (e: ActivityNotFoundException) {
        Log.d(TAG,"No Browser available")
    }
}

fun Context.openGuideLink() {
    try {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.link_guide))))
    } catch (e: ActivityNotFoundException) {
        Log.d(TAG,"No Browser available")
    }
}

fun Context.askForNotificationAccess(){
        val action = Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS
        val intent = Intent(action)
        this.startActivity(intent)
}
fun Context.openTwitterSocialLink() {
    try {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.social_twitter))))
    } catch (e: ActivityNotFoundException) {
        Log.d(TAG,"No Browser available")
    }
}


fun Context.unregisterCalendarObserver(observer: CalendarContentObserver){
    try {
        contentResolver.unregisterContentObserver(observer)
    } catch (e: IllegalArgumentException) {
        // Handle the case where the observer wasn't registered
        Log.w("CalendarObserver", "Observer wasn't registered, ignoring.")
    }
}
fun Context.registerCalendarObserver(observer: CalendarContentObserver){
    unregisterCalendarObserver(observer)
    contentResolver.registerContentObserver(
        CalendarContract.Events.CONTENT_URI,
        true, // true for recursive monitoring of child URIs
        observer
    )
}