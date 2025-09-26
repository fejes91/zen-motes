package hu.adamfejes.zenmotes.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import hu.adamfejes.zenmotes.logic.ColorType
import hu.adamfejes.zenmotes.navigation.LocalTheme
import hu.adamfejes.zenmotes.ui.components.ThreeStateSwitch
import hu.adamfejes.zenmotes.ui.theme.toColorScheme
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel
import zenmotescmp.composeapp.generated.resources.Res
import zenmotescmp.composeapp.generated.resources.wider_tower

@Composable
fun MainMenuDialog(
    viewModel: MainMenuViewModel = koinViewModel(),
    onStartGame: () -> Unit
) {
    val currentAppTheme by viewModel.appTheme.collectAsState()
    val soundEnabled by viewModel.soundEnabled.collectAsState()
    val highScore by viewModel.highScore.collectAsState()
    val appVersion by viewModel.appVersion.collectAsState()

    if (currentAppTheme == null) {
        return
    }

    val colorScheme = LocalTheme.current.toColorScheme()

    // Get random color from palette for tower
    val randomColorType = remember { ColorType.entries.random() }

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
            Image(
                painter = painterResource(Res.drawable.wider_tower),
                contentDescription = "Castle Tower",
                modifier = Modifier
                    .size(100.dp),
                colorFilter = ColorFilter.tint(
                    color = mapObstacleColorToTheme(randomColorType, colorScheme),
                    blendMode = BlendMode.Modulate
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Castle Blaster",
                fontSize = 48.sp,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                color = colorScheme.pausedTitleText
            )

            Button(
                onClick = onStartGame,
                modifier = Modifier.fillMaxWidth(),
                shape = RectangleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorScheme.primaryButtonBackground
                )
            ) {
                Text(
                    text = "START GAME",
                    color = colorScheme.primaryButtonText,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Text(
                text = if (highScore != null) "HIGH SCORE: $highScore" else "HIGH SCORE: --",
                fontSize = 24.sp,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Medium,
                color = colorScheme.pausedTitleText
            )

            Spacer(modifier = Modifier.height(16.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
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

        Text(
            text = appVersion ?: "",
            fontSize = 12.sp,
            color = colorScheme.pausedTitleText.copy(alpha = 0.6f),
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
        )
    }
}