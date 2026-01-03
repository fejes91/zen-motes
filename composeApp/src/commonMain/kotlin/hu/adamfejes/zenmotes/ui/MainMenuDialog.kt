package hu.adamfejes.zenmotes.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.TweenSpec
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import hu.adamfejes.zenmotes.BackHandler
import hu.adamfejes.zenmotes.navigation.LocalTheme
import hu.adamfejes.zenmotes.ui.components.AppThemeSwitch
import hu.adamfejes.zenmotes.ui.theme.ColorScheme
import hu.adamfejes.zenmotes.ui.theme.toColorScheme
import hu.adamfejes.zenmotes.utils.formatScore
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import zenmotescmp.composeapp.generated.resources.Res
import zenmotescmp.composeapp.generated.resources.main_menu_app_name1
import zenmotescmp.composeapp.generated.resources.main_menu_app_name2
import zenmotescmp.composeapp.generated.resources.main_menu_high_score_label
import zenmotescmp.composeapp.generated.resources.main_menu_start_game
import zenmotescmp.composeapp.generated.resources.pause_dialog_sound_off
import zenmotescmp.composeapp.generated.resources.pause_dialog_sound_on
import zenmotescmp.composeapp.generated.resources.wider_tower
import kotlin.random.Random

@Composable
fun MainMenuDialog(
    viewModel: MainMenuViewModel = koinViewModel(),
    onStartGame: () -> Unit,
    onShowInfo: () -> Unit = {},
    onExit: () -> Unit = {}
) {
    val currentAppTheme by viewModel.appTheme.collectAsState()
    val soundEnabled by viewModel.soundEnabled.collectAsState()
    val highScore by viewModel.highScore.collectAsState()
    val appVersion by viewModel.appVersion.collectAsState()

    BackHandler(onBack = onExit)

    if (currentAppTheme == null) {
        return
    }

    ConsentDialog()

    DisposableEffect(Unit) {
        viewModel.initialize()

        onDispose {
            viewModel.cleanUp()
        }
    }

    val colorScheme = LocalTheme.current.toColorScheme()
    val animatedRandomColors = getRandomColors(colorScheme)

    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 32.dp, vertical = 64.dp)
                .widthIn(min = 280.dp)
                .fillMaxHeight()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Image(
                    painter = painterResource(Res.drawable.wider_tower),
                    contentDescription = "Castle Tower",
                    modifier = Modifier
                        .size(100.dp),
                    colorFilter = ColorFilter.tint(
                        color = animatedRandomColors[0].value,
                        blendMode = BlendMode.Modulate
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = stringResource(Res.string.main_menu_app_name1),
                    fontSize = 48.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 48.sp,
                    color = animatedRandomColors[1].value,
                )

                Text(
                    text = stringResource(Res.string.main_menu_app_name2),
                    fontSize = 48.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 48.sp,
                    color = animatedRandomColors[2].value,
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Button(
                    onClick = onStartGame,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RectangleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorScheme.primaryButtonBackground
                    )
                ) {
                    Text(
                        modifier = Modifier.padding(vertical = 8.dp),
                        text = stringResource(Res.string.main_menu_start_game),
                        color = colorScheme.primaryButtonText,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1
                    )
                }

                Spacer(modifier = Modifier.height(22.dp))

                Text(
                    text = stringResource(
                        Res.string.main_menu_high_score_label,
                        highScore?.formatScore() ?: "--"
                    ),
                    fontSize = 24.sp,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Medium,
                    color = colorScheme.pausedTitleText
                )
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AppThemeSwitch(
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
                        text = stringResource(if (soundEnabled) Res.string.pause_dialog_sound_on else Res.string.pause_dialog_sound_off),
                        color = if (soundEnabled) colorScheme.primaryButtonText else colorScheme.secondaryButtonText,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1
                    )
                }
            }
        }

        Text(
            text = appVersion ?: "",
            fontSize = 10.sp,
            color = colorScheme.pausedTitleText.copy(alpha = 0.6f),
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
        )

        ControlButton(
            onClick = onShowInfo,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(8.dp)
        ) {
            Text(
                text = "i",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun getRandomColors(colorScheme: ColorScheme) : List<State<Color>> {
    var randomColors by remember {
        mutableStateOf(
            listOf(
                colorScheme.obstacleColors.random(),
                colorScheme.obstacleColors.random(),
                colorScheme.obstacleColors.random()
            )
        )
    }

    val animatedRandomColors = randomColors.map { randomColor ->
        animateColorAsState(targetValue = randomColor, animationSpec = TweenSpec())
    }

    LaunchedEffect(Unit) {
        while (isActive) {
            delay(Random.nextLong(3000L, 6000L))
            randomColors = randomColors.map { randomColor ->
                colorScheme.obstacleColors.filter { it != randomColor }.random()
            }
        }
    }

    return animatedRandomColors
}
