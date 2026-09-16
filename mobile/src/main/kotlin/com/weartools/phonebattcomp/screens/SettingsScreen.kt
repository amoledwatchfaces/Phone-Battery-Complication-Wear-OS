package com.weartools.phonebattcomp.screens

import android.content.Context
import android.view.SoundEffectConstants
import android.view.View
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BluetoothConnected
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.createBitmap
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import com.google.android.gms.wearable.Node
import com.weartools.phonebattcomp.MainViewModel
import com.weartools.phonebattcomp.R
import com.weartools.phonebattcomp.data.UserPreferences
import com.weartools.phonebattcomp.utils.askForNotificationAccess
import com.weartools.phonebattcomp.utils.openAmoledWebPage

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun SettingsScreen(
    view: View,
    context: Context,
    preferences: State<UserPreferences>,
    viewModel: MainViewModel,
    isWatchConnected: State<Boolean>,
    isWearableApiSupported: State<Boolean>,
    commonNodesList: State<List<Node>?>,
    connectedNodesList: State<List<Node>?>,
    permissionStateCalendar: PermissionState
) {

    val listState = rememberLazyListState()

    // Calendars
    val allCalendars by viewModel.calendarsStateFlow.collectAsState()
    var openCalendars by remember { mutableStateOf(false) }

    // App Filter
    var openAppFilter by remember { mutableStateOf(false) }

    var showCrashlyticsDialog by remember { mutableStateOf(false) }

    LaunchedEffect(preferences.value.crashlyticsNoticeAccepted) {
        if (!preferences.value.crashlyticsNoticeAccepted && viewModel.isCrashlyticsAvailable) {
            showCrashlyticsDialog = true
        }
    }


    Column(Modifier.fillMaxSize()) {
        // Show the top app bar on top level destinations.
        //val destination = appState.currentTopLevelDestination
        //if (destination != null) {
        CenterAlignedTopAppBar(
            title = { Text(fontWeight = FontWeight.Medium, text = "Settings") },
            colors = TopAppBarDefaults.topAppBarColors(
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
                        text = if (isWearableApiSupported.value.not()) "Wearable API not supported"
                        else if (isWatchConnected.value.not()) "Disconnected"
                        else  "Connected  •  "+"${connectedNodesList.value?.joinToString(", ") {it.displayName}}"
                    )
                }
            }
            item {
                ElevatedCard(
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
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
                        Column(
                            modifier = Modifier.weight(0.8f)
                        ) {
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
                            modifier = Modifier.weight(0.2f),
                            checked = preferences.value.backgroundServiceState,
                            onCheckedChange = {
                                viewModel.isMyNotificationsServiceRunning(context)
                                context.askForNotificationAccess()
                        }
                        )
                    }
                    // Notifications Sync Switch
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(
                            modifier = Modifier.weight(0.8f)
                        ) {
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
                            modifier = Modifier.weight(0.2f),
                            enabled = isWearableApiSupported.value && preferences.value.backgroundServiceState && commonNodesList.value.isNullOrEmpty().not() && isWatchConnected.value,
                            checked = preferences.value.notificationsSync,
                            onCheckedChange = {
                            if (it){
                                viewModel.setNotificationsSyncState(true)
                            }
                            else {
                                viewModel.setNotificationsSyncState(false)
                            }
                        })
                    }
                    if (preferences.value.notificationsSync) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 2.dp),
                            horizontalArrangement = Arrangement.Start,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedButton(
                                onClick = {
                                    viewModel.fetchInstalledApps(context)
                                    openAppFilter = true
                                },
                                enabled = isWearableApiSupported.value && preferences.value.backgroundServiceState && isWatchConnected.value
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.FilterList,
                                    contentDescription = "Filter Apps",
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Filter Apps")
                            }
                        }
                    }
                    // Media Playback Sync Switch
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(
                            modifier = Modifier.weight(0.8f)
                        ) {
                            Text(
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Medium,
                                text = "Media Playback Sync")
                            Text(
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = Color.Gray,
                                text = "Now Playing Complication support")
                        }
                        Switch(
                            modifier = Modifier.weight(0.2f),
                            enabled = isWearableApiSupported.value && preferences.value.backgroundServiceState && commonNodesList.value.isNullOrEmpty().not() && isWatchConnected.value,
                            checked = preferences.value.mediaPlaybackSync,
                            onCheckedChange = {
                                if (it){
                                    viewModel.setMediaPlaybackSyncState(true)
                                }
                                else {
                                    viewModel.setMediaPlaybackSyncState(false)
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
                        Column(
                            modifier = Modifier.weight(0.8f)
                        ) {
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
                            modifier = Modifier.weight(0.2f),
                            enabled = isWearableApiSupported.value && preferences.value.backgroundServiceState && commonNodesList.value.isNullOrEmpty().not() && isWatchConnected.value,
                            checked = preferences.value.calendarSync,
                            onCheckedChange = {
                                if (it){
                                    if (permissionStateCalendar.status.isGranted){
                                        viewModel.saveAllCalendarsOnEnabled(context)
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
                    OutlinedButton(
                        modifier = Modifier.padding(start = 16.dp, bottom = 8.dp),
                        enabled = isWearableApiSupported.value && preferences.value.calendarSync && commonNodesList.value.isNullOrEmpty().not() && isWatchConnected.value,
                        onClick = {
                            viewModel.fetchCalendars(context)
                            openCalendars=openCalendars.not()
                            view.playSoundEffect(SoundEffectConstants.CLICK)
                        }
                    ) {
                        Icon(
                            modifier = Modifier.padding(end = 8.dp),
                            imageVector = Icons.Filled.EditCalendar,
                            contentDescription = null,
                        )
                        Text(
                            fontWeight = FontWeight.Medium,
                            style = MaterialTheme.typography.bodyMedium,
                            text = stringResource(id = R.string.calendars)
                        )
                    }
                    // Crashlytics Toggle
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(
                            modifier = Modifier.weight(0.8f)
                        ) {
                            Text(
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Medium,
                                text = stringResource(id = R.string.crash_reports))
                            Text(
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = Color.Gray,
                                text = "Help us identify and fix issues")
                        }
                        Switch(
                            modifier = Modifier.weight(0.2f),
                            enabled = viewModel.isCrashlyticsAvailable,
                            checked = preferences.value.crashlytics && viewModel.isCrashlyticsAvailable,
                            onCheckedChange = { viewModel.setCrashlytics(it) }
                        )
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
        if (openCalendars){
            BasicAlertDialog(
                onDismissRequest = {
                    viewModel.changeCalendarContentObserver(true, context)
                    openCalendars = false
                    return@BasicAlertDialog
                }
            ) {
                Surface(
                    modifier = Modifier.wrapContentWidth().wrapContentHeight(),
                    shape = MaterialTheme.shapes.large,
                    tonalElevation = 3.dp
                ) {
                    LazyColumn(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        item {
                            Text(
                                modifier = Modifier.padding(bottom = 8.dp),
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Medium,
                                text = "Select calendars to sync"
                            )
                        }
                        item { HorizontalDivider(color=Color.DarkGray) }
                        items(allCalendars.size) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(allCalendars[it].displayName)
                                Checkbox(
                                    checked = allCalendars[it] in preferences.value.syncedCalendars,
                                    onCheckedChange = { checked ->
                                        if (checked){
                                            viewModel.addSyncedCalendar(allCalendars[it])
                                        }
                                        else {
                                            viewModel.removeSyncedCalendar(allCalendars[it])
                                        }
                                    }
                                )
                            }

                        }
                        item { Spacer(modifier = Modifier.height(8.dp)) }
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                TextButton(
                                    onClick = {
                                        viewModel.changeCalendarContentObserver(true, context)
                                        openCalendars = false
                                    }
                                ) {
                                    Text("Confirm")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCrashlyticsDialog) {
        BasicAlertDialog(
            onDismissRequest = { showCrashlyticsDialog = false }
        ) {
            Surface(
                modifier = Modifier.wrapContentWidth().wrapContentHeight(),
                shape = MaterialTheme.shapes.large,
                tonalElevation = 3.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = stringResource(id = R.string.crashlytics_title),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(id = R.string.crashlytics_message),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = {
                            viewModel.setCrashlytics(false)
                            showCrashlyticsDialog = false
                        }) {
                            Text("Not now")
                        }
                        TextButton(onClick = {
                            viewModel.setCrashlytics(true)
                            showCrashlyticsDialog = false
                        }) {
                            Text("Enable")
                        }
                    }
                }
            }
        }
    }

    if (openAppFilter) {
        val installedApps by viewModel.installedAppsStateFlow.collectAsState()
        var tempDisabledApps by remember(preferences.value.disabledAppPackages) {
            mutableStateOf(preferences.value.disabledAppPackages)
        }
        val allSelected = installedApps.isNotEmpty() && installedApps.none { tempDisabledApps.contains(it.packageName) }

        BasicAlertDialog(
            onDismissRequest = { openAppFilter = false }
        ) {
            Surface(
                modifier = Modifier.wrapContentWidth().wrapContentHeight(),
                shape = MaterialTheme.shapes.large,
                tonalElevation = 3.dp
            ) {
                Column(
                    modifier = Modifier.padding(16.dp).fillMaxWidth()
                ) {
                    // Fixed Title
                    Text(
                        modifier = Modifier.padding(bottom = 8.dp),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        text = "Filter App Notifications"
                    )
                    HorizontalDivider(color = Color.DarkGray)

                    // Fixed Select All Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (allSelected) "Unselect All" else "Select All",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                        Checkbox(
                            checked = allSelected,
                            onCheckedChange = { selectAll ->
                                tempDisabledApps = if (selectAll) {
                                    emptySet()
                                } else {
                                    installedApps.map { it.packageName }.toSet()
                                }
                            }
                        )
                    }
                    HorizontalDivider(color = Color.DarkGray)

                    // Scrollable App List
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f, fill = false)
                            .heightIn(max = 350.dp)
                    ) {
                        items(installedApps.size) { index ->
                            val app = installedApps[index]
                            val isEnabled = !tempDisabledApps.contains(app.packageName)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(0.8f)
                                ) {
                                    AppDrawableImage(drawable = app.icon, appName = app.appName)
                                    Text(
                                        text = app.appName,
                                        style = MaterialTheme.typography.bodyMedium,
                                        maxLines = 1
                                    )
                                }
                                Checkbox(
                                    checked = isEnabled,
                                    onCheckedChange = { checked ->
                                        tempDisabledApps = if (checked) {
                                            tempDisabledApps - app.packageName
                                        } else {
                                            tempDisabledApps + app.packageName
                                        }
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Fixed Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = { openAppFilter = false }
                        ) {
                            Text("Dismiss")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        TextButton(
                            onClick = {
                                viewModel.setDisabledAppPackages(tempDisabledApps)
                                openAppFilter = false
                            }
                        ) {
                            Text("Confirm")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AppDrawableImage(drawable: Drawable?, appName: String) {
    val imageBitmap = remember(drawable) {
        drawable?.run {
            val bitmap = createBitmap(48, 48)
            val canvas = Canvas(bitmap)
            setBounds(0, 0, 48, 48)
            draw(canvas)
            bitmap.asImageBitmap()
        }
    }
    imageBitmap?.let {
        Image(
            bitmap = it,
            contentDescription = appName,
            modifier = Modifier.padding(end = 8.dp).size(24.dp)
        )
    }
}
