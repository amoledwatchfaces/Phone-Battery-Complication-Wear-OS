package com.weartools.phonebattcomp.screens

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BluetoothConnected
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.wearable.Node
import com.weartools.phonebattcomp.MainViewModel
import com.weartools.phonebattcomp.R
import com.weartools.phonebattcomp.receiver.BatteryStatusBroadcastReceiver
import com.weartools.phonebattcomp.utils.askForNotificationAccess
import com.weartools.phonebattcomp.utils.openAmoledWebPage

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun ExperimentalScreen(
    context: Context,
    viewModel: MainViewModel,
    lifecycleOwner: LifecycleOwner,
    isWatchConnected: State<Boolean>,
    commonNodesList: State<List<Node>?>,
    connectedNodesList: State<List<Node>?>,
) {

    val listState = rememberLazyListState()
    val activeSyncState by viewModel.activeSync.collectAsState()
    val calendarSyncState by viewModel.calendarSync.collectAsState()
    val notificationsSyncState by viewModel.notificationsSync.collectAsState()
    val backgroundSyncState by viewModel.backgroundService.collectAsState()

    val permissionStateCalendar = rememberPermissionState(
        permission = "android.permission.READ_CALENDAR",
        onPermissionResult = {
            if (it){
                viewModel.setCalendarSyncState(true)
                viewModel.changeCalendarContentObserver(true, context)
            }
        }
    )


    LaunchedEffect(Unit) {
        lifecycleOwner.repeatOnLifecycle(state = Lifecycle.State.RESUMED) {
            viewModel.isMyNotificationsServiceRunning(context)
            if (permissionStateCalendar.status.isGranted.not()){
                viewModel.setCalendarSyncState(false)
            }
        }
    }

    Column(Modifier.fillMaxSize()) {
        // Show the top app bar on top level destinations.
        //val destination = appState.currentTopLevelDestination
        //if (destination != null) {
        CenterAlignedTopAppBar(
            title = { Text(fontWeight = FontWeight.Medium, text = "Experimental Settings") },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = Color.Transparent,
            )
        )

        LazyColumn(
            state = listState,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()

        ) {

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(0.9f).padding(bottom = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                )
                {
                    Icon(
                        modifier = Modifier.padding(end = 8.dp).size(18.dp),
                        imageVector = if (isWatchConnected.value.not()) Icons.Filled.BluetoothConnected
                        else Icons.Filled.BluetoothConnected,
                        contentDescription = "StatusIcon",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        text = if (isWatchConnected.value.not()) "Disconnected"
                        else  "Connected  •  "+"${connectedNodesList.value?.joinToString(", ") {it.displayName}}"
                    )
                }
            }

            item {
                ElevatedCard(
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 6.dp
                    ),
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .padding(bottom = 20.dp, top = 10.dp)
                ){

                    // Background Service
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column {
                            Text(
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Medium,
                                text = "Background Service")
                            Text(
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray,
                                fontWeight = FontWeight.Medium,
                                text = "Important - Notifications Access required")
                        }
                        Switch(
                            checked = backgroundSyncState,
                            onCheckedChange = {
                                viewModel.isMyNotificationsServiceRunning(context)
                                context.askForNotificationAccess()
                        }
                        )
                    }
                    // Active Sync Switch
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column {
                            Text(
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Medium,
                                text = "Active Sync")
                            Text(
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = Color.Gray,
                                text = "Battery charging status + live updates")
                        }

                        Switch(
                            enabled = backgroundSyncState && commonNodesList.value.isNullOrEmpty().not() && isWatchConnected.value,
                            checked = activeSyncState,
                            onCheckedChange = {
                            if (it){
                                viewModel.setActiveSyncState(true)
                                BatteryStatusBroadcastReceiver.subscribeToUpdates(context)
                            }
                            else {
                                viewModel.setActiveSyncState(false)
                                BatteryStatusBroadcastReceiver.unsubscribeFromUpdates(context)
                            }
                        })
                    }
                    // Notifications Sync Switch
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column {
                            Text(
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Medium,
                                text = "Notifications Sync")
                            Text(
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = Color.Gray,
                                text = "Only for Phone Notifications Complication")
                        }
                        Switch(
                            enabled = backgroundSyncState && commonNodesList.value.isNullOrEmpty().not() && isWatchConnected.value,
                            checked = notificationsSyncState,
                            onCheckedChange = {
                            if (it){
                                viewModel.setNotificationsSyncState(true)
                            }
                            else {
                                viewModel.setNotificationsSyncState(false)
                            }
                        })
                    }
                    // Calendar Events Sync Switch
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column {
                            Text(
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Medium,
                                text = "Calendar Events Sync")
                            Text(
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = Color.Gray,
                                text = "Upcoming Event & Event Timer Complication")
                        }

                        Switch(
                            enabled = backgroundSyncState && commonNodesList.value.isNullOrEmpty().not() && isWatchConnected.value,
                            checked = calendarSyncState,
                            onCheckedChange = {
                                if (it){
                                    if (permissionStateCalendar.status.isGranted){
                                        viewModel.setCalendarSyncState(true)
                                        viewModel.changeCalendarContentObserver(true, context)
                                    }
                                    else permissionStateCalendar.launchPermissionRequest()
                                }
                                else {
                                    viewModel.setCalendarSyncState(false)
                                    viewModel.changeCalendarContentObserver(false, context)
                                }
                            })
                    }
                }

            }
            item { TextButton(
                modifier = Modifier
                    .padding(top = 20.dp, bottom = 40.dp)
                    .wrapContentSize(),
                onClick = { context.openAmoledWebPage() }) {
                Text(
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    fontStyle = FontStyle.Italic,
                    text = stringResource(id = R.string.website),
                    color = Color.Gray)
            } }
        }
    }

}