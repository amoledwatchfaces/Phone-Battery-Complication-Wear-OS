package com.weartools.phonebattcomp.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.SoundEffectConstants
import android.view.View
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CurrencyBitcoin
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.Shop2
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.weartools.phonebattcomp.BuildConfig
import com.weartools.phonebattcomp.MainViewModel
import com.weartools.phonebattcomp.R
import com.weartools.phonebattcomp.utils.openAmoledWebPage
import com.weartools.phonebattcomp.utils.openBuyMeACoffeeSocialLink
import com.weartools.phonebattcomp.utils.openGithubSocialLink
import com.weartools.phonebattcomp.utils.openPlayStorePortfolio
import com.weartools.phonebattcomp.utils.openPrivacyPolicyLink
import com.weartools.phonebattcomp.utils.openTelegramSocialLink
import com.weartools.phonebattcomp.utils.openTwitterSocialLink
import com.weartools.phonebattcomp.utils.sendFeedbackEmail

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InfoScreen(
    view: View,
    context: Context,
    viewModel: MainViewModel
) {

    val listState = rememberLazyListState()

    Column(Modifier.fillMaxSize()) {
        // Show the top app bar on top level destinations.
        //val destination = appState.currentTopLevelDestination
        //if (destination != null) {
        CenterAlignedTopAppBar(
            actions = {
                IconButton(
                    modifier = Modifier.padding(end = 5.dp),
                    onClick = {
                        view.playSoundEffect(SoundEffectConstants.CLICK)
                        context.openPlayStorePortfolio() }) {
                    Icon(
                        imageVector = Icons.Default.Shop2,
                        contentDescription = "Play Store Portfolio",
                        tint = colorScheme.onPrimaryContainer
                    )
                }
            },
            title = {
                Text(
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                    fontWeight = FontWeight.Medium,
                    text = stringResource(id = R.string.app_name)
                ) },
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
            // Show the top app bar on top level destinations.
            //val destination = appState.currentTopLevelDestination
            //if (destination != null) {

            item {
                ElevatedCard(
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 6.dp
                    ),
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .padding(bottom = 20.dp, top = 10.dp)
                ){
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp, horizontal = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = colorScheme.onPrimaryContainer,
                            ),
                            onClick = {
                                view.playSoundEffect(SoundEffectConstants.CLICK)
                                context.openPrivacyPolicyLink() }) {
                            Icon(
                                modifier = Modifier.padding(end = 8.dp),
                                imageVector = Icons.Filled.Policy,
                                contentDescription = null,
                            )
                            Text(
                                fontWeight = FontWeight.Medium,
                                style = MaterialTheme.typography.bodyMedium,
                                text = stringResource(id = R.string.privacy)
                            )
                        }
                        TextButton(
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = colorScheme.onPrimaryContainer,
                            ),
                            onClick = {
                                view.playSoundEffect(SoundEffectConstants.CLICK)
                                context.sendFeedbackEmail() }) {
                            Icon(
                                modifier = Modifier.padding(end = 9.dp),
                                imageVector = Icons.Filled.Mail,
                                contentDescription = null,
                            )
                            Text(
                                fontWeight = FontWeight.Medium,
                                style = MaterialTheme.typography.bodyMedium,
                                text = stringResource(id = R.string.feedback)
                            )
                        }
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
                        .padding(bottom = 20.dp, top = 10.dp)
                ){
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.SpaceBetween,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth(0.9F)
                                .padding(top = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    style = MaterialTheme.typography.bodyMedium,
                                    text = stringResource(id = R.string.follow_us), fontWeight = FontWeight.Medium)
                            }
                            Column {
                                Row {
                                    IconButton(onClick = {
                                        view.playSoundEffect(SoundEffectConstants.CLICK)
                                        context.openTelegramSocialLink()}) {
                                        Icon(
                                            tint = colorScheme.onPrimaryContainer,
                                            imageVector = ImageVector.vectorResource(id = R.drawable.social_telegram_2),
                                            contentDescription = null,
                                        )
                                    }
                                    IconButton(onClick = {
                                        view.playSoundEffect(SoundEffectConstants.CLICK)
                                        context.openGithubSocialLink()
                                    }) {
                                        Icon(
                                            tint = colorScheme.onPrimaryContainer,
                                            imageVector = ImageVector.vectorResource(id = R.drawable.social_github),
                                            contentDescription = null,
                                        )
                                    }
                                    /*
                                    IconButton(onClick = {
                                        view.playSoundEffect(SoundEffectConstants.CLICK)
                                        context.openFacebookSocialLink()}) {
                                        Icon(
                                            tint = colorScheme.onPrimaryContainer,
                                            imageVector = ImageVector.vectorResource(id = R.drawable.social_facebook),
                                            contentDescription = null,
                                        )
                                    }

                                     */
                                    IconButton(onClick = {
                                        view.playSoundEffect(SoundEffectConstants.CLICK)
                                        context.openTwitterSocialLink()
                                    }) {
                                        Icon(
                                            tint = colorScheme.onPrimaryContainer,
                                            imageVector = ImageVector.vectorResource(id = R.drawable.social_x),
                                            contentDescription = null,
                                        )
                                    }
                                }
                            }
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth(0.9F)
                                .padding(bottom = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        )  {
                            Column {
                                Text(
                                    style = MaterialTheme.typography.bodyMedium,
                                    text = stringResource(id = R.string.support_us), fontWeight = FontWeight.Medium)
                            }
                            Column {
                                Row {
                                    IconButton(onClick = {
                                        view.playSoundEffect(SoundEffectConstants.CLICK)
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        val clip = ClipData.newPlainText("BTC Address","bc1qvn4m56rjmagr2g9treawwpjmjpeuq5023hyjd2")
                                        clipboard.setPrimaryClip(clip)
                                        Toast.makeText(context, "BTC Address copied to clipboard", Toast.LENGTH_LONG).show()
                                    }) {
                                        Icon(
                                            modifier = Modifier
                                                .padding()
                                                .rotate(15F),
                                            tint = colorScheme.onPrimaryContainer,
                                            imageVector = Icons.Filled.CurrencyBitcoin,
                                            contentDescription = null,
                                        )
                                    }
                                    IconButton(onClick = {
                                        view.playSoundEffect(SoundEffectConstants.CLICK)
                                        context.openBuyMeACoffeeSocialLink()
                                    }) {
                                        Icon(
                                            modifier = Modifier.padding(),
                                            tint = colorScheme.onPrimaryContainer,
                                            imageVector = Icons.Filled.LocalCafe,
                                            contentDescription = null,
                                        )
                                    }
                                }
                            }
                        }

                    }
                }
            }

            item { Text(
                modifier = Modifier.padding(bottom = 10.dp, top = 10.dp),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                text = stringResource(id = R.string.liked_watch_face),) }
            item {
                Button(
                    colors = ButtonDefaults.textButtonColors(
                        containerColor = colorScheme.primaryContainer,
                        contentColor = colorScheme.onPrimaryContainer

                    ),
                    onClick = {
                        view.playSoundEffect(SoundEffectConstants.CLICK)
                        viewModel.showRateDialog(context)}) {
                    Text(
                        text = stringResource(id = R.string.leave_review),
                    )
                    Icon(
                        modifier = Modifier.padding(start = 14.dp),
                        imageVector = Icons.Default.RateReview,
                        contentDescription = null,
                    )

                }
            }

            item { TextButton(
                modifier = Modifier
                    .padding(top = 20.dp, bottom = 40.dp)
                    .wrapContentSize(),
                onClick = {
                    view.playSoundEffect(SoundEffectConstants.CLICK)
                    context.openAmoledWebPage() }) {
                Text(
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    fontStyle = FontStyle.Italic,
                    text = stringResource(id = R.string.website),
                    color = Color.Gray)
            } }

            item {
                Text(
                    modifier = Modifier.align(Alignment.End).padding(top = 140.dp),
                    textAlign = TextAlign.Center,
                    color = Color.DarkGray,
                    style = MaterialTheme.typography.bodyMedium,
                    text = stringResource(R.string.version)+" "+BuildConfig.VERSION_NAME,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }

}