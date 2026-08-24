package com.weartools.phonebattcomp.presentation

import androidx.compose.animation.graphics.ExperimentalAnimationGraphicsApi
import androidx.compose.animation.graphics.res.animatedVectorResource
import androidx.compose.animation.graphics.res.rememberAnimatedVectorPainter
import androidx.compose.animation.graphics.vector.AnimatedImageVector
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material3.AlertDialog
import androidx.wear.compose.material3.AlertDialogDefaults
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.ConfirmationDialog
import androidx.wear.compose.material3.ConfirmationDialogDefaults
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.SurfaceTransformation
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.TitleCard
import androidx.wear.compose.material3.lazy.rememberTransformationSpec
import androidx.wear.compose.material3.lazy.transformedHeight
import com.weartools.phonebattcomp.R

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
            Text(text = title)
        },
    )
}

@Composable
fun HowToCard(
    title: String,
    text: String,
    modifier: Modifier,
    surfaceTransformation: SurfaceTransformation,
) {
    TitleCard(
        modifier = modifier,
        transformation = surfaceTransformation,
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

@OptIn(ExperimentalAnimationGraphicsApi::class)
@Composable
fun ConfirmationOverlay(
    showConfirmation: Boolean,
    confirmationState: Boolean,
    onTimeout: () -> Unit
) {
    ConfirmationDialog(
        visible = showConfirmation,
        onDismissRequest = onTimeout,
        durationMillis = ConfirmationDialogDefaults.DurationMillis,
        colors = if (confirmationState) ConfirmationDialogDefaults.colors() else ConfirmationDialogDefaults.failureColors(),
        text = {
            Text(
                text = stringResource(id = if (confirmationState) R.string.check_your_phone else R.string.check_your_phone_failure),
                textAlign = TextAlign.Center,
            )
        }
    ){
        val animation = AnimatedImageVector.animatedVectorResource(if (confirmationState) R.drawable.open_on_phone_animation else R.drawable.open_on_phone_animation_failure)
        // Initially, animation is static and shown at the start position (atEnd = false).
        // Then, we use the EffectAPI to trigger a state change to atEnd = true,
        // which plays the animation from start to end.
        var atEnd by remember { mutableStateOf(false) }
        DisposableEffect(Unit) {
            atEnd = true
            onDispose {}
        }
        Image(
            painter = rememberAnimatedVectorPainter(animation, atEnd),
            contentDescription = "Open on phone",
            modifier = Modifier.size(ConfirmationDialogDefaults.SmallIconSize),
        )
    }
}
@Composable
fun ConfirmationDialog(
    showDialog: Boolean,
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
) {
    val transformationSpec = rememberTransformationSpec()

    AlertDialog(
        visible = showDialog,
        title = {
            Text(
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.primary,
                text = title,
                style = MaterialTheme.typography.titleMedium
            )
        },
        transformationSpec = transformationSpec,
        confirmButton = {
            AlertDialogDefaults.ConfirmButton(
                onClick = onConfirm
            )
        },
        dismissButton = {
            AlertDialogDefaults.DismissButton(
                onClick = onCancel
            )
        },
        onDismissRequest = onCancel
    ) {
        item {
            TitleCard(
                modifier = Modifier.fillMaxWidth()
                    .transformedHeight(this, transformationSpec),
                transformation = SurfaceTransformation(transformationSpec),
                title = {
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Start,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                },
            )
        }
    }
}

