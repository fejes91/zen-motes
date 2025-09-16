package hu.adamfejes.zenmotes.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import hu.adamfejes.zenmotes.ui.components.ThreeStateSwitch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import hu.adamfejes.zenmotes.ui.theme.AppTheme
import hu.adamfejes.zenmotes.ui.theme.ColorScheme
import hu.adamfejes.zenmotes.ui.theme.getPixeledFontFamily
import hu.adamfejes.zenmotes.ui.theme.toColorScheme

fun formatTime(millis: Long): String {
    val totalSeconds = millis / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "${minutes}:${seconds.toString().padStart(2, '0')}"
}

@Composable
fun PauseOverlay(
    onResume: () -> Unit,
    onRestart: () -> Unit,
    currentAppTheme: AppTheme,
    onThemeChange: (AppTheme) -> Unit,
    score: Int,
    sessionTimeMillis: Long
) {
    val colorScheme = LocalTheme.current.toColorScheme()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.pauseOverlayBackground),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .padding(32.dp)
                .widthIn(min = 280.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Score:",
                    fontSize = 24.sp,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Medium,
                    color = colorScheme.pausedTitleText
                )

                Text(
                    text = score.toString(),
                    fontSize = 36.sp,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Medium,
                    color = colorScheme.pausedTitleText
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Time:",
                    fontSize = 24.sp,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Medium,
                    color = colorScheme.pausedTitleText
                )

                Text(
                    text = formatTime(sessionTimeMillis),
                    fontSize = 36.sp,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Medium,
                    color = colorScheme.pausedTitleText
                )
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Buttons(
                    colorScheme = colorScheme,
                    onResume = onResume,
                    onRestart = onRestart
                )

                Spacer(modifier = Modifier.height(16.dp))

                ThreeStateSwitch(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    currentState = currentAppTheme,
                    onStateChange = onThemeChange
                )
            }
        }
    }
}

@Composable
fun Buttons(colorScheme: ColorScheme, onResume: () -> Unit, onRestart: () -> Unit) {
    Button(
        onClick = onResume,
        modifier = Modifier.fillMaxWidth(),
        shape = RectangleShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = colorScheme.primaryButtonBackground
        )
    ) {
        Text(
            text = "Resume",
            color = colorScheme.primaryButtonText
        )
    }

    Button(
        onClick = onRestart,
        modifier = Modifier.fillMaxWidth(),
        shape = RectangleShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = colorScheme.secondaryButtonBackground
        )
    ) {
        Text(
            text = "Restart",
            color = colorScheme.secondaryButtonText
        )
    }
}
