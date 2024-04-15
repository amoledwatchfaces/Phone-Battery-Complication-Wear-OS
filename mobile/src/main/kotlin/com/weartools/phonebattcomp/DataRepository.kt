package com.weartools.phonebattcomp

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map


private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "passive_data_mobile")
class DataRepository(private val context: Context) {

    val activeSync: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[ACTIVE_SYNC] ?: false
    }
    suspend fun setActiveSyncState(state: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[ACTIVE_SYNC] = state
        }
    }
    companion object {
        private val ACTIVE_SYNC = booleanPreferencesKey("active_sync")
    }
}
