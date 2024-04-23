package com.weartools.phonebattcomp


import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.wear.remote.interactions.RemoteActivityHelper
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.Node
import com.google.android.gms.wearable.NodeClient
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.play.core.review.ReviewManager
import com.google.android.play.core.review.ReviewManagerFactory
import com.weartools.phonebattcomp.data.DataStoreRepository
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
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.guava.await
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
    private val dataRepository: DataStoreRepository
) : ViewModel(){

    private lateinit var reviewManager: ReviewManager

    private var allConnectedNodes: List<Node>? = null
    private var wearNodesWithApp: Set<Node>? = null

    private val listener = CapabilityClient.OnCapabilityChangedListener { capabilityInfo ->
            // Handle capability changes here
            Log.d("CAPABILITY","Capability changed: ${capabilityInfo.name}")
            // You can use viewmodel events or other methods to notify the UI
            viewModelScope.launch{
                findAllWearDevices()
            }
        }

    private val _isMessageShown = MutableSharedFlow<Boolean>()
    private val loaderStateMutableStateFlow = MutableStateFlow(value = false)
    private val watchAvailableStateMutableStateFlow = MutableStateFlow(value = false)
    private val commonNodesMutableStateFlow = MutableStateFlow(emptyList<Node>())

    val isMessageShownFlow = _isMessageShown.asSharedFlow()
    val loaderStateStateFlow: StateFlow<Boolean> = loaderStateMutableStateFlow.asStateFlow()
    val watchAvailableStateStateFlow: StateFlow<Boolean> = watchAvailableStateMutableStateFlow.asStateFlow()
    val commonNodesStateFlow: StateFlow<List<Node>?> = commonNodesMutableStateFlow.asStateFlow()

    var message: String = ""

    val activeSync = dataRepository.activeSync.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), false)
    val notificationsSync = dataRepository.notificationsSync.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), true)

    init {
        viewModelScope.launch {
            capabilityClient.addListener(listener,BuildConfig.CAPABILITY_WEAR_APP)
            dataRepository.activeSync.distinctUntilChanged().collect {}
            dataRepository.notificationsSync.distinctUntilChanged().collect {}
        }
    }

    override fun onCleared() {
        super.onCleared()
        // Unregister listener to avoid leaks
        capabilityClient.removeListener(listener)
    }

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
                allConnectedNodes = connectedNodes
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
                        it in (allConnectedNodes?.toSet() ?: emptySet())
                    }
                if (!commonNodes.isNullOrEmpty()) {
                    commonNodesMutableStateFlow.value = commonNodes
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
        val allConnectedNodes = allConnectedNodes
        when {
            allConnectedNodes.isNullOrEmpty() -> {
                loaderStateMutableStateFlow.value = false
                watchAvailableStateMutableStateFlow.value = false
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
                    dataMap.putInt(BATTERY_KEY, BatteryStatusBroadcastReceiver.getCurrentBatteryLevel(context))
                    dataMap.putBoolean(IS_CHARGING_KEY, BatteryStatusBroadcastReceiver.getCurrentBatteryChargingStatus(context))
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

    companion object {
        private const val TAG = "MainViewModel"
    }

}