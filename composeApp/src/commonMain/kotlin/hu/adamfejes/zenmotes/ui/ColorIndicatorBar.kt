package hu.adamfejes.zenmotes.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import hu.adamfejes.zenmotes.logic.ColorType
import hu.adamfejes.zenmotes.navigation.LocalTheme
import hu.adamfejes.zenmotes.ui.theme.toColorScheme

@Composable
fun ColorIndicatorBar(
    currentColor: ColorType,
    nextColor: ColorType?,
    modifier: Modifier = Modifier.Companion
) {
    val theme = LocalTheme.current
    val colorScheme = theme.toColorScheme()
    val currentColorValue = mapObstacleColorToTheme(currentColor, colorScheme)
    val nextColorValue = nextColor?.let { mapObstacleColorToTheme(it, colorScheme) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(16.dp)
    ) {
        // Current color (base layer)
        Box(
            modifier = Modifier.Companion
                .fillMaxSize()
                .background(currentColorValue)
        )

        val animatedWidthFraction by animateFloatAsState(
            targetValue = if (nextColorValue != null) 1f else 0f,
            animationSpec = tween(
                durationMillis = if (nextColorValue != null) Constants.COLOR_CHANGE_ANIMATION_DURATION.toInt() else 0,
                easing = LinearEasing
            ),
            label = "AnimatedWidthFraction"
        )

        // Next color (slides in from right when animation starts)
        if (nextColorValue != null) {
            Box(
                modifier = Modifier.Companion
                    .fillMaxHeight()
                    .fillMaxWidth(animatedWidthFraction)
                    .background(nextColorValue)
            )
        }
    }
}