package hu.adamfejes.zenmotes.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import hu.adamfejes.zenmotes.logic.ScoreEvent
import androidx.compose.ui.Alignment
import kotlinx.coroutines.delay
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import hu.adamfejes.zenmotes.logic.ColorType
import hu.adamfejes.zenmotes.logic.SandColorManager
import hu.adamfejes.zenmotes.logic.SlidingObstacle
import hu.adamfejes.zenmotes.ui.Constants.COLOR_CHANGE_ANIMATION_DURATION
import hu.adamfejes.zenmotes.ui.theme.AppTheme
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
    val sandColorManager: SandColorManager = koinInject()
    val score by viewModel.score.collectAsState(0)
    val scoreEvent by viewModel.scoreEvent.collectAsState(null)
    val currentAppTheme by viewModel.appTheme.collectAsState()

    SandSimulationContent(
        modifier = modifier,
        currentAppTheme = currentAppTheme,
        scoreEvent = scoreEvent,
        score = score,
        resetScore = viewModel::resetScore,
        setTheme = viewModel::setTheme,
        increaseScore = viewModel::increaseScore,
        decreaseScore = viewModel::decreaseScore,
        toggleAddingSand = viewModel::toggleAddingSand,
        sandColorManager = sandColorManager
    )
}

@Composable
private fun SandSimulationContent(
    modifier: Modifier,
    currentAppTheme: AppTheme?,
    scoreEvent: ScoreEvent?,
    score: Int,
    resetScore: () -> Unit,
    setTheme: (AppTheme) -> Unit,
    increaseScore: (SlidingObstacle, Boolean) -> Unit,
    decreaseScore: (SlidingObstacle) -> Unit,
    toggleAddingSand: (Boolean) -> Unit,
    sandColorManager: SandColorManager
) {
    if (currentAppTheme == null) {
        return
    }

    // Manage active score events for animation
    var activeScoreEvents by remember { mutableStateOf<Set<ScoreEvent>>(emptySet()) }

    // Add new score events to the active list
    LaunchedEffect(scoreEvent?.obstacleId) {
        scoreEvent?.let { event ->
            activeScoreEvents = activeScoreEvents + event
        }
    }

    val isSystemDarkTheme = isSystemInDarkTheme()
    val currentTheme by derivedStateOf {
        when (currentAppTheme) {
            AppTheme.LIGHT -> Theme.LIGHT
            AppTheme.DARK -> Theme.DARK
            AppTheme.SYSTEM -> if (isSystemDarkTheme) Theme.DARK else Theme.LIGHT
        }
    }
    val currentSandColor by sandColorManager.currentSandColor.collectAsState()
    val nextSandColor by sandColorManager.nextSandColor.collectAsState()
    var isPaused by remember { mutableStateOf(false) }
    var resetTrigger by remember { mutableIntStateOf(0) }

    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> {
                    isPaused = true
                    sandColorManager.pause()
                }
                Lifecycle.Event.ON_RESUME -> {
                    sandColorManager.resume()
                }
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
                    sandColorType = currentSandColor,
                    sandGenerationAmount = 60,
                    showPerformanceOverlay = true, // Toggle performance overlay for testing
                    isPaused = isPaused,
                    resetTrigger = resetTrigger,
                    increaseScore = increaseScore,
                    decreaseScore = decreaseScore,
                    toggleAddingSand = toggleAddingSand
                )

                // Color indicator bar at the very top
                ColorIndicatorBar(
                    currentColor = currentSandColor,
                    nextColor = nextSandColor,
                )

                // Score display at the top center
                Scores(
                    score = score,
                    activeScoreEvents = activeScoreEvents,
                    onAnimationComplete = { obstacleId ->
                        activeScoreEvents =
                            activeScoreEvents.filter { it.obstacleId != obstacleId }
                                .toSet()
                    })

                // Top UI overlay - pause button only
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(end = 16.dp),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    PauseButton { isPaused = !isPaused }
                }
            }

            // Pause overlay with blur and menu
            if (isPaused) {
                PauseOverlay(
                    onResume = {
                        isPaused = false
                        sandColorManager.resume()
                    },
                    onRestart = {
                        resetTrigger++
                        resetScore()
                        isPaused = false
                    },
                    currentAppTheme = currentAppTheme,
                    onThemeChange = { newTheme -> setTheme(newTheme) }
                )
            }
        }
    }
}

@Composable
private fun PauseButton(onClick: () -> Unit) {
    val colorScheme = LocalTheme.current.toColorScheme()
    Box(
        modifier = Modifier
            .size(48.dp)
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
                text = "Pause",
                fontSize = 6.sp,
                color = colorScheme.pauseButtonIcon
            )
        }
    }
}