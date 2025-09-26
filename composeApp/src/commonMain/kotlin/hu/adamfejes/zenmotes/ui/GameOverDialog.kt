package hu.adamfejes.zenmotes.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import hu.adamfejes.zenmotes.navigation.LocalTheme
import hu.adamfejes.zenmotes.ui.theme.ColorScheme
import hu.adamfejes.zenmotes.ui.theme.toColorScheme
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun GameOverDialog(
    viewModel: GameOverViewModel = koinViewModel(),
    onBack: () -> Unit
) {
    val currentAppTheme by viewModel.appTheme.collectAsState(initial = null)
    val scoreComparison by viewModel.scoreComparison.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.initialize()
    }

    if (currentAppTheme == null) {
        return
    }

    val colorScheme = LocalTheme.current.toColorScheme()

    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .padding(32.dp)
                .widthIn(min = 280.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(
                text = "GAME OVER",
                fontSize = 36.sp,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                color = colorScheme.pausedTitleText
            )

            Spacer(modifier = Modifier.height(16.dp))

            scoreComparison?.let { comparison ->
                when (comparison) {
                    is ScoreComparison.NewHighScore -> {
                        NewHighScoreDisplay(comparison.score, colorScheme)
                    }
                    is ScoreComparison.RegularScore -> {
                        RegularScoreDisplay(comparison.score, comparison.highScore, colorScheme)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    viewModel.resetSession()
                    onBack()
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RectangleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorScheme.secondaryButtonBackground
                )
            ) {
                Text(
                    text = "RESTART",
                    color = colorScheme.secondaryButtonText
                )
            }
        }
    }
}

@Composable
private fun NewHighScoreDisplay(score: Int, colorScheme: ColorScheme) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "NEW HIGH SCORE!",
            fontSize = 28.sp,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Medium,
            color = colorScheme.pausedTitleText
        )

        Text(
            text = score.toString(),
            fontSize = 42.sp,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Medium,
            color = colorScheme.pausedTitleText
        )
    }
}

@Composable
private fun RegularScoreDisplay(score: Int, highScore: Int?, colorScheme: ColorScheme) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "SCORE",
            fontSize = 28.sp,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Medium,
            color = colorScheme.pausedTitleText
        )

        Text(
            text = score.toString(),
            fontSize = 32.sp,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Medium,
            color = colorScheme.pausedTitleText
        )

        Spacer(modifier = Modifier.height(16.dp))

        highScore?.let {
            Text(
                text = "HIGH SCORE",
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Medium,
                color = colorScheme.pausedTitleText
            )

            Text(
                text = it.toString(),
                fontSize = 20.sp,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Medium,
                color = colorScheme.pausedTitleText
            )
        }
    }
}