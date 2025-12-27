package hu.adamfejes.zenmotes.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import hu.adamfejes.zenmotes.logic.ScoreEvent
import hu.adamfejes.zenmotes.navigation.LocalTheme
import hu.adamfejes.zenmotes.ui.Constants.SCORE_DISPLAY_DURATION
import hu.adamfejes.zenmotes.ui.theme.ColorScheme
import hu.adamfejes.zenmotes.ui.theme.toColorScheme
import hu.adamfejes.zenmotes.utils.formatScore
import hu.adamfejes.zenmotes.utils.formatTime
import org.jetbrains.compose.resources.stringResource
import zenmotescmp.composeapp.generated.resources.Res
import zenmotescmp.composeapp.generated.resources.game_screen_score_label
import zenmotescmp.composeapp.generated.resources.game_screen_time_label
import kotlin.math.roundToInt

@Composable
fun Scores(
    score: Int,
    countDownTimeMillis: Long,
    activeScoreEvents: Set<ScoreEvent>,
    onAnimationComplete: (ScoreEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    val scoreAnimatable = remember { Animatable(score.toFloat()) }
    val countDownAnimatable = remember { Animatable(countDownTimeMillis.toFloat()) }
    var targetScore by remember { mutableStateOf(score) }
    var targetCountDownTime by remember { mutableStateOf(countDownTimeMillis) }

    // Update target score when new score arrives, but don't animate yet
    LaunchedEffect(score, countDownTimeMillis) {
        targetScore = score
        targetCountDownTime = countDownTimeMillis

        if (score == 0) {
            scoreAnimatable.animateTo(0f)
        }
    }

    // Animate when triggered by score event completion
    LaunchedEffect(targetScore) {
        scoreAnimatable.animateTo(
            targetValue = targetScore.toFloat(),
            animationSpec = tween(durationMillis = SCORE_DISPLAY_DURATION)
        )
    }

    LaunchedEffect(targetCountDownTime) {
        countDownAnimatable.animateTo(
            targetValue = targetCountDownTime.toFloat(),
            animationSpec = tween(durationMillis = SCORE_DISPLAY_DURATION)
        )
    }

    val currentAnimatedScore = scoreAnimatable.value
    val currentCountDownTime = countDownAnimatable.value.toLong()

    val colorScheme = LocalTheme.current.toColorScheme()
    Box(modifier = modifier.fillMaxSize()) {
        ScoreDisplay(colorScheme, currentAnimatedScore, currentCountDownTime)

        activeScoreEvents.forEach { event ->
            key(event.obstacle.id) {
                AnimatedScoreLabel(
                    scoreEvent = event,
                    modifier = Modifier.fillMaxSize(),
                    onAnimationComplete = {
                        onAnimationComplete(event)
                    }
                )
            }
        }
    }
}

@Composable
private fun ScoreDisplay(
    colorScheme: ColorScheme,
    currentAnimatedScore: Float,
    countDownTimeMillis: Long
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = stringResource(Res.string.game_screen_score_label),
                    color = colorScheme.pausedTitleText,
                    textAlign = TextAlign.End,
                    fontSize = 16.sp,
                    modifier = Modifier.fillMaxWidth().weight(1f),
                )
                Text(
                    text = currentAnimatedScore.roundToInt().formatScore(),
                    color = colorScheme.pausedTitleText,
                    textAlign = TextAlign.Start,
                    fontSize = 22.sp,
                    modifier = Modifier.fillMaxWidth().weight(1f)
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = stringResource(Res.string.game_screen_time_label),
                    color = colorScheme.pausedTitleText,
                    textAlign = TextAlign.End,
                    fontSize = 16.sp,
                    modifier = Modifier.fillMaxWidth().weight(1f),
                )
                Text(
                    text = countDownTimeMillis.formatTime(),
                    color = colorScheme.pausedTitleText,
                    textAlign = TextAlign.Start,
                    fontSize = 22.sp,
                    modifier = Modifier.fillMaxWidth().weight(1f)
                )
            }
        }
    }
}