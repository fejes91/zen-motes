package hu.adamfejes.zenmotes.ui

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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import hu.adamfejes.zenmotes.ui.components.ThreeStateSwitch
import hu.adamfejes.zenmotes.ui.theme.ColorScheme
import hu.adamfejes.zenmotes.ui.theme.toColorScheme
import hu.adamfejes.zenmotes.utils.formatTime
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun PauseDialog(
    viewModel: PauseViewModel = koinViewModel(),
    onBack: () -> Unit
) {
    val currentAppTheme by viewModel.appTheme.collectAsState()
    val soundEnabled by viewModel.soundEnabled.collectAsState()
    val score by viewModel.score.collectAsState(0)
    val countDownTime by viewModel.countDownTimeMillis.collectAsState()

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
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        modifier = Modifier.weight(1f),
                        text = "SCORE",
                        fontSize = 28.sp,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Medium,
                        color = colorScheme.pausedTitleText
                    )

                    Text(
                        modifier = Modifier.weight(1f),
                        text = "TIME",
                        fontSize = 28.sp,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Medium,
                        color = colorScheme.pausedTitleText
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        modifier = Modifier.weight(1f),
                        text = score.toString(),
                        fontSize = 32.sp,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Medium,
                        color = colorScheme.pausedTitleText
                    )

                    Text(
                        modifier = Modifier.weight(1f),
                        text = formatTime(countDownTime),
                        fontSize = 32.sp,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Medium,
                        color = colorScheme.pausedTitleText
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                PauseScreenButtons(
                    colorScheme = colorScheme,
                    onResume = {
                        viewModel.resumeSession()
                        onBack()
                    },
                    onRestart = {
                        viewModel.resetSession()
                        onBack()
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                ThreeStateSwitch(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    currentState = currentAppTheme!!,
                    onStateChange = viewModel::setTheme
                )

                Button(
                    onClick = { viewModel.setSoundEnabled(!soundEnabled) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    shape = RectangleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (soundEnabled) colorScheme.primaryButtonBackground else colorScheme.secondaryButtonBackground
                    )
                ) {
                    Text(
                        text = if (soundEnabled) "SOUND ON" else "SOUND OFF",
                        color = if (soundEnabled) colorScheme.primaryButtonText else colorScheme.secondaryButtonText
                    )
                }
            }
        }
    }
}

@Composable
private fun PauseScreenButtons(
    colorScheme: ColorScheme,
    onResume: () -> Unit,
    onRestart: () -> Unit
) {
    Button(
        onClick = onResume,
        modifier = Modifier.fillMaxWidth(),
        shape = RectangleShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = colorScheme.primaryButtonBackground
        )
    ) {
        Text(
            text = "RESUME",
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
            text = "RESTART",
            color = colorScheme.secondaryButtonText
        )
    }
}