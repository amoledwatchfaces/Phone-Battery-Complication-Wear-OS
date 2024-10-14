package com.weartools.phonebattcomp

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.PutDataMapRequest
import com.weartools.phonebattcomp.data.DataStoreRepository
import com.weartools.phonebattcomp.di.ServiceCommunication
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import javax.inject.Inject

@AndroidEntryPoint
class NotificationListener : NotificationListenerService() {

    @Inject
    lateinit var dataRepository: DataStoreRepository
    @Inject
    lateinit var dataClient: DataClient

    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    companion object {
        private const val ICON_SIZE = 48
        private const val URI = "/foobar"
    }

    override fun onCreate() {
        super.onCreate()
        serviceScope.launch {
            // Collect the SharedFlow when notifications need to be sent
            ServiceCommunication.sendToWatchFlow.collect { sendToWatch() }
        }
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        serviceScope.launch {
            dataRepository.setBackgroundServiceState(true)
            if (dataRepository.notificationsSync.first()) { sendToWatch() }
        }
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        //Log.w(ContentValues.TAG, "Notification Posted!")
        serviceScope.launch {
            if (dataRepository.notificationsSync.first()) sendToWatch()
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        //Log.w(ContentValues.TAG, "Notification Removed!")
        serviceScope.launch {
            if (dataRepository.notificationsSync.first()) sendToWatch()
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    fun sendToWatch() {
        //Log.i("NotificationListener", "Sending notifications icon to watch")
        //Log.i("NotificationListener", "Notifications size: ${activeNotifications.size}")
        val putDataMapReq = PutDataMapRequest.create(URI)
        val bitmaps = ArrayList<Bitmap>()
        for (notification in activeNotifications) {

            if (notification.isOngoing) {
                continue
            }

            val bitmap = notification.notification.smallIcon?.let {
                it.loadDrawable(this)?.let {
                    drawable -> drawableToBitmap(drawable)
                }
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
        // Ensure notifications are always delivered
        putDataMapReq.dataMap.putLong("/immediate-update", System.currentTimeMillis())
        val putDataReq = putDataMapReq.asPutDataRequest()
        dataClient.putDataItem(putDataReq)
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
