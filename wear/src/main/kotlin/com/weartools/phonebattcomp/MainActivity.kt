/*
 * Copyright 2022 amoledwatchfaces™
 * support@amoledwatchfaces.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.weartools.phonebattcomp

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.datastore.core.DataStore
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.CapabilityClient.OnCapabilityChangedListener
import com.google.android.gms.wearable.CapabilityInfo
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.Wearable
import com.weartools.phonebattcomp.MobileListener.Companion.sendPhoneBatteryRequest
import com.weartools.phonebattcomp.data.UserPreferences
import com.weartools.phonebattcomp.presentation.PhoneBatteryApp
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity(), OnCapabilityChangedListener  {

    @Inject
    lateinit var dataStore: DataStore<UserPreferences>

    @Inject lateinit var dataClient: DataClient
    private lateinit var capabilityClient: CapabilityClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()

        capabilityClient = Wearable.getCapabilityClient(this)
        sendPhoneBatteryRequest(0,dataClient,true)

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                launch {
                    // Initial request for devices with our capability, aka, our Wear app installed.
                    findWearDevicesWithApp()
                }
            }
        }

        setContent {
            PhoneBatteryApp(
                dataClient = dataClient
            )
        }
    }

    override fun onResume() {
        super.onResume()
        sendPhoneBatteryRequest(0, dataClient,true)
        capabilityClient.addListener(this, BuildConfig.CAPABILITY_MOBILE_APP)
    }

    override fun onPause() {
        super.onPause()
        capabilityClient.removeListener(this, BuildConfig.CAPABILITY_MOBILE_APP)

    }

    override fun onCapabilityChanged(capabilityInfo: CapabilityInfo) {
        if (capabilityInfo.name == BuildConfig.CAPABILITY_MOBILE_APP){
            //Log.d("MainActivity", "capabilityInfo.name matches ${BuildConfig.CAPABILITY_MOBILE_APP}")
            if (capabilityInfo.nodes.size > 0){
                //Log.d("MainActivity", "capability ${capabilityInfo.name} connected!")
                capabilityInfo.nodes.firstOrNull()?.let { node ->
                    lifecycleScope.launch {
                        dataStore.updateData { it.copy(nodeName = node.displayName) }
                    }
                }
                //sendPhoneBatteryRequest(0,dataClient, forceUpdate = true)
            }
            /*
            else {
                //Log.d("MainActivity", "capability ${capabilityInfo.name} disconnected!")
                phoneIsConnected = false
                afterMobileResult = true
                updateComplication(MobileBatteryComplicationService::class.java)
            }
            */
        }
    }


    private suspend fun findWearDevicesWithApp() {
        Log.d(TAG, "findWearDevicesWithApp()")

        try {
            val capabilityInfo = capabilityClient
                .getCapability(BuildConfig.CAPABILITY_MOBILE_APP, CapabilityClient.FILTER_REACHABLE)
                .await()

            withContext(Dispatchers.Main) {
                capabilityInfo.nodes.firstOrNull()?.let { node ->
                    dataStore.updateData { it.copy(nodeName = node.displayName) }
                }
            }
        } catch (cancellationException: CancellationException) {
            // Request was cancelled normally
            throw cancellationException
        } catch (throwable: Throwable) {
            Log.d(TAG, "Capability request failed to return any results.")
        }
    }
}



