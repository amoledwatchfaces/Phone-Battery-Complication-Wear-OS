package com.weartools.phonebattcomp.data

import android.content.Context
import androidx.datastore.core.DataMigration
import androidx.datastore.core.DataStore
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStoreFile
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton


interface Repository

// If ever needed, to migrate datastore
/*
private val Context.oldPreferencesDataStore by preferencesDataStore(
    name = "passive_data_mobile"
)
 */

@Module
@InstallIn(SingletonComponent::class)
abstract class DataStoreModule {

    @Binds
    @Singleton
    abstract fun bindDataStoreRepository(
        dataStoreRepository: DataStoreRepository
    ): Repository

    companion object {
        @Provides
        @Singleton
        fun provideDataStore(
            @ApplicationContext applicationContext: Context
        ): DataStore<Preferences> {
            return PreferenceDataStoreFactory.create(
                corruptionHandler = ReplaceFileCorruptionHandler(
                    produceNewData = {
                        emptyPreferences()
                    }
                ),
                scope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
                produceFile = { applicationContext.preferencesDataStoreFile("phone_batt_comp_prefs") },
                migrations = listOf(
                    object : DataMigration<Preferences> {
                        override suspend fun cleanUp() {
                            //Delete any data that is no longer needed
                        }
                        override suspend fun migrate(currentData: Preferences): Preferences {
                            // Handle the migration logic to remove the 'className' property
                            // Return the updated UserPreferences object
                            //val oldData = applicationContext.oldPreferencesDataStore.data.first().asMap()
                            //val currentMutablePrefs = currentData.toMutablePreferences()

                            return currentData
                        }

                        override suspend fun shouldMigrate(currentData: Preferences): Boolean {
                            //Check the current version and compare it to the desired version
                            //return true
                            var version = currentData[intPreferencesKey("preferencesVersion")]
                            version = version ?: 0

                            return version < 4
                        }
                    }
                )
            )
        }
    }
}