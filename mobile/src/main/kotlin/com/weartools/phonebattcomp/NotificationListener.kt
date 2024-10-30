package com.weartools.phonebattcomp

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import javax.inject.Inject

@AndroidEntryPoint
class NotificationListener : NotificationListenerService() {

    @Inject
    lateinit var dataRepository: DataStoreRepository
    @Inject
    lateinit var dataClient: DataClient

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    var syncEnabled = true

    private val mutex = Mutex()

    // Shared Job to cancel previous task before starting a new one
    private var notificationJob: Job? = null

    // Store the previous bitmaps
    private var lastBitmapsSent = mutableListOf<Bitmap>()

    companion object {
        private const val ICON_SIZE = 48
        private const val URI = "/foobar"
        private const val NOTIFICATIONS_UPDATE_KEY = "ts"
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        serviceScope.launch {
            // Set background service state to true
            dataRepository.setBackgroundServiceState(true)

            // Send notifications to watch when listener is connected
            debounceSendToWatch(updateTime = System.currentTimeMillis())

            // Send notifications to watch when WearListener requests it using flow collection
            ServiceCommunication.sendToWatchFlow.collect {
                debounceSendToWatch(
                updateTime = System.currentTimeMillis(),
                forceSend = true
                )
            }
        }
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        debounceSendToWatch(updateTime = System.currentTimeMillis())
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        debounceSendToWatch(updateTime = System.currentTimeMillis())
    }
    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    private fun debounceSendToWatch(
        updateTime: Long,
        forceSend: Boolean = false
    ) {
        notificationJob?.cancel()
        notificationJob = serviceScope.launch {

            if (!dataRepository.notificationsSync.first()) return@launch // Early exit if syncing is disabled

            delay(300)  // Debounce delay

            if (isActive) {
                sendToWatch(updateTime, forceSend)
            }
        }
    }

    suspend fun sendToWatch(
        updateTime: Long,
        forceSend: Boolean
    ) {
        mutex.withLock {

            /** Catch exceptions when onListenerConnected is not called before accessing notifications **/
            val notifications = try { activeNotifications } catch (e: Exception) {
                FirebaseCrashlytics.getInstance().recordException(e)
                return@withLock
            }

            // Initialize current bitmaps list.
            val currentBitmaps = mutableListOf<Bitmap>()

            // Get activeNotifications bitmaps and add unique ones to currentBitmaps.
            for (notification in notifications) {

                if (notification.isOngoing) { continue }

                notification.notification.smallIcon?.let {
                    it.loadDrawable(this@NotificationListener)?.let { drawable ->
                        drawableToBitmap(drawable)
                    }
                }?.let { bitmap ->
                    if (currentBitmaps.none { it.sameAs(bitmap) }) {
                        currentBitmaps.add(bitmap)
                    }
                }
            }

            /** If update is not forced and list sizes are same, check bitmaps pairs
             * to see if the bitmaps on their positions matches. If bitmaps match, return early
             * so there is no need to send new complication update, else, continue with sending
            **/
            if (!forceSend && currentBitmaps.size == lastBitmapsSent.size) {
                var allIdentical = true
                for (i in 0 until minOf(8, currentBitmaps.size)) {
                    if (!currentBitmaps[i].sameAs(lastBitmapsSent[i])) {
                        // If any compared bitmaps are different, break the loop
                        allIdentical = false
                        break
                    }
                }
                // If loop completes without breaking, all bitmaps are identical
                if (allIdentical) { return@withLock }
            }

            // Set currentBitmaps as lastBitmapsSent for next comparison
            lastBitmapsSent = currentBitmaps

            // Create DataMap Request
            val putDataMapReq = PutDataMapRequest.create(URI)
            putDataMapReq.dataMap.apply {
                currentBitmaps.forEachIndexed { i, bitmap ->
                    if (i < 8) {
                        // send only first 8 drawables
                        putByteArray("icon$i", bitmapToByteArray(bitmap))
                    } else {
                        // Add an empty byte array for each drawable above the first 8
                        putByteArray("icon$i", ByteArray(0))
                    }
                }
                putLong(NOTIFICATIONS_UPDATE_KEY, updateTime)
            }

            // Send DataMap to watch using DataClient
            try {
                dataClient.putDataItem(putDataMapReq.asPutDataRequest().setUrgent()).await()
            } catch (e: Exception) {
                FirebaseCrashlytics.getInstance().recordException(e)
                Log.e("NotificationListener", "Notification sending failed: ${e.message}")
            }
        }
    }

    private fun bitmapToByteArray(bitmap: Bitmap): ByteArray {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        return stream.toByteArray()
    }
    private fun drawableToBitmap(drawable: Drawable): Bitmap {
        val bitmap = Bitmap.createBitmap(ICON_SIZE, ICON_SIZE, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.setTint(Color.WHITE)
        drawable.draw(canvas)
        return bitmap
    }
}
