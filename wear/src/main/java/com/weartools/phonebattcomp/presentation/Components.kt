package com.weartools.phonebattcomp.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.*
import androidx.wear.compose.material.dialog.Alert
import androidx.wear.compose.material.dialog.Dialog
import com.weartools.phonebattcomp.R
import com.weartools.phonebattcomp.theme.PhoneBatteryAppTheme
import com.weartools.phonebattcomp.theme.wearColorPalette

@Composable
fun DialogChip(
    text: String,
    title: String,
    onClick: (() -> Unit)? = null,
) {
    Chip(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp),
        onClick = {
            onClick?.invoke()
        },
        colors = ChipDefaults.gradientBackgroundChipColors(
            startBackgroundColor = Color(0xff2c2c2d),
            endBackgroundColor = Color(0xff2c2c2d)
        ),
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
    iconId: Int,
    onClick: (() -> Unit)? = null,
) {
    CompactChip(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp),
        onClick = {
            onClick?.invoke()
        },
        colors = ChipDefaults.gradientBackgroundChipColors(
            startBackgroundColor = Color(0xff2c2c2d),
            endBackgroundColor = Color(0xff2c2c2d)
        ),
        label = {
            Text(
                text = text,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        icon = {
            Icon(
                painter = painterResource(iconId),
                contentDescription = "Mark Castle",
                tint = wearColorPalette.secondaryVariant,
                modifier = Modifier
                    .size(ChipDefaults.SmallIconSize)
                    .wrapContentSize(align = Alignment.Center)
            )
        }
    )
}


@Composable
fun ToggleChip(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    label: String,
    secondaryLabelOn: String,
    secondaryLabelOff: String,
) {
    ToggleChip(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp),
        checked = checked,
        colors = ToggleChipDefaults.toggleChipColors(
            checkedEndBackgroundColor = wearColorPalette.primaryVariant,
            checkedToggleControlColor = Color(0xFFb9f7ff)
        ),
        onCheckedChange = { enabled ->
            onCheckedChange(enabled)
        },
        label = { Text(label) },
        secondaryLabel = {
            if (checked) {
                Text(text = secondaryLabelOn, color = Color.LightGray)
            } else Text(text = secondaryLabelOff, color = Color.LightGray)
        },
        toggleControl = {
            Icon(
                imageVector = ToggleChipDefaults.switchIcon(checked),
                contentDescription = "compose_toggle"
            )
        }
    )
}

@Composable
fun SettingsText(modifier: Modifier = Modifier) {
    Text(
        modifier = modifier
            .padding(top = 2.dp, bottom = 2.dp)
            .offset(y = (-7).dp),
        textAlign = TextAlign.Center,
        color = MaterialTheme.colors.primary,
        text = stringResource(id = R.string.app_info),
        style = MaterialTheme.typography.title3
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
            start = 16.dp,
            top = 14.dp,
            end = 16.dp,
            bottom = 4.dp
        ),
        color = wearColorPalette.secondary,
        style = MaterialTheme.typography.caption2
    )
}

@Composable
fun SectionText(modifier: Modifier = Modifier, text: String) {
    Text(
        modifier = modifier,
        textAlign = TextAlign.Center,
        color = MaterialTheme.colors.onSecondary,
        text = text,
        style = MaterialTheme.typography.caption3
    )
}

@Composable
fun ListItemsWidget(
    titles: String,
    callback: (Int) -> Unit
) {
    val state = remember { mutableStateOf(true) }

    PhoneBatteryAppTheme {
        val listState = rememberScalingLazyListState()
        Dialog(

            showDialog = state.value,
            scrollState = listState,
            onDismissRequest = { callback.invoke(-1) }
        )
        {
            Alert(
                backgroundColor = Color.Black,
                scrollState = listState,
                title = { PreferenceCategory(title = titles) },
                verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.Top),
                contentPadding = PaddingValues(
                    start = 10.dp,
                    end = 10.dp,
                    top = 24.dp,
                    bottom = 52.dp
                ),
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
                }
            )

        }
    }
}

@Composable
fun HowToCard(title: String, text: String) {
    TitleCard(
        modifier = Modifier.padding(horizontal = 10.dp),
        onClick = {  },
        title = { Text(title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = wearColorPalette.primary, lineHeight = 16.sp) },
        contentColor = MaterialTheme.colors.onSurface,
        titleColor = MaterialTheme.colors.onSurface
    ) {
        Text(text, fontSize = 12.sp)
    }
}

