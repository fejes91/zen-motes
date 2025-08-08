package hu.adamfejes.zenmotes.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationResult
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import hu.adamfejes.zenmotes.ui.theme.toColorScheme
import kotlin.math.roundToInt

@Composable
fun ScoreDisplay(
    score: Int,
    modifier: Modifier = Modifier
) {
    var currentAnimatedScore by remember { mutableStateOf(score.toFloat()) }
    val scoreAnimatable = remember { Animatable(score.toFloat()) }

    LaunchedEffect(score) {
        // Start animation from current value to new score
        scoreAnimatable.animateTo(
            targetValue = score.toFloat(),
            animationSpec = tween(durationMillis = 1000)
        )
    }

    // Update the displayed score with the animated value
    currentAnimatedScore = scoreAnimatable.value

    val colorScheme = LocalTheme.current.toColorScheme()
    Box(
        modifier = modifier
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