package hu.adamfejes.zenmotes.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import hu.adamfejes.zenmotes.getScreenWidth
import hu.adamfejes.zenmotes.logic.ScoreEvent
import hu.adamfejes.zenmotes.navigation.LocalTheme
import hu.adamfejes.zenmotes.ui.Constants.CELL_SIZE
import hu.adamfejes.zenmotes.ui.Constants.SCORE_FLY_DURATION
import hu.adamfejes.zenmotes.ui.theme.toColorScheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

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
    val targetY =
        with(density) { 180.toDp() } // Top of screen with some padding, matching ScoreDisplay

    // Animation state
    val animatedX = remember(scoreEvent.obstacle.id) { Animatable(startX.value) }
    val animatedY = remember(scoreEvent.obstacle.id) { Animatable(startY.value) }
    val animatedAlpha = remember(scoreEvent.obstacle.id) { Animatable(1f) }

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
            onAnimationComplete()
        }

        // Wait for most of the animation, then fade out
        delay((SCORE_FLY_DURATION * 0.8f).toLong())
        animatedAlpha.animateTo(
            targetValue = 0f,
            animationSpec = tween(durationMillis = 500)
        )
    }

    Box(
        modifier = modifier
    ) {
        Text(
            text = if (scoreEvent.score > 0) "+${(scoreEvent.score / 1000f).roundToInt()}" else "${(scoreEvent.score / 1000f).roundToInt()}",
            color = when {
                scoreEvent.isBonus -> colorScheme.positiveText
                scoreEvent.score > 0 -> colorScheme.textColorOnBackground
                else -> colorScheme.negativeText
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