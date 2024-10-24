package com.weartools.phonebattcomp

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.weartools.phonebattcomp.data.DataStoreRepository
import com.weartools.phonebattcomp.di.ServiceCommunication
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import java.util.concurrent.ExecutionException
import javax.inject.Inject

@AndroidEntryPoint
class NotificationListener : NotificationListenerService() {

    @Inject
    lateinit var dataRepository: DataStoreRepository
    @Inject
    lateinit var dataClient: DataClient

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    // Shared Job to cancel previous task before starting a new one
    private var notificationJob: Job? = null

    // Store the previous bitmaps
    private var lastBitmapsSent: List<Bitmap>? = null

    companion object {
        private const val ICON_SIZE = 48
        private const val URI = "/foobar"
        private const val NOTIFICATIONS_UPDATE_KEY = "update-time"
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        serviceScope.launch {
            // Set background service state to true
            dataRepository.setBackgroundServiceState(true)

            // Send notifications to watch when listener is connected
            sendToWatch(updateTime = System.currentTimeMillis())

            // Send notifications to watch when WearListener requests it using flow collection
            ServiceCommunication.sendToWatchFlow.collect { sendToWatch(updateTime = System.currentTimeMillis()) }
        }
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        //Log.w(ContentValues.TAG, "Notification Posted!")
        sendToWatch(updateTime = System.currentTimeMillis())
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        //Log.w(ContentValues.TAG, "Notification Removed!")
        sendToWatch(updateTime = System.currentTimeMillis())
    }
    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    fun sendToWatch(
        updateTime: Long
    ) {
        // Cancel the previous job and start a new one
        notificationJob?.cancel()
        notificationJob = serviceScope.launch {
            // Check if notifications should be synced
            if (!dataRepository.notificationsSync.first()) return@launch

            /** Catch exceptions when onListenerConnected is not called before accessing notifications **/
            val notifications = try { activeNotifications } catch (e: Exception) {
                FirebaseCrashlytics.getInstance().recordException(e)
                return@launch
            }

            val currentBitmaps = ArrayList<Bitmap>()

            for (notification in notifications) {

                if (notification.isOngoing) {
                    continue
                }

                val bitmap = notification.notification.smallIcon?.let {
                    it.loadDrawable(this@NotificationListener)?.let {
                            drawable -> drawableToBitmap(drawable)
                    }
                }

                if (!currentBitmaps.any { it.sameAs(bitmap) }) {
                    if (bitmap != null) {
                        currentBitmaps.add(bitmap)
                    }
                }
            }
            // Take max 9 icons (Notifications Row allows 8 & (+) sign)
            val bitmapsToSend = currentBitmaps.take(9)

            // Compare currentBitmaps with lastBitmaps
            if (bitmapsToSend == lastBitmapsSent) { return@launch }

            // Save current bitmaps as last sent bitmaps
            lastBitmapsSent = bitmapsToSend

            // Create DataMap Request
            val putDataMapReq = PutDataMapRequest.create(URI)
            putDataMapReq.dataMap.apply {
                for ((i, bitmap) in bitmapsToSend.withIndex()) {
                    putByteArray("icon$i", bitmapToByteArray(bitmap))
                }
                putLong(NOTIFICATIONS_UPDATE_KEY, updateTime)
            }

            try {
                dataClient.putDataItem(putDataMapReq.asPutDataRequest().setUrgent()).await()
            }
            catch (e: ExecutionException) {
                FirebaseCrashlytics.getInstance().recordException(e)
                Log.e("NotificationListener", "Notification sending failed: ${e.message}")
            }
            catch (e: InterruptedException) {
                FirebaseCrashlytics.getInstance().recordException(e)
                Log.e("NotificationListener", "Notification sending failed: ${e.message}")
            }
        }
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
