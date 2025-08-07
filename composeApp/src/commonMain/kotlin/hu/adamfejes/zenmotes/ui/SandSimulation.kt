package hu.adamfejes.zenmotes.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import hu.adamfejes.zenmotes.logic.ColorType
import hu.adamfejes.zenmotes.ui.theme.Theme
import hu.adamfejes.zenmotes.ui.theme.toColorScheme
import org.koin.compose.koinInject

val LocalTheme = staticCompositionLocalOf {
    Theme.DARK
}

@Composable
fun SandSimulation(
    modifier: Modifier = Modifier
) {
    val viewModel: SandSimulationViewModel = koinInject()
    val score by viewModel.score.collectAsState(0)
    
    // TODO store it in shared preferences, after converting to multi-platform
    var currentTheme by remember { mutableStateOf(Theme.DARK) }
    var selectedColor by remember { mutableStateOf(ColorType.OBSTACLE_COLOR_1) }
    var isPaused by remember { mutableStateOf(false) }
    var resetTrigger by remember { mutableIntStateOf(0) }

    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> isPaused = true
                Lifecycle.Event.ON_RESUME -> {}
                else -> {}
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    CompositionLocalProvider(LocalTheme provides currentTheme) {
        Box(
            modifier = modifier.fillMaxSize()
        ) {
            // Everything that should be blurred when paused
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .then(if (isPaused) Modifier.blur(20.dp) else Modifier)
            ) {
                // Sand simulation view - full edge-to-edge behind everything
                SandView(
                    modifier = Modifier.fillMaxSize(),
                    sandColorType = selectedColor,
                    sandGenerationAmount = 60,
                    showPerformanceOverlay = false, // Toggle performance overlay for testing
                    isPaused = isPaused,
                    resetTrigger = resetTrigger
                )

                // Score display at the top center
                ScoreDisplay(score = score)

                // Top UI overlay - color picker and reset button
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 16.dp)
                        .padding(top = 56.dp), // Add padding to not overlap with score
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items(ColorType.entries) { colorType ->
                        ColorButton(
                            colorType = colorType,
                            isSelected = colorType == selectedColor,
                            onClick = { selectedColor = colorType }
                        )
                    }

                    item {
                        // Pause button - circular and same size as color buttons
                        PauseButton(
                            isPaused = isPaused,
                            onClick = { isPaused = !isPaused }
                        )
                    }
                }
            }

            // Pause overlay with blur and menu
            if (isPaused) {
                PauseOverlay(
                    onResume = { isPaused = false },
                    onRestart = {
                        resetTrigger++
                        viewModel.resetScore()
                        isPaused = false
                    },
                    currentTheme = currentTheme,
                    onThemeChange = { newTheme -> currentTheme = newTheme }
                )
            }
        }
    }
}

@Composable
private fun ColorButton(
    colorType: ColorType,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val theme = LocalTheme.current
    val colorScheme = theme.toColorScheme()
    val color = mapObstacleColorToTheme(colorType, colorScheme)
    Box(
        modifier = Modifier
            .size(48.dp)
            .then(
                if (isSelected) {
                    Modifier.border(
                        width = 6.dp,
                        color = if(theme == Theme.DARK) {
                            color.lighten(0.5f)
                        } else {
                            color.darken(0.5f)
                        },
                    )
                } else {
                    Modifier
                }
            )
            .background(color),
        contentAlignment = Alignment.Center
    ) {
        Button(
            onClick = onClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                contentColor = Color.Transparent
            )
        ) {}
    }
}

private fun Color.darken(f: Float): Color {
    return Color(
        red = (red * f).coerceIn(0f, 1f),
        green = (green * f).coerceIn(0f, 1f),
        blue = (blue * f).coerceIn(0f, 1f),
        alpha = alpha
    )
}

private fun Color.lighten(f: Float): Color {
    return Color(
        red = (red + (1f - red) * f).coerceIn(0f, 1f),
        green = (green + (1f - green) * f).coerceIn(0f, 1f),
        blue = (blue + (1f - blue) * f).coerceIn(0f, 1f),
        alpha = alpha
    )
}

@Composable
private fun PauseButton(
    isPaused: Boolean,
    onClick: () -> Unit
) {
    val colorScheme = LocalTheme.current.toColorScheme()
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(colorScheme.pauseButtonBackground),
        contentAlignment = Alignment.Center
    ) {
        Button(
            onClick = onClick,
            modifier = Modifier.size(40.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                contentColor = colorScheme.pauseButtonIcon
            ),
            contentPadding = PaddingValues(0.dp)
        ) {
            Text(
                text = if (isPaused) "▶" else "⏸",
                fontSize = 16.sp,
                color = colorScheme.pauseButtonIcon
            )
        }
    }
}