package com.weartools.phonebattcomp.data

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataMigration
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.dataStoreFile
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Singleton

// Proto DataStore --> New
private const val DATA_STORE_FILE_NAME = "user_prefs.pb"
// Preferences DataStore --> Old (not used)
private val Context.oldPreferencesDataStore by preferencesDataStore(name = "phone_batt_comp_prefs")

@InstallIn(SingletonComponent::class)
@Module
object ProtoDataStoreModule {
    @Singleton
    @Provides
    fun provideProtoDataStore(@ApplicationContext appContext: Context): DataStore<UserPreferences> {
        return DataStoreFactory.create(
            serializer = UserPreferencesSerializer,
            produceFile = { appContext.dataStoreFile(DATA_STORE_FILE_NAME) },
            corruptionHandler = ReplaceFileCorruptionHandler(produceNewData = { UserPreferences() }),
            migrations = listOf(
                object : DataMigration<UserPreferences> {
                    override suspend fun cleanUp() {}
                    override suspend fun migrate(currentData: UserPreferences): UserPreferences {

                        // TODO: Remove this migration with future versions

                        // Migrate from Preferences DataStore to Proto DataStore
                        // Get Old Preferences DataStore Instance
                        // if old preferences does not exist, return new UserPreferences instance
                        val oldPreferencesDataStore: Map<Preferences.Key<*>, Any> = try {
                            appContext.oldPreferencesDataStore.data.firstOrNull()?.asMap() ?: return UserPreferences()
                        } catch (e: Exception) {
                            Log.e("ProtoDataStoreModule", "Error reading old preferences: ${e.message}")
                            return UserPreferences()
                        }

                        // Return the updated UserPreferences object
                        return UserPreferences(

                            phoneBatteryLevel = oldPreferencesDataStore[intPreferencesKey("battery_level")] as? Int ?: 0,
                            phoneIsCharging = oldPreferencesDataStore[booleanPreferencesKey("is_charging")] as? Boolean ?: false,
                            phoneIsConnected = oldPreferencesDataStore[booleanPreferencesKey("is_connected")] as? Boolean ?: false,
                            afterMobileResult = oldPreferencesDataStore[booleanPreferencesKey("after_mobile_result")] as? Boolean ?: false,
                            lastUpdate = oldPreferencesDataStore[longPreferencesKey("last_update_time")] as? Long ?: 0L,
                            activeSync = oldPreferencesDataStore[booleanPreferencesKey("active_sync")] as? Boolean ?: false,
                            nodeName = oldPreferencesDataStore[stringPreferencesKey("node_name")] as? String ?:"Disconnected",
                            tempUnit = oldPreferencesDataStore[booleanPreferencesKey("temp_unit")] as? Boolean ?: true,
                            notificationsIconType = oldPreferencesDataStore[intPreferencesKey("notifications_icon_type")] as? Int ?: 1,

                            // Set version to prevent migration in the future
                            version = 1  // Update the version number to not migrate again
                        )
                    }
                    override suspend fun shouldMigrate(currentData: UserPreferences): Boolean {
                        //Check the current version and compare it to the desired version
                        //return true
                        return currentData.version < 1
                    }
                }
            )
        )
    }
}