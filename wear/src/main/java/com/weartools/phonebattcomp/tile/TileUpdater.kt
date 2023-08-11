package com.weartools.phonebattcomp.tile

import androidx.wear.tiles.TileUpdateRequester

class TileUpdater(private val tileUpdateRequester: TileUpdateRequester) {
    fun updatePhoneBatteryTile() {
        tileUpdateRequester
            .requestUpdate(PhoneBatteryTileService::class.java)
    }
    fun updateAll() {
        updatePhoneBatteryTile()
    }
}