package hu.adamfejes.zenmotes.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import hu.adamfejes.zenmotes.logic.SandColorManager
import hu.adamfejes.zenmotes.logic.ScoreEvent
import hu.adamfejes.zenmotes.logic.SlidingObstacle
import hu.adamfejes.zenmotes.navigation.LocalTheme
import hu.adamfejes.zenmotes.ui.Constants.SCORE_FLY_DURATION
import hu.adamfejes.zenmotes.ui.theme.AppTheme
import hu.adamfejes.zenmotes.ui.theme.toColorScheme
import kotlinx.coroutines.delay
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun GameScreen(
    viewModel: SandSimulationViewModel = koinViewModel(),
    onNavigateToPause: () -> Unit,
    onNavigateToGameOver: () -> Unit
) {
    val sandColorManager: SandColorManager = koinInject()
    val score by viewModel.score.collectAsState(0)
    val scoreEvent by viewModel.scoreEvent.collectAsState(null)
    val currentAppTheme by viewModel.appTheme.collectAsState()
    val countDownTime by viewModel.countDownTimeMillis.collectAsState()
    val isPaused by viewModel.isPaused.collectAsState()

    GameScreenContent(
        isPaused = isPaused,
        currentAppTheme = currentAppTheme,
        scoreEvent = scoreEvent,
        score = score,
        countDownTime = countDownTime,
        increaseScore = viewModel::increaseScore,
        decreaseScore = viewModel::decreaseScore,
        startSession = viewModel::startSession,
        pauseSession = viewModel::pauseSession,
        sandColorManager = sandColorManager,
        playSound = viewModel::playSound,
        onNavigateToPause = onNavigateToPause,
        onNavigateToGameOver = onNavigateToGameOver
    )
}

@Composable
private fun GameScreenContent(
    isPaused: Boolean,
    currentAppTheme: AppTheme?,
    scoreEvent: ScoreEvent?,
    score: Int,
    countDownTime: Long,
    increaseScore: (SlidingObstacle, Boolean) -> Unit,
    decreaseScore: (SlidingObstacle) -> Unit,
    startSession: () -> Unit,
    pauseSession: () -> Unit,
    playSound: (Int) -> Unit,
    sandColorManager: SandColorManager,
    onNavigateToPause: () -> Unit,
    onNavigateToGameOver: () -> Unit
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

    val currentSandColor by sandColorManager.currentSandColor.collectAsState()
    val nextSandColor by sandColorManager.nextSandColor.collectAsState()

    LaunchedEffect(Unit) {
        startSession()
    }

    // Game over logic - watch for when countdown reaches zero
    LaunchedEffect(countDownTime) {
        if (countDownTime == 0L && !isPaused) {
            delay(SCORE_FLY_DURATION.toLong()) // Wait for any final animations
            pauseSession()
            onNavigateToGameOver()
        }
    }

    val colorScheme = LocalTheme.current.toColorScheme()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .then(if (isPaused) Modifier.blur(20.dp)
                .background(colorScheme.pauseOverlayBackground)
            else Modifier)
    ) {
        // Sand simulation view - full edge-to-edge behind everything
        SandView(
            modifier = Modifier.fillMaxSize(),
            sandColorType = currentSandColor,
            sandGenerationAmount = 5,
            showPerformanceOverlay = true, // Toggle performance overlay for testing
            isPaused = isPaused,
            increaseScore = increaseScore,
            decreaseScore = decreaseScore
        )

        // Color indicator bar at the very top
        ColorIndicatorBar(
            currentColor = currentSandColor,
            nextColor = nextSandColor,
        )

        // Score display at the top center
        Scores(
            score = score,
            countDownTimeMillis = countDownTime,
            activeScoreEvents = activeScoreEvents,
            onAnimationNearlyComplete = { obstacleId ->
                playSound(activeScoreEvents.first { it.obstacleId == obstacleId }.score)
            },
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
            PauseButton {
                pauseSession()
                onNavigateToPause()
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
                text = "PAUSE",
                fontSize = 10.sp,
                color = colorScheme.pauseButtonIcon
            )
        }
    }
}