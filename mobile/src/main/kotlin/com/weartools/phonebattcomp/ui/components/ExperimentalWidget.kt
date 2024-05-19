package com.weartools.phonebattcomp.ui.components

import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.weartools.phonebattcomp.utils.askForNotificationAccess

@Composable
fun ExperimentalWidget(
    context: Context,
    callback: (Int) -> Unit
) {
    AlertDialog(
        icon = {
            Icon(
            imageVector = Icons.Default.Notifications,
            contentDescription = "Notifications Complication",
            tint = colorScheme.onPrimaryContainer
        )

        },
        onDismissRequest = {
            // Handle when the user dismisses the dialog (e.g., tapping outside)
            callback.invoke(-1)
        },
        title = {
            Text("Phone Notifications Complication")
        },
        text = {
            Column {
                Text(style = MaterialTheme.typography.bodyLarge,modifier = Modifier.padding(bottom = 12.dp),text = "This is an experimental SMALL_IMAGE type complication which will mirror phone notifications.")
                Text(style = MaterialTheme.typography.bodyLarge,text ="Application needs notifications access for complication to be working.")
            }

        },
        confirmButton = {
            Button(
                colors = ButtonColors(
                    containerColor = colorScheme.primary,
                    contentColor = Color(0xFF131313),
                    disabledContainerColor = colorScheme.primary,
                    disabledContentColor = Color(0xFF131313),
                ),
                onClick = {
                    context.askForNotificationAccess()
                    callback.invoke(-1)
                }
            ) {
                Text("Give Access")
            }
        },
        dismissButton = {
            Button(
                colors = ButtonColors(
                    containerColor = colorScheme.primary,
                    contentColor = Color(0xFF131313),
                    disabledContainerColor = colorScheme.primary,
                    disabledContentColor = Color(0xFF131313),
                ),
                onClick = {
                    callback.invoke(-1)
                }
            ) {
                Text("Dismiss")
            }
        }
    )
}

