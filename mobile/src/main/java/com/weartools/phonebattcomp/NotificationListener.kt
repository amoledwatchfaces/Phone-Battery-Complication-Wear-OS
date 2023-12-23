package com.weartools.phonebattcomp

import android.annotation.SuppressLint
import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import java.io.ByteArrayOutputStream
import java.util.*

class NotificationListener : NotificationListenerService() {

    companion object {
        private const val ICON_SIZE = 48
        private const val URI = "/foobar"
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        sendToWatch()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        Log.w(ContentValues.TAG, "Notification Posted!")
        sendToWatch()
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        Log.w(ContentValues.TAG, "Notification Removed!")
        sendToWatch()
    }

    @SuppressLint("VisibleForTests")
    private fun sendToWatch() {
        val putDataMapReq = PutDataMapRequest.create(URI)
        val bitmaps = ArrayList<Bitmap>()
        for (notification in activeNotifications) {

            if (notification.isOngoing) {
                continue
            }

            val bitmap = notification.notification.smallIcon.loadDrawable(this)?.let {
                drawableToBitmap(
                    it
                )
            }
            if (!bitmaps.any { it.sameAs(bitmap) }) {
                if (bitmap != null) {
                    bitmaps.add(bitmap)
                }
            }
        }
        for ((i, bitmap) in bitmaps.withIndex()) {
            putDataMapReq.dataMap.putByteArray("icon$i", bitmapToByteArray(bitmap))
        }
        putDataMapReq.setUrgent()
        val putDataReq = putDataMapReq.asPutDataRequest()
        Wearable.getDataClient(this).putDataItem(putDataReq)
    }

    private fun drawableToBitmap(drawable: Drawable): Bitmap {
        val bitmap = Bitmap.createBitmap(ICON_SIZE, ICON_SIZE, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }

    private fun bitmapToByteArray(bitmap: Bitmap): ByteArray {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        return stream.toByteArray()
    }
}
