package com.weartools.phonebattcomp.screens

import android.content.Context
import android.view.SoundEffectConstants
import android.view.View
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ContactSupport
import androidx.compose.material.icons.filled.BluetoothConnected
import androidx.compose.material.icons.filled.Shop
import androidx.compose.material.icons.filled.Shop2
import androidx.compose.material.icons.filled.SmartDisplay
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.google.android.gms.wearable.Node
import com.weartools.phonebattcomp.MainViewModel
import com.weartools.phonebattcomp.R
import com.weartools.phonebattcomp.ui.components.ImageSwitchBox
import com.weartools.phonebattcomp.ui.components.NavigationDefaults
import com.weartools.phonebattcomp.utils.openAmoledWebPage
import com.weartools.phonebattcomp.utils.openGuideLink
import com.weartools.phonebattcomp.utils.openPlayStore
import com.weartools.phonebattcomp.utils.openPlayStorePortfolio
import com.weartools.phonebattcomp.utils.sendFeedbackEmail
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun HomeScreen(
    view: View,
    context: Context,
    viewModel: MainViewModel,
    scope: CoroutineScope,
    isWatchConnected: State<Boolean>,
    commonNodesList: State<List<Node>?>,
    connectedNodesList: State<List<Node>?>,
    listState: LazyListState
) {
    val state by viewModel.loaderStateStateFlow.collectAsState()
    val nodesListEmpty = commonNodesList.value.isNullOrEmpty()

    val pullRefreshState = rememberPullRefreshState(
        refreshing = state,
        onRefresh = { scope.launch {
            viewModel.findAllWearDevices()
        } }
    )


    Column(
        Modifier.fillMaxSize()
    ) {
        CenterAlignedTopAppBar(
            actions = {
                IconButton(
                    modifier = Modifier.padding(end = 5.dp),
                    onClick = { context.openPlayStore() }) {
                    Icon(
                        imageVector = Icons.Default.Shop,
                        contentDescription = "Play Store",
                        tint = colorScheme.onPrimaryContainer
                    )
                }
            },
            title = {
                Text(
                    fontWeight = FontWeight.Medium,
                    text = stringResource(id = R.string.app_name)
                )
            },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = colorScheme.background,
            )
        )

        LazyColumn(
            state = listState,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .pullRefresh(pullRefreshState)

        ) {
            // Show the top app bar on top level destinations.
            //val destination = appState.currentTopLevelDestination
            //if (destination != null) {
            item {
                Box(
                    modifier = Modifier.offset(y=(-10).dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(
                        modifier = Modifier
                            .size(224.dp)
                            .shadow(
                                clip = true,
                                shape = CircleShape,
                                elevation = 12.dp,
                                ambientColor = colorScheme.onPrimaryContainer,
                                spotColor = colorScheme.onPrimaryContainer
                            ),
                    ) {
                        drawCircle(
                            color = Color.Black,
                            radius = size.minDimension / 2,
                            center = center
                        )
                    }
                    ImageSwitchBox()
                    Image(
                        modifier = Modifier
                            .size(300.dp),
                        alignment = Alignment.Center,
                        painter = painterResource(id = R.drawable.frame_800_pixel),
                        contentDescription = "Frame"
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(0.9f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                )
                {
                    Icon(
                        modifier = Modifier.padding(end = 8.dp).size(18.dp),
                        imageVector = if (isWatchConnected.value.not()) Icons.Filled.BluetoothConnected
                        else Icons.Filled.BluetoothConnected,
                        contentDescription = "StatusIcon",
                        tint = colorScheme.onSurfaceVariant
                    )
                    Text(
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = colorScheme.onSurfaceVariant,
                        text = if (isWatchConnected.value.not()) "Disconnected  •  Pull to refresh"
                        else  "Connected  •  "+"${connectedNodesList.value?.joinToString(", ") {it.displayName}}"
                    )
                }
            }

            item {
                ElevatedCard(
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .padding(bottom = 10.dp, top = 20.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp, bottom = 0.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            modifier = Modifier.padding(start = 16.dp),
                            text = stringResource(id = R.string.welcome),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center,
                        )
                        TextButton(
                            enabled = (nodesListEmpty),
                            modifier = Modifier.padding(end = 4.dp),
                            onClick = {
                                view.playSoundEffect(SoundEffectConstants.CLICK)
                                context.openGuideLink()})
                        {
                            if (nodesListEmpty) {
                                Icon(
                                    modifier = Modifier.padding(end = 10.dp),
                                    imageVector = Icons.Filled.SmartDisplay,
                                    contentDescription = null,
                                )
                                Text(
                                    textDecoration = TextDecoration.Underline,
                                    text = stringResource(id = R.string.installation_guides),
                                    color = colorScheme.onPrimaryContainer)
                            }
                        }
                    }

                    Text(
                        text = stringResource(id = R.string.intro),
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(16.dp),
                        textAlign = TextAlign.Left,
                    )
                    if(isWatchConnected.value && nodesListEmpty){
                        Text(
                            text = stringResource(id = R.string.connected),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(16.dp),
                            textAlign = TextAlign.Left,
                        )
                    }
                    else if (isWatchConnected.value && nodesListEmpty.not()){
                        Text(
                            text = "App is installed on your wear device(s):",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 0.dp),
                            textAlign = TextAlign.Left,
                        )
                        Text(
                            text = "${commonNodesList.value?.joinToString(", ") {it.displayName}}",
                            color = colorScheme.onPrimaryContainer,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 16.dp),
                            textAlign = TextAlign.Left,
                        )
                        Text(
                            text = stringResource(id = R.string.uninstall),
                            style = MaterialTheme.typography.bodyMedium,
                            color = colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(16.dp),
                            textAlign = TextAlign.Left,
                        )
                    }
                    else
                    {
                        Text(
                            text = stringResource(id = R.string.no_devices),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(16.dp),
                            textAlign = TextAlign.Left,
                        )
                    }

                }
            }

            item {
                ElevatedCard(
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 6.dp
                    ),
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .padding(bottom = 20.dp, top = 20.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp, bottom = 0.dp),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ){
                        Icon(
                            modifier = Modifier.padding(start = 16.dp, end = 14.dp),
                            imageVector = Icons.AutoMirrored.Filled.ContactSupport,
                            contentDescription = "Play Store Portfolio",
                            tint = colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = stringResource(id = R.string.note),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center,
                        )
                        // HIDDEN TEXTBUTTON TO CRATE SAME SPACING AS IN WELCOME
                        TextButton(
                            enabled = false,
                            modifier = Modifier.padding(end = 4.dp),
                            onClick = {}
                        ) {}
                    }
                    Text(
                        text = stringResource(id = R.string.note_text),
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(16.dp),
                        textAlign = TextAlign.Left,
                    )

                    TextButton(
                        modifier = Modifier.padding(start = 4.dp, end = 16.dp, top = 0.dp, bottom = 4.dp),
                        onClick = {
                            view.playSoundEffect(SoundEffectConstants.CLICK)
                            context.sendFeedbackEmail()}) {
                        Text(
                            text = stringResource(id = R.string.support),
                            color = colorScheme.onPrimaryContainer,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Left,
                        )
                    }
                }
            }

            // PORTFOLIO
            item {
                Text(
                    modifier = Modifier
                        .padding(top = 10.dp, bottom = 2.dp)
                        .fillMaxWidth(0.9f),
                    text = stringResource(id = R.string.check_portfolio),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                )
                OutlinedButton(
                    modifier = Modifier.padding( top = 16.dp, bottom = 4.dp),
                    colors = ButtonDefaults.outlinedButtonColors(),
                    onClick = {
                        view.playSoundEffect(SoundEffectConstants.CLICK)
                        context.openPlayStorePortfolio()
                    }
                ) {
                    Icon(
                        modifier = Modifier.padding(end = 14.dp),
                        imageVector = Icons.Default.Shop2,
                        contentDescription = "Play Store Portfolio",
                        tint = colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = stringResource(id = R.string.dev_page),
                    )


                }
            }

            // AMOLEDWATCHFACES:COM
            item { TextButton(
                modifier = Modifier
                    .padding(top = 20.dp, bottom = 40.dp)
                    .wrapContentSize(),
                onClick = {
                    view.playSoundEffect(SoundEffectConstants.CLICK)
                    context.openAmoledWebPage()
                }) {
                Text(
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    fontStyle = FontStyle.Italic,
                    text = stringResource(id = R.string.website),
                    color = Color.Gray)
            } }
            }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding()
            .pullRefresh(pullRefreshState)
    ){
        PullRefreshIndicator(
            refreshing = state,
            state = pullRefreshState,
            modifier = Modifier
                .padding(top = 60.dp)
                .align(alignment = Alignment.TopCenter),
            contentColor = colorScheme.primaryContainer,
            backgroundColor = NavigationDefaults.navigationSelectedItemColor(),
        )
    }

}