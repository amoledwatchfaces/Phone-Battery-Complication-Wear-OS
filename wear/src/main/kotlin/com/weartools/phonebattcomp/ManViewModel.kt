package com.weartools.phonebattcomp

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.concurrent.futures.await
import androidx.core.net.toUri
import androidx.datastore.core.DataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.wear.remote.interactions.RemoteActivityHelper
import androidx.wear.watchface.complications.datasource.ComplicationDataSourceUpdateRequester
import com.weartools.phonebattcomp.complication.MobileBatteryComplicationService
import com.weartools.phonebattcomp.complication.NotificationsIconsComplicationService
import com.weartools.phonebattcomp.complication.WatchBatteryComplicationService
import com.weartools.phonebattcomp.complication.WatchTempComplicationService
import com.weartools.phonebattcomp.data.UserPreferences
import com.weartools.phonebattcomp.data.UserPreferencesRepository
import com.weartools.phonebattcomp.widget.PhoneBatteryWidget
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.collections.copy
import kotlin.coroutines.cancellation.CancellationException

@HiltViewModel
class MainViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dataStore: DataStore<UserPreferences>,
    preferences: UserPreferencesRepository,
) : ViewModel(){

    private val _isPreferencesLoaded = MutableStateFlow(false)
    val isPreferencesLoaded = _isPreferencesLoaded.asStateFlow()

    val isCrashlyticsAvailable = BuildConfig.CRASHLYTICS_AVAILABLE

    val preferences: StateFlow<UserPreferences> = preferences
        .getPreferences()
        .onEach { _isPreferencesLoaded.value = true }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = UserPreferences()
        )


    fun toggleEnabled(context: Context) {
        viewModelScope.launch {
            val newEnabledStatus = !preferences.value.tempUnit
            dataStore.updateData { it.copy(tempUnit = newEnabledStatus) }
            updateComplication(context = context, WatchTempComplicationService::class.java)
        }
    }
    fun togglePercentage(context: Context) {
        viewModelScope.launch {
            val newEnabledStatus = !preferences.value.percentage
            dataStore.updateData { it.copy(percentage = newEnabledStatus) }
            updateComplication(context = context, MobileBatteryComplicationService::class.java)
            updateComplication(context = context, WatchBatteryComplicationService::class.java)
        }
    }
    fun toggleMaterialSymbols(value: Boolean, context: Context) {
        viewModelScope.launch {
            dataStore.updateData { it.copy(materialSymbols = value) }
            updateComplication(context = context, MobileBatteryComplicationService::class.java)
            updateComplication(context = context, WatchBatteryComplicationService::class.java)
        }
    }
    fun toggleChargingSymbol(value: Boolean, context: Context) {
        viewModelScope.launch {
            dataStore.updateData { it.copy(chargingSymbolInsideIcon = value) }
            updateComplication(context = context, MobileBatteryComplicationService::class.java)
            updateComplication(context = context, WatchBatteryComplicationService::class.java)
        }
    }
    fun storeNotificationIconType(context: Context, type: Int) {
        viewModelScope.launch {
            dataStore.updateData { it.copy(notificationsIconType = type) }
            updateComplication(context = context, NotificationsIconsComplicationService::class.java)
        }
    }

    fun setUseDynamicColor(useDynamicColor: Boolean){
        viewModelScope.launch {
            dataStore.updateData { it.copy(useDynamicColor = useDynamicColor) }
            PhoneBatteryWidget(dataStore).triggerUpdateAll(context)
        }
    }
    fun setCrashlytics(enabled: Boolean) {
        if (!isCrashlyticsAvailable) return
        viewModelScope.launch {
            dataStore.updateData { it.copy(crashlytics = enabled, crashlyticsNoticeAccepted = true) }
            runCatching {
                if (com.google.firebase.FirebaseApp.getApps(context).isNotEmpty()) {
                    com.google.firebase.crashlytics.FirebaseCrashlytics.getInstance().isCrashlyticsCollectionEnabled = enabled
                }
            }
        }
    }

    fun updateComplication(context: Context, clazz: Class<*>){
        ComplicationDataSourceUpdateRequester.create(
            context.applicationContext,
            ComponentName(context.applicationContext, clazz)
        ).run { requestUpdateAll() }
    }

    fun openExperimentalSettings(onOpened: (Boolean) -> Unit) {
        viewModelScope.launch {
            val remoteActivityHelper = RemoteActivityHelper(context)
            val intent = Intent(Intent.ACTION_VIEW)
                .addCategory(Intent.CATEGORY_BROWSABLE)
                .setData("https://amoledwatchfaces.com/phonebattcomp".toUri())

            try {
                remoteActivityHelper.startRemoteActivity(targetIntent = intent, targetNodeId = null).await()
                onOpened(true)
            } catch (_: CancellationException) {
                // Request was canceled normally
            } catch (_: Throwable) {
                onOpened(false)
            }
        }
    }

    fun openLinkOnPhone(link: String, onOpened: (Boolean) -> Unit) {
        viewModelScope.launch {
            val remoteActivityHelper = RemoteActivityHelper(context)
            val intent = Intent(Intent.ACTION_VIEW)
                .addCategory(Intent.CATEGORY_BROWSABLE)
                .setData(link.toUri())
            try {
                remoteActivityHelper.startRemoteActivity(targetIntent = intent, targetNodeId = null).await()
                onOpened(true)
            } catch (_: CancellationException) {
                // Request was canceled normally
            } catch (_: Throwable) {
                onOpened(false)
            }
        }
    }

}


