package com.weartools.phonebattcomp


import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.BatteryManager
import android.os.Build
import android.util.Log
import androidx.concurrent.futures.await
import androidx.core.app.NotificationManagerCompat
import androidx.datastore.core.DataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.wear.remote.interactions.RemoteActivityHelper
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.CapabilityInfo
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.Node
import com.google.android.gms.wearable.NodeClient
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.play.core.review.ReviewManager
import com.google.android.play.core.review.ReviewManagerFactory
import com.weartools.phonebattcomp.data.CalendarInfo
import com.weartools.phonebattcomp.data.UserPreferences
import com.weartools.phonebattcomp.data.UserPreferencesRepository
import com.weartools.phonebattcomp.di.ServiceCommunication
import com.weartools.phonebattcomp.receiver.BatteryStatusBroadcastReceiver
import com.weartools.phonebattcomp.receiver.CalendarContentObserver
import com.weartools.phonebattcomp.receiver.CalendarContentObserver.Companion.getAllCalendars
import com.weartools.phonebattcomp.utils.registerCalendarObserver
import com.weartools.phonebattcomp.utils.unregisterCalendarObserver
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject


@HiltViewModel
class MainViewModel @Inject constructor(
    private val capabilityClient: CapabilityClient,
    private val nodeClient: NodeClient,
    private val remoteActivityHelper: RemoteActivityHelper,
    private val dataClient: DataClient,
    private val batteryManager: BatteryManager,
    private val calendarContentObserver: CalendarContentObserver,
    private val dataStore: DataStore<UserPreferences>,
    preferences: UserPreferencesRepository,
) : ViewModel(){

    val preferences: StateFlow<UserPreferences> = preferences
        .getPreferences()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = UserPreferences()
        )

    private lateinit var reviewManager: ReviewManager

    private var wearNodesWithApp: Set<Node>? = null

    // Method to handle capability changes
    fun onCapabilityChanged(capabilityInfo: CapabilityInfo) {
        Log.d("CAPABILITY", "Capability changed: ${capabilityInfo.name}")
        // Handle the logic in ViewModel (e.g., finding wear devices)
        viewModelScope.launch {
            findAllWearDevices()
        }
    }

    private val _isMessageShown = MutableSharedFlow<Boolean>()
    private val loaderStateMutableStateFlow = MutableStateFlow(value = false)
    private val watchAvailableStateMutableStateFlow = MutableStateFlow(value = false)
    private val connectedNodesMutableStateFlow = MutableStateFlow(emptyList<Node>())
    private val commonNodesMutableStateFlow = MutableStateFlow(emptyList<Node>())

    // Backing property to hold the mutable state of the calendar list
    private val _calendarsStateFlow = MutableStateFlow<List<CalendarInfo>>(emptyList())
    val calendarsStateFlow: StateFlow<List<CalendarInfo>> = _calendarsStateFlow

    val isMessageShownFlow = _isMessageShown.asSharedFlow()
    val loaderStateStateFlow: StateFlow<Boolean> = loaderStateMutableStateFlow.asStateFlow()
    val watchAvailableStateStateFlow: StateFlow<Boolean> = watchAvailableStateMutableStateFlow.asStateFlow()
    val connectedNodesStateFlow: StateFlow<List<Node>?> = connectedNodesMutableStateFlow.asStateFlow()
    val commonNodesStateFlow: StateFlow<List<Node>?> = commonNodesMutableStateFlow.asStateFlow()

    var message: String = ""

    private fun setMessageShown(){
        viewModelScope.launch {
            _isMessageShown.emit(true)
        }
    }

    fun openPlayStoreOnWear(context: Context) {
        viewModelScope.launch {
            val intent = Intent(Intent.ACTION_VIEW)
                .addCategory(Intent.CATEGORY_BROWSABLE)
                .setData(Uri.parse(BuildConfig.PLAY_STORE_APP_URI))
            try {
                remoteActivityHelper.startRemoteActivity(targetIntent = intent,targetNodeId = null).await()
                message = context.getString(R.string.toast_check_wearable)
                _isMessageShown.emit(true)
            } catch (cancellationException: CancellationException) {
                // Request was cancelled normally
            } catch (throwable: Throwable) {
                message = "Play Store Request Failed. Wear device(s) may not support Play Store"
                _isMessageShown.emit(true)
            }
        }
    }

    suspend fun findAllWearDevices() {

        loaderStateMutableStateFlow.value = true
        try {
            val connectedNodes = nodeClient.connectedNodes.await()

            withContext(Dispatchers.Main) {
                connectedNodesMutableStateFlow.value = connectedNodes
                delay(1_000L)
                updateUI()
            }
        } catch (cancellationException: CancellationException) {
            // Request was cancelled normally
            loaderStateMutableStateFlow.value = false
        } catch (throwable: Throwable) {
            loaderStateMutableStateFlow.value = false

            message = "Node request failed to return any results."
            _isMessageShown.emit(true)
        }
    }

    private fun findWearDevicesWithApp() {
        Log.d(TAG, "findWearDevicesWithApp()")
        val capabilityInfoTask = capabilityClient.getCapability(BuildConfig.CAPABILITY_WEAR_APP, CapabilityClient.FILTER_ALL)

        capabilityInfoTask.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d(TAG, "Capability request succeeded.")
                wearNodesWithApp = task.result.nodes

                // Find common nodes
                val commonNodes = wearNodesWithApp?.filter {
                    it in (connectedNodesStateFlow.value?.toSet() ?: emptySet())
                }
                if (!commonNodes.isNullOrEmpty()) {
                    commonNodesMutableStateFlow.value = commonNodes
                    WearListener.sendBatteryInfoToWatch(
                        level = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY),
                        isCharging = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_STATUS) == BatteryManager.BATTERY_STATUS_CHARGING,
                        forceUpdate = true,
                        dataClient = dataClient,
                        batteryManager = batteryManager,
                    )
                    if (preferences.value.notificationsSync){
                        viewModelScope.launch {
                            ServiceCommunication.sendToWatchFlow.emit(Unit)
                        }
                    }
                }else {
                    commonNodesMutableStateFlow.value = emptyList()
                }
                loaderStateMutableStateFlow.value = false
            } else {
                Log.d(TAG, "Capability request failed to return any results.")
                loaderStateMutableStateFlow.value = false
            }
        }
    }

    @SuppressLint("StringFormatInvalid")
    private fun updateUI() {
        val allConnectedNodes = connectedNodesStateFlow.value
        when {
            allConnectedNodes.isNullOrEmpty() -> {
                loaderStateMutableStateFlow.value = false
                watchAvailableStateMutableStateFlow.value = false
                commonNodesMutableStateFlow.value = emptyList()
                message = "No wearable devices found…"
                setMessageShown()
            }
            else -> {
                watchAvailableStateMutableStateFlow.value = true
                message = "Wearable connected: ${allConnectedNodes.joinToString(", ") {it.displayName}}"
                setMessageShown()
                findWearDevicesWithApp()
            }
        }
    }

    fun showRateDialog(context: Context) {
        reviewManager = ReviewManagerFactory.create(context)
        val request = reviewManager.requestReviewFlow()
        request.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // We got the ReviewInfo object
                val reviewInfo = task.result
                val flow = reviewManager.launchReviewFlow(context as Activity, reviewInfo)
                flow.addOnCompleteListener {
                    context.startActivity(
                        Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("market://details?id=${context.packageName}")
                        )
                    )
                }
            }
        }
    }

    fun activateBatterySync(context: Context) {
        BatteryStatusBroadcastReceiver.subscribeToUpdates(context)

        viewModelScope.launch {
            try {
                val request = PutDataMapRequest.create(BATTERY_PATH).apply{
                    dataMap.putInt(BATTERY_KEY, batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY))
                    dataMap.putBoolean(IS_CHARGING_KEY, batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_STATUS) == BatteryManager.BATTERY_STATUS_CHARGING)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        dataMap.putLong(CHARGE_TIME_REMAINING_KEY, batteryManager.computeChargeTimeRemaining())
                    }
                }
                    .asPutDataRequest()
                    .setUrgent()
                dataClient.putDataItem(request)
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                Log.e(TAG, "Error while activating battery sync", e)
            }
        }
    }

    fun setActiveSyncState(state: Boolean) {
        viewModelScope.launch {
            dataStore.updateData { it.copy(activeSync = state) }
        }
        val request = PutDataMapRequest.create(ACTIVE_SYNC_PATH).apply{
            dataMap.putBoolean(ACTIVE_SYNC_KEY, state)
            dataMap.putLong("immediate-update", System.currentTimeMillis()) }
            .asPutDataRequest()
            .setUrgent()

        dataClient.putDataItem(request)
    }

    fun setCalendarSyncState(state: Boolean) {
        viewModelScope.launch {
            dataStore.updateData { it.copy(calendarSync = state) }
        }
    }

    fun setNotificationsSyncState(state: Boolean) {
        viewModelScope.launch {
            dataStore.updateData { it.copy(notificationsSync = state) }
            ServiceCommunication.sendToWatchFlow.emit(Unit)
        }
    }
    fun setBackgroundServiceState(state: Boolean) {
        viewModelScope.launch {
            dataStore.updateData { it.copy(backgroundServiceState = state) }
        }
    }

    fun isMyNotificationsServiceRunning(context: Context): Boolean {
        val isServiceRunning = NotificationManagerCompat.getEnabledListenerPackages(context).contains(BuildConfig.APPLICATION_ID)
        viewModelScope.launch {
            dataStore.updateData { it.copy(backgroundServiceState = isServiceRunning) }
        }
        return isServiceRunning
    }

    fun changeCalendarContentObserver(register: Boolean, context: Context){
        /** Register Content observer **/
        if (register){
            context.registerCalendarObserver(calendarContentObserver)
            // Send Events immediately
            viewModelScope.launch {
                CalendarContentObserver.queryAllFutureCalendarEventAndSend(
                    context, preferences.value.syncedCalendarsIds
                )
            }
        }
        else {
            context.unregisterCalendarObserver(calendarContentObserver)
        }

    }

    fun fetchCalendars(context: Context) {
        viewModelScope.launch {
            // Fetch the calendars in a background thread
            val calendars = withContext(Dispatchers.IO) {
                getAllCalendars(context)
            }
            // Update the StateFlow with the new list of calendars
            _calendarsStateFlow.value = calendars
        }
    }
    fun saveAllCalendarsOnEnabled(context: Context){
        viewModelScope.launch {
            val allCalendars = withContext(Dispatchers.IO) { getAllCalendars(context) }
            dataStore.updateData { it.copy(
                syncedCalendars = allCalendars,
                syncedCalendarsIds = allCalendars.map { calendar -> calendar.calendarId }.joinToString(",")
            ) }
        }
    }
    fun addSyncedCalendar(calendarInfo: CalendarInfo){
        viewModelScope.launch {
            val updatedCalendars = preferences.value.syncedCalendars.plus(calendarInfo)
            dataStore.updateData {
                it.copy(
                    syncedCalendars = updatedCalendars,
                    syncedCalendarsIds = updatedCalendars.map { calendar -> calendar.calendarId }.joinToString(",")
                )
            }
        }
    }
    fun removeSyncedCalendar(calendarInfo: CalendarInfo){
        viewModelScope.launch {
            val updatedCalendars = preferences.value.syncedCalendars.minus(calendarInfo)
            dataStore.updateData {
                it.copy(
                    syncedCalendars = updatedCalendars,
                    syncedCalendarsIds = updatedCalendars.map { calendar -> calendar.calendarId }.joinToString(",")
                )
            }
        }
    }


}