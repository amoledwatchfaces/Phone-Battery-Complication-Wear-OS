/*
 * Copyright 2021 The Android Open Source Project
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
package com.weartools.phonebattcomp.tile

import android.os.Handler
import androidx.wear.protolayout.ResourceBuilders.Resources
import androidx.wear.tiles.EventBuilders
import androidx.wear.tiles.RequestBuilders.ResourcesRequest
import androidx.wear.tiles.RequestBuilders.TileRequest
import androidx.wear.tiles.TileBuilders.Tile
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.tiles.SuspendingTileService
import com.weartools.phonebattcomp.MobileListener
import com.weartools.phonebattcomp.data.DataRepository
import kotlinx.coroutines.flow.first

@OptIn(ExperimentalHorologistApi::class)
class PhoneBatteryTileService : SuspendingTileService() {
    private lateinit var repo2: DataRepository
    private lateinit var renderer: PhoneBatteryTileRenderer
    override suspend fun tileRequest(requestParams: TileRequest): Tile {
        MobileListener.sendPhoneBatteryRequest(0, applicationContext, true)
        repo2 = DataRepository(this)
        renderer = PhoneBatteryTileRenderer(this)


        val handler = Handler(mainLooper)
        handler.postDelayed({ getUpdater(this).requestUpdate(PhoneBatteryTileService::class.java) }, 1000)

        return renderer.renderTimeline(tileState(), requestParams)
    }

    private suspend fun tileState(): PhoneBatteryTileData.PhoneBatteryData {
        repo2 = DataRepository(this)
        renderer = PhoneBatteryTileRenderer(this)

        val batteryLevel = repo2.batteryLevel.first()
        val nodeName = repo2.nodeName.first()

        return PhoneBatteryTileData.PhoneBatteryData(batteryLevel, nodeName)
    }


    override suspend fun resourcesRequest(requestParams: ResourcesRequest): Resources {
        return renderer.produceRequestedResources(tileState(), requestParams)
    }

    override fun onTileAddEvent(requestParams: EventBuilders.TileAddEvent) {
        super.onTileAddEvent(requestParams)
        //runBlocking { repo2.storeTileSetState(true) }
        MobileListener.sendPhoneBatteryRequest(0, applicationContext, true)
    }
    override fun onTileEnterEvent(requestParams: EventBuilders.TileEnterEvent) {
        super.onTileEnterEvent(requestParams)
        MobileListener.sendPhoneBatteryRequest(0, applicationContext, true)
    }
/*
    override fun onTileRemoveEvent(requestParams: EventBuilders.TileRemoveEvent) {
        super.onTileRemoveEvent(requestParams)
        //runBlocking { repo2.storeTileSetState(false) }
    }

 */

}
