package hu.adamfejes.zenmotes.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import hu.adamfejes.zenmotes.navigation.LocalTheme
import hu.adamfejes.zenmotes.ui.theme.AppTheme
import hu.adamfejes.zenmotes.ui.theme.getFontFamily
import hu.adamfejes.zenmotes.ui.theme.toColorScheme

@Composable
fun ThreeStateSwitch(
    modifier: Modifier = Modifier,
    currentState: AppTheme,
    onStateChange: (AppTheme) -> Unit
) {
    val density = LocalDensity.current
    val textMeasurer = rememberTextMeasurer()

    val thumbOffset = remember { Animatable(0f) }
    val states = listOf(AppTheme.LIGHT, AppTheme.SYSTEM, AppTheme.DARK)
    val labels = states.map { it.name.uppercase().replaceFirstChar { char -> char.uppercase() } }
    val currentIndex = states.indexOf(currentState)

    val colorScheme = LocalTheme.current.toColorScheme()
    val textColor = colorScheme.textColor
    val trackColor = colorScheme.background
    val thumbColor = colorScheme.textBackground
    val fontFamily = getFontFamily()

    LaunchedEffect(currentState) {
        thumbOffset.animateTo(
            targetValue = currentIndex.toFloat(),
            animationSpec = tween(300)
        )
    }

    Canvas(
        modifier = modifier
            .height(50.dp)
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val segmentWidth = size.width / 3f
                    val tappedIndex = (offset.x / segmentWidth).toInt().coerceIn(0, 2)
                    onStateChange(states[tappedIndex])
                }
            }
    ) {
        drawThreeStateSwitch(
            size = size,
            thumbOffset = thumbOffset.value,
            trackColor = trackColor,
            thumbColor = thumbColor,
            textMeasurer = textMeasurer,
            labels = labels,
            density = density,
            textColor = textColor,
            fontFamily = fontFamily
        )
    }
}

private fun DrawScope.drawThreeStateSwitch(
    size: Size,
    thumbOffset: Float,
    textColor: Color,
    fontFamily: FontFamily,
    trackColor: Color,
    thumbColor: Color,
    textMeasurer: TextMeasurer,
    labels: List<String>,
    density: androidx.compose.ui.unit.Density
) {
    val trackRect = Size(size.width, size.height)

    // Draw track
    drawRect(
        color = trackColor,
        size = trackRect,
    )

    // Calculate segment properties
    val segmentWidth = size.width / 3f
    val thumbPadding = with(density) { 4.dp.toPx() }
    val thumbWidth = (size.width - thumbPadding * 2) / 3f
    val thumbHeight = size.height - thumbPadding * 2
    val thumbX = thumbPadding + (thumbWidth * thumbOffset)
    val thumbY = thumbPadding

    // Draw thumb
    drawRect(
        color = thumbColor,
        topLeft = Offset(thumbX, thumbY),
        size = Size(thumbWidth, thumbHeight),
    )

    labels.forEachIndexed { index, label ->
        val segmentCenterX = segmentWidth * index + segmentWidth / 2f

        val textStyle = TextStyle(
            fontSize = 16.sp,
            fontFamily = fontFamily,
            color = textColor
        )

        val textResult = textMeasurer.measure(
            text = label,
            style = textStyle
        )

        drawText(
            textLayoutResult = textResult,
            topLeft = Offset(
                x = segmentCenterX - textResult.size.width / 2f,
                y = size.height / 2f - textResult.size.height / 2f
            )
        )
    }
}