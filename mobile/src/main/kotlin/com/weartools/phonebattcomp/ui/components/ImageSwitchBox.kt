package com.weartools.phonebattcomp.ui.components

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun ImageSwitchBox() {

    val images = listOf(
        "https://amoledwatchfaces.com/assets/appIcons/phonebattcomp_1.webp",
        "https://amoledwatchfaces.com/assets/appIcons/phonebattcomp_2.webp",
        "https://amoledwatchfaces.com/assets/appIcons/phonebattcomp_3.webp",
        "https://amoledwatchfaces.com/assets/appIcons/phonebattcomp_4.webp",
        "https://amoledwatchfaces.com/assets/appIcons/phonebattcomp_5.webp"
    )

    var currentImageIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        val switchInterval = 3000L // 3 seconds

        while (true) {
            delay(switchInterval.milliseconds)
            if (currentImageIndex == 4) currentImageIndex = 0
            else currentImageIndex += 1
        }
    }


    Crossfade(
        targetState = currentImageIndex,
        animationSpec = tween(1000), label = "") { index ->
        AsyncImage(
            modifier = Modifier
                .size(175.dp)
                .clip(CircleShape),
            alignment = Alignment.Center,
            contentScale = ContentScale.Crop,
            model = images[index],
            contentDescription = "Frame"
        )
    }
}