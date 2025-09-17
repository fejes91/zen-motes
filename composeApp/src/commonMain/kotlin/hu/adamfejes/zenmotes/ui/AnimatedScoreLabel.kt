package hu.adamfejes.zenmotes.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import hu.adamfejes.zenmotes.getScreenWidth
import hu.adamfejes.zenmotes.logic.ScoreEvent
import hu.adamfejes.zenmotes.ui.Constants.CELL_SIZE
import hu.adamfejes.zenmotes.ui.Constants.SCORE_FLY_DURATION
import hu.adamfejes.zenmotes.ui.theme.toColorScheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun AnimatedScoreLabel(
    scoreEvent: ScoreEvent,
    modifier: Modifier = Modifier,
    onAnimationNearlyComplete: () -> Unit,
    onAnimationComplete: () -> Unit
) {
    val density = LocalDensity.current
    val colorScheme = LocalTheme.current.toColorScheme()

    // Convert pixel coordinates to dp coordinates
    val startX = with(density) { (scoreEvent.x * CELL_SIZE).toDp() }
    val startY = with(density) { (scoreEvent.y * CELL_SIZE).toDp() }

    val targetX = with(density) { (getScreenWidth() / 2).toDp() }
    val targetY =
        with(density) { 180.toDp() } // Top of screen with some padding, matching ScoreDisplay

    // Animation state
    val animatedX = remember(scoreEvent.obstacleId) { Animatable(startX.value) }
    val animatedY = remember(scoreEvent.obstacleId) { Animatable(startY.value) }
    val animatedAlpha = remember(scoreEvent.obstacleId) { Animatable(1f) }

    LaunchedEffect(Unit) {
        launch {
            animatedX.animateTo(
                targetValue = targetX.value,
                animationSpec = tween(durationMillis = SCORE_FLY_DURATION, easing = FastOutLinearInEasing)
            )
        }

        launch {
            animatedY.animateTo(
                targetValue = targetY.value,
                animationSpec = tween(durationMillis = SCORE_FLY_DURATION, easing = FastOutLinearInEasing)
            )
        }

        launch {
            delay((SCORE_FLY_DURATION * 0.8f).toLong())
            onAnimationNearlyComplete()
        }

        // Wait for most of the animation, then fade out
        delay((SCORE_FLY_DURATION * 4 / 5).toLong())
        animatedAlpha.animateTo(
            targetValue = 0f,
            animationSpec = tween(durationMillis = 500)
        )

        onAnimationComplete()
    }

    Box(
        modifier = modifier
    ) {
        Text(
            text = if (scoreEvent.score > 0) "+${scoreEvent.score}" else "${scoreEvent.score}",
            color = when {
                scoreEvent.isBonus -> Color.Green
                scoreEvent.score > 0 -> colorScheme.textColor
                else -> Color.Red
            },
            fontSize = if(scoreEvent.isBonus) 24.sp else 20.sp,
            modifier = Modifier
                .offset(
                    x = animatedX.value.dp,
                    y = animatedY.value.dp
                )
                .alpha(animatedAlpha.value)
        )
    }
}