package com.weartools.phonebattcomp.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.wear.compose.material3.ColorScheme
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Typography
import androidx.wear.compose.material3.dynamicColorScheme

val Blue200 = Color(0xFFe5fcff)
val Blue500 = Color(0xFFb9f7ff)
val Blue700 = Color(0xFF407b83)
val Teal200 = Color(0xFFe5fcff)
val Red400 = Color(0xFFCF6679)
val Yellow = Color(0xFFffd215)


@Composable
fun PhoneBatteryAppTheme(
    content: @Composable () -> Unit
) {
    val appColorScheme = ColorScheme(
        primary = Blue200,
        onPrimary = Color.White,
        secondary = Teal200,
        onSecondary = Color.Gray,
        tertiary = Blue700,
        onTertiary = Blue500,
        error = Red400,
        onError = Color.Black
    )
    val dynamicColorScheme = dynamicColorScheme(LocalContext.current)

    MaterialTheme(
        colorScheme = dynamicColorScheme ?: appColorScheme,
        content = content
    )
}

val Typography = Typography(
    // M3 TextStyle parameters
)

/*
val Typography = Typography(
    body1 = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp
    )
)
 */