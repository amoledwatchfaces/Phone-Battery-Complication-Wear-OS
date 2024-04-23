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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.CapabilityClient.OnCapabilityChangedListener
import com.google.android.gms.wearable.CapabilityInfo
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.Wearable
import com.weartools.phonebattcomp.data.DataStoreRepository
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

    @Inject lateinit var dataRepository: DataStoreRepository
    @Inject lateinit var dataClient: DataClient
    private lateinit var capabilityClient: CapabilityClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()

        capabilityClient = Wearable.getCapabilityClient(this)

        MobileListener.sendPhoneBatteryRequest(0,dataClient,true)

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
        MobileListener.sendPhoneBatteryRequest(0, dataClient,true)
        capabilityClient.addListener(this, BuildConfig.CAPABILITY_MOBILE_APP)
    }

    override fun onPause() {
        super.onPause()
        capabilityClient.removeListener(this, BuildConfig.CAPABILITY_MOBILE_APP)

    }

    override fun onCapabilityChanged(capabilityInfo: CapabilityInfo) {
        Log.d(TAG, "onCapabilityChanged(): $capabilityInfo")
        capabilityInfo.nodes.firstOrNull()?.let {
            lifecycleScope.launch {
                dataRepository.storeNodeName(it.displayName)
            }
        }
    }

    private suspend fun findWearDevicesWithApp() {
        Log.d(TAG, "findWearDevicesWithApp()")

        try {
            val capabilityInfo = capabilityClient
                .getCapability(BuildConfig.CAPABILITY_MOBILE_APP, CapabilityClient.FILTER_REACHABLE)
                .await()

            withContext(Dispatchers.Main) {
                capabilityInfo.nodes.firstOrNull()?.let {
                    dataRepository.storeNodeName(it.displayName)
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



