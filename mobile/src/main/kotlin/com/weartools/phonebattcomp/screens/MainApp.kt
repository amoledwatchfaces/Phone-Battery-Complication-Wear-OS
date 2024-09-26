package com.weartools.phonebattcomp.screens

import android.annotation.SuppressLint
import android.content.Intent
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Watch
import androidx.compose.material.icons.filled.WatchOff
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navDeepLink
import com.weartools.phonebattcomp.MainViewModel
import com.weartools.phonebattcomp.R
import com.weartools.phonebattcomp.ui.components.BottomNavigationBar
import com.weartools.phonebattcomp.ui.components.Screen
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


@SuppressLint("UnrememberedMutableState")
@Composable
fun MainApp(
    viewModel: MainViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val view = LocalView.current
    val navController = rememberNavController()
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()


    val snackbarHostState = remember { SnackbarHostState()}

    val fabVisibility by derivedStateOf {
        listState.firstVisibleItemIndex == 0
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val isWearableConnected = viewModel.watchAvailableStateStateFlow.collectAsState()
    val commonNodesList = viewModel.commonNodesStateFlow.collectAsState()
    val connectedNodesList = viewModel.connectedNodesStateFlow.collectAsState()

    LaunchedEffect(Unit) {
        lifecycleOwner.repeatOnLifecycle(state = Lifecycle.State.CREATED) {
            viewModel.findAllWearDevices()
        }
    }
    LaunchedEffect(Unit){
        viewModel.isMessageShownFlow.collectLatest { if (it){
            snackbarHostState.showSnackbar(
                message = viewModel.message
            )
        } }
    }
    LaunchedEffect(Unit){
        viewModel.activeSync.collectLatest {
            if (it){ viewModel.activateBatterySync(context)}}
    }

    Scaffold(
        snackbarHost = {
                       SnackbarHost(
                           hostState = snackbarHostState
                       )
        },
        floatingActionButton = {
            if (isWearableConnected.value && (currentDestination?.route == Screen.Home.route)) {
                ExtendedFloatingActionButton(
                    icon = {
                        Icon(
                            tint = colorScheme.primaryContainer,
                            imageVector = Icons.Default.Watch,
                            contentDescription = null,)
                           },
                    text = {
                        Text(color = colorScheme.primaryContainer,text = stringResource(id = R.string.install_to_wearable)) },
                    containerColor = colorScheme.onPrimaryContainer,
                    onClick = { viewModel.openPlayStoreOnWear(context) },
                    expanded = (fabVisibility && commonNodesList.value.isNullOrEmpty())
                )
            }
            else if (currentDestination?.route == Screen.Home.route)
            {
                FloatingActionButton(
                    containerColor = colorScheme.primaryContainer,
                    onClick = { scope.launch {
                        viewModel.findAllWearDevices()
                    } }
                ){
                    Icon(
                        tint = colorScheme.onPrimaryContainer,
                        imageVector = Icons.Default.WatchOff,
                        contentDescription = null,
                    )
                }
            }
        },
        floatingActionButtonPosition = FabPosition.End,
        containerColor = colorScheme.surface,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            BottomNavigationBar(
                navController = navController,
                currentDestination = currentDestination
            )
        },
    )
    { padding ->
        NavHost(
            navController,
            startDestination = Screen.Home.route,
            Modifier.padding(padding)) {
            composable(Screen.Home.route) { HomeScreen(view, context, viewModel, scope, isWearableConnected, commonNodesList,connectedNodesList, listState) }
            composable(
                deepLinks = listOf(
                    navDeepLink{
                    uriPattern = "https://amoledwatchfaces.com/phonebattcomp"
                    action = Intent.ACTION_VIEW
                }),
                route = Screen.Experimental.route) {
                ExperimentalScreen(context, viewModel,lifecycleOwner ,isWearableConnected,commonNodesList, connectedNodesList )
            }
            composable(Screen.About.route) { InfoScreen(view, context, viewModel) }
        }
    }
}