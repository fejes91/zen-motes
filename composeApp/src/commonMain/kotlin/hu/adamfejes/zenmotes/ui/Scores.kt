package hu.adamfejes.zenmotes.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import hu.adamfejes.zenmotes.ui.Constants.SCORE_DISPLAY_DURATION
import hu.adamfejes.zenmotes.ui.theme.ColorScheme
import hu.adamfejes.zenmotes.ui.theme.toColorScheme
import kotlin.math.roundToInt

@Composable
fun Scores(
    score: Int,
    activeScoreEvents: Set<ScoreEvent>,
    onAnimationComplete: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val scoreAnimatable = remember { Animatable(score.toFloat()) }
    var targetScore by remember { mutableStateOf(score) }
    var animationTrigger by remember { mutableStateOf(0) }

    // Update target score when new score arrives, but don't animate yet
    LaunchedEffect(score) {
        targetScore = score

        if(score == 0) {
            scoreAnimatable.snapTo(0f)
        }
    }

    // Animate when triggered by score event completion
    LaunchedEffect(animationTrigger) {
        scoreAnimatable.animateTo(
            targetValue = targetScore.toFloat(),
            animationSpec = tween(durationMillis = SCORE_DISPLAY_DURATION)
        )
    }

    val currentAnimatedScore = scoreAnimatable.value

    val colorScheme = LocalTheme.current.toColorScheme()
    Box(modifier = modifier.fillMaxSize()) {
        ScoreDisplay(colorScheme, currentAnimatedScore)

        activeScoreEvents.forEach { event ->
            key(event.obstacleId) {
                AnimatedScoreLabel(
                    scoreEvent = event,
                    modifier = Modifier.fillMaxSize(),
                    onAnimationNearlyComplete = {
                        animationTrigger++
                    },
                    onAnimationComplete = {
                        onAnimationComplete(event.obstacleId)
                    }
                )
            }
        }
    }
}

@Composable
private fun ScoreDisplay(
    colorScheme: ColorScheme,
    currentAnimatedScore: Float
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding(),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "Score: ",
                color = colorScheme.textColor,
                textAlign = TextAlign.End,
                fontSize = 16.sp,
                modifier = Modifier.fillMaxWidth().weight(1f),
            )
            Text(
                text = "${currentAnimatedScore.roundToInt()}",
                color = colorScheme.textColor,
                textAlign = TextAlign.Start,
                fontSize = 16.sp,
                modifier = Modifier.fillMaxWidth().weight(1f)
            )
        }
    }
}