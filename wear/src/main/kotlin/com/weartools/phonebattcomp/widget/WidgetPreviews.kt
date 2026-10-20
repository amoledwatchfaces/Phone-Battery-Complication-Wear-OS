package com.weartools.phonebattcomp.widget

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.glance.wear.core.WearWidgetParams
import androidx.glance.wear.tooling.preview.WearWidgetPreview
import androidx.glance.wear.tooling.preview.RectangularSmallWidgetPreviewParams
import androidx.glance.wear.tooling.preview.RoundSmallWidgetPreviewParams
import androidx.glance.wear.tooling.preview.SquircleSmallWidgetPreviewParams

/**
 * Android Studio Design Panel previews for Phone Battery Widget.
 */
@Preview
@Composable
fun PhoneBatteryWidgetSquirclePreview(
    @PreviewParameter(SquircleSmallWidgetPreviewParams ::class) params: WearWidgetParams,
) {
    WearWidgetPreview(
        widget = PhoneBatteryWidget(),
        params = params,
    )
}

@Preview
@Composable
fun PhoneBatteryWidgetRoundPreview(
    @PreviewParameter(RoundSmallWidgetPreviewParams::class) params: WearWidgetParams,
) {
    WearWidgetPreview(
        widget = PhoneBatteryWidget(),
        params = params,
    )
}

@Preview(
    name = "Phone Battery Widget Preview",
    device = "spec:width=1000dp,height=1000dp,dpi=320",
)
@Composable
fun PhoneBatteryWidgetRectangularPreview(
    @PreviewParameter(RectangularSmallWidgetPreviewParams::class) params: WearWidgetParams,
) {
    WearWidgetPreview(
        widget = PhoneBatteryWidget(),
        params = params,
    )
}


