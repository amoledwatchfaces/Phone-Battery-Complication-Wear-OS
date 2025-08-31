package com.weartools.phonebattcomp.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.foundation.rotary.RotaryScrollableDefaults
import androidx.wear.compose.foundation.rotary.rotaryScrollable
import androidx.wear.compose.material3.AlertDialog
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.CompactButton
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.SurfaceTransformation
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.TitleCard

@Composable
fun DialogChip(
    modifier: Modifier,
    transformation: SurfaceTransformation,
    text: String,
    title: String,
    onClick: (() -> Unit)? = null,
    icon: @Composable (BoxScope.() -> Unit)?
) {
    Button(
        colors = ButtonDefaults.filledTonalButtonColors(),
        modifier = modifier,
        transformation = transformation,
        onClick = {
            onClick?.invoke()
        },
        icon = icon,
        label = {
            Text(
                text = text,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        secondaryLabel = {
            Text(text = title, color = Color.LightGray)
        },
    )
}

@Composable
fun SimpleChip(
    text: String,
    onClick: (() -> Unit)? = null,
    icon: @Composable (BoxScope.() -> Unit)?
) {
    CompactButton(
        modifier = Modifier
            .fillMaxWidth(),
        onClick = {
            onClick?.invoke()
        },
        label = {
            Text(
                text = text,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        icon = icon
    )
}



@Composable
fun PreferenceCategory(
    modifier: Modifier = Modifier,
    title: String
) {
    Text(
        text = title,
        modifier = modifier.padding(
            top = 14.dp,
            bottom = 4.dp
        ),
        color = MaterialTheme.colorScheme.secondary,
        textAlign = TextAlign.Center,
        style = MaterialTheme.typography.bodyMedium
    )
}

@Composable
fun SectionText(modifier: Modifier = Modifier, text: String) {
    Text(
        modifier = modifier,
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.outlineVariant,
        text = text,
        style = MaterialTheme.typography.bodySmall
    )
}

@Composable
fun ListItemsWidget(
    titles: String,
    callback: (Int) -> Unit
) {
    val state = remember { mutableStateOf(true) }

        val listState = rememberTransformingLazyColumnState()
        val focusRequester = remember { FocusRequester() }

        LaunchedEffect(Unit) {focusRequester.requestFocus()}
        AlertDialog(
            visible = state.value,
            modifier = Modifier
                .rotaryScrollable(
                    RotaryScrollableDefaults.behavior(scrollableState = listState),
                    focusRequester = focusRequester
                ),
            verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.Top),
            contentPadding = PaddingValues(
                start = 12.dp,
                end = 12.dp,
                top = 24.dp,
                bottom = 52.dp
            ),
            onDismissRequest = { callback.invoke(-1) },
            title = { PreferenceCategory(title = titles) },
            content = {
                item { HowToCard(
                    title = "What can this app do?",
                    text = "This app will add a Phone Battery Complication to your watch face customization menu. " +
                            "Watch Battery, Temperature & Voltage Complications are included too.") }
                item { HowToCard(
                    title = "My complication is not showing a phone battery level. Why?",
                    text = "This app requires the mobile companion app to work. You can install it with the help of the remote button in the main screen. " +
                            "In order to get the latest phone battery level, wearable app is sending a message to the mobile companion app and receives the phone battery level back.") }
                item { HowToCard(
                    title = "How often Phone Battery Complication updates?",
                    text = "Phone Battery Complication updates automatically in pre-defined 5-minute intervals. This is a minimum possible level to ensure minimal battery consumption. " +
                            "You can also update this complication manually by tapping on it.") }
                item { HowToCard(
                    title = "Which watch faces are the best suited?",
                    text = "This app is compatible with any watch face which has at least one complication slot of SHORT_TEXT, LONG_TEXT or RANGED_VALUE type. " +
                            "Complication type & look depends on the watch face complication implementation method.") }
                item { HowToCard(
                    title = "Phone Notifications Complication",
                    text = "This complication is new and requires mobile companion app to have background service enabled in the experimental setting. " +
                            "Complication simply mirrors phone notifications (not ongoing)") }
                item { HowToCard(
                    title = "Active Sync",
                    text = "This feature requires mobile companion app to have background service enabled in the companion app setting. " +
                            "Active Sync provides real-time phone battery updates & charging status") }
            }
        )
}

@Composable
fun HowToCard(
    title: String,
    text: String
) {
    TitleCard(
        onClick = {  },
        title = {
            Text(
                title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                lineHeight = 16.sp)
                },
        content = {
            Text(text, fontSize = 12.sp)
        }
    )
}

