package com.weartools.phonebattcomp


import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.wear.remote.interactions.RemoteActivityHelper
import com.google.android.gms.tasks.Task
import com.google.android.gms.wearable.DataItem
import com.google.android.gms.wearable.Node
import com.google.android.gms.wearable.NodeClient
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import com.google.android.play.core.review.ReviewManager
import com.google.android.play.core.review.ReviewManagerFactory
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

class MainViewModel(
    private val dataRepository: DataRepository
) : ViewModel() {

    val activeSync = dataRepository.activeSync.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), false)
    val notificationsSync = dataRepository.notificationsSync.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), true)

    init {
        viewModelScope.launch {
            dataRepository.activeSync.distinctUntilChanged().collect {}
            dataRepository.notificationsSync.distinctUntilChanged().collect {}
        }
    }

    private lateinit var nodeClient: NodeClient
    private lateinit var remoteActivityHelper: RemoteActivityHelper
    private lateinit var reviewManager: ReviewManager

    private var allConnectedNodes: List<Node>? = null

    private val loaderStateMutableStateFlow = MutableStateFlow(value = false)
    val loaderStateStateFlow: StateFlow<Boolean> = loaderStateMutableStateFlow.asStateFlow()

    private val _isMessageShown = MutableSharedFlow<Boolean>()
    val isMessageShownFlow = _isMessageShown.asSharedFlow()

    var message: String = ""

    private fun setMessageShown(){
        viewModelScope.launch {
            _isMessageShown.emit(true)
        }
    }

    private val watchAvailableStateMutableStateFlow = MutableStateFlow(value = false)
    val watchAvailableStateStateFlow: StateFlow<Boolean> = watchAvailableStateMutableStateFlow.asStateFlow()

    fun openPlayStoreOnWear(context: Context) {
        //Log.d(TAG, "Opening Play Store listing on Watch")

        viewModelScope.launch {
            val intent = Intent(Intent.ACTION_VIEW)
                .addCategory(Intent.CATEGORY_BROWSABLE)
                .setData(Uri.parse("market://details?id=${context.packageName}"))
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

    suspend fun findAllWearDevices(context: Context) {

        loaderStateMutableStateFlow.value = true

        nodeClient = Wearable.getNodeClient(context)
        remoteActivityHelper = RemoteActivityHelper(context)

        //Toast.makeText(context, context.getString(R.string.toast_searching), Toast.LENGTH_SHORT).show()

        try {
            val connectedNodes = nodeClient.connectedNodes.await()

            withContext(Dispatchers.Main) {
                allConnectedNodes = connectedNodes
                delay(1_000L)
                updateUI(context)
            }
        } catch (cancellationException: CancellationException) {
            // Request was cancelled normally
            loaderStateMutableStateFlow.value = false
        } catch (throwable: Throwable) {
            loaderStateMutableStateFlow.value = false

            message = context.getString(R.string.toast_fail)
            _isMessageShown.emit(true)
            //Toast.makeText(context, context.getString(R.string.toast_fail), Toast.LENGTH_SHORT).show()
            Log.d(TAG, context.getString(R.string.toast_fail))
        }
    }

    @SuppressLint("StringFormatInvalid")
    private fun updateUI(context: Context) {
        //Log.d(TAG, "updateUI()")

        val allConnectedNodes = allConnectedNodes

        //Log.d(TAG, "allConnectedNotes =  $allConnectedNodes")

        when {
            allConnectedNodes == null -> {
                //Toast.makeText(context, context.getString(R.string.toast_no_devices), Toast.LENGTH_LONG).show()
                loaderStateMutableStateFlow.value = false
                watchAvailableStateMutableStateFlow.value = false
                message = context.getString(R.string.toast_no_devices)
                setMessageShown()
            }
            allConnectedNodes.isEmpty() -> {

                //Toast.makeText(context, context.getString(R.string.toast_no_devices), Toast.LENGTH_LONG).show()
                loaderStateMutableStateFlow.value = false
                watchAvailableStateMutableStateFlow.value = false
                message = context.getString(R.string.toast_no_devices)
                setMessageShown()
            }
            else -> {
                //Toast.makeText(context, "${context.getString(R.string.toast_wearable_connected)} ${allConnectedNodes.first().displayName}", Toast.LENGTH_LONG).show()
                loaderStateMutableStateFlow.value = false
                watchAvailableStateMutableStateFlow.value = true
                message = "${context.getString(R.string.toast_wearable_connected)} ${allConnectedNodes.first().displayName}"
                setMessageShown()
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

                val dataItemTask: Task<DataItem> = Wearable.getDataClient(context).putDataItem(request)
                dataItemTask
                    .addOnSuccessListener { dataItem -> Log.d(ContentValues.TAG,"WM: Sending Phone Battery request was successful: $dataItem") }
                    .addOnFailureListener { e -> Log.e(ContentValues.TAG,"WM: Sending request task failed!: $e") }
                    .addOnCompleteListener{task -> Log.d(ContentValues.TAG,"WM: Sending request Task complete!: $task")}

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

class MainViewModelFactory(
    private val dataRepository: DataRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(
                dataRepository = dataRepository
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}