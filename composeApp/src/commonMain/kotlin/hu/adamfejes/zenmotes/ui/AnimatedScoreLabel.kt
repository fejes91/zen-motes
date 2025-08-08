package hu.adamfejes.zenmotes.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import hu.adamfejes.zenmotes.getScreenWidth
import hu.adamfejes.zenmotes.logic.ScoreEvent
import hu.adamfejes.zenmotes.ui.Constants.CELL_SIZE
import hu.adamfejes.zenmotes.ui.theme.toColorScheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun AnimatedScoreLabel(
    scoreEvent: ScoreEvent,
    modifier: Modifier = Modifier,
    onAnimationComplete: () -> Unit
) {
    val density = LocalDensity.current
    val colorScheme = LocalTheme.current.toColorScheme()

    // Convert pixel coordinates to dp coordinates
    val startX = with(density) { (scoreEvent.x * CELL_SIZE).toDp() }
    val startY = with(density) { (scoreEvent.y * CELL_SIZE).toDp() }

    val targetX = with(density) { (getScreenWidth() / 2).toDp() }
    val targetY = with(density) { 180.toDp() } // Top of screen with some padding, matching ScoreDisplay

    // Animation state
    val animatedX = remember(scoreEvent.obstacleId) { Animatable(startX.value) }
    val animatedY = remember(scoreEvent.obstacleId) { Animatable(startY.value) }
    val animatedAlpha = remember(scoreEvent.obstacleId) { Animatable(1f) }

    LaunchedEffect(Unit) {
        // Start animations in parallel
        val animationDuration = 2000 // 2 seconds

        // Launch position animations concurrently
        launch {
            animatedX.animateTo(
                targetValue = targetX.value,
                animationSpec = tween(durationMillis = animationDuration)
            )
        }

        launch {
            animatedY.animateTo(
                targetValue = targetY.value,
                animationSpec = tween(durationMillis = animationDuration )
            )
        }

        // Wait for most of the animation, then fade out
        delay(animationDuration - 500L)
        animatedAlpha.animateTo(
            targetValue = 0f,
            animationSpec = tween(durationMillis = 500)
        )

        // Animation complete
        onAnimationComplete()
    }
    
    Box(
        modifier = modifier
    ) {
        Text(
            text = if (scoreEvent.score > 0) "+${scoreEvent.score}" else "${scoreEvent.score}",
            color = if (scoreEvent.score > 0) colorScheme.textColor else Color.Red,
            fontSize = 12.sp,
            modifier = Modifier
                .offset(
                    x = animatedX.value.dp,
                    y = animatedY.value.dp
                )
                .alpha(animatedAlpha.value)
        )
    }
}