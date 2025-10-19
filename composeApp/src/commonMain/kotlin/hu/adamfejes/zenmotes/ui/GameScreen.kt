package hu.adamfejes.zenmotes.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.windowInsetsPadding
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
import hu.adamfejes.zenmotes.navigation.LocalTheme
import hu.adamfejes.zenmotes.ui.Constants.SCORE_FLY_DURATION
import hu.adamfejes.zenmotes.ui.theme.AppTheme
import hu.adamfejes.zenmotes.ui.theme.toColorScheme
import hu.adamfejes.zenmotes.utils.createScoreDecreaseEvent
import hu.adamfejes.zenmotes.utils.createScoreIncreaseEvent
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import zenmotescmp.composeapp.generated.resources.Res
import zenmotescmp.composeapp.generated.resources.game_screen_pause

@Composable
fun GameScreen(
    viewModel: SandSimulationViewModel = koinViewModel(),
    onNavigateToPause: () -> Unit,
    onNavigateToGameOver: () -> Unit
) {
    val sandColorManager: SandColorManager = koinInject()
    val score by viewModel.score.collectAsState(0)
    val currentAppTheme by viewModel.appTheme.collectAsState()
    val countDownTime by viewModel.countDownTimeMillis.collectAsState()
    val isPaused by viewModel.isPaused.collectAsState()
    val isDemoMode by viewModel.isDemoMode.collectAsState()

    GameScreenContent(
        isPaused = isPaused,
        isDemoMode = isDemoMode,
        currentAppTheme = currentAppTheme,
        score = score,
        countDownTime = countDownTime,
        updateScore = viewModel::updateScore,
        pauseSession = viewModel::pauseSession,
        sandColorManager = sandColorManager,
        playSound = viewModel::playSound,
        onNavigateToPause = onNavigateToPause,
        onNavigateToGameOver = onNavigateToGameOver,
    )
}

@Composable
private fun GameScreenContent(
    isPaused: Boolean,
    isDemoMode: Boolean,
    currentAppTheme: AppTheme?,
    score: Int,
    countDownTime: Long,
    updateScore: (Int) -> Unit,
    pauseSession: () -> Unit,
    playSound: (Int) -> Unit,
    sandColorManager: SandColorManager,
    onNavigateToPause: () -> Unit,
    onNavigateToGameOver: () -> Unit,
) {
    if (currentAppTheme == null) {
        return
    }

    // Manage active score events for animation
    var activeScoreEvents by remember { mutableStateOf<Set<ScoreEvent>>(emptySet()) }

    val currentSandColor by sandColorManager.currentSandColor.collectAsState()
    val nextSandColor by sandColorManager.nextSandColor.collectAsState()

    // Game over logic - watch for when countdown reaches zero
    LaunchedEffect(countDownTime) {
        if (countDownTime == 0L && !isPaused) {
            delay(SCORE_FLY_DURATION.toLong()) // Wait for any final animations
            pauseSession()
            onNavigateToGameOver()
        }
    }

    val colorScheme = LocalTheme.current.toColorScheme()

    ColorIndicatorBar(
        currentColor = currentSandColor,
        nextColor = nextSandColor,
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .then(
                if (isPaused || isDemoMode) Modifier.blur(20.dp)
                    .background(colorScheme.pauseOverlayBackground)
                else Modifier
            )
            .windowInsetsPadding(WindowInsets.statusBars)
    ) {
        // Sand simulation view - full edge-to-edge behind everything
        SandView(
            modifier = Modifier.fillMaxSize(),
            sandColorType = currentSandColor,
            sandGenerationAmount = 5,
            showPerformanceOverlay = false, // Toggle performance overlay for testing
            isPaused = isPaused,
            increaseScore = { slidingObstacle, isBonus ->
                val event = createScoreIncreaseEvent(isBonus, slidingObstacle)
                activeScoreEvents = activeScoreEvents + event

            },
            decreaseScore = { slidingObstacle ->
                val event = createScoreDecreaseEvent(slidingObstacle)
                activeScoreEvents = activeScoreEvents + event
            }
        )

        // Score display at the top center
        Scores(
            modifier = Modifier.padding(top = 8.dp),
            score = score,
            countDownTimeMillis = countDownTime,
            activeScoreEvents = activeScoreEvents,
            onAnimationComplete = { event ->
                playSound(activeScoreEvents.first { it.obstacle.id == event.obstacle.id }.score)
                activeScoreEvents =
                    activeScoreEvents.filter { it.obstacle.id != event.obstacle.id }
                        .toSet()

                updateScore(event.score)
            })

        // Top UI overlay - pause button only
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(end = 16.dp, top = 8.dp),
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
                text = stringResource(Res.string.game_screen_pause),
                fontSize = 10.sp,
                color = colorScheme.pauseButtonIcon
            )
        }
    }
}