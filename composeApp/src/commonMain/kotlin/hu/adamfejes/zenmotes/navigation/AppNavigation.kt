package hu.adamfejes.zenmotes.navigation

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.dialog
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import hu.adamfejes.zenmotes.logic.GameStateHolder
import hu.adamfejes.zenmotes.service.AnalyticsService
import hu.adamfejes.zenmotes.service.PreferencesService
import hu.adamfejes.zenmotes.ui.GameScreen
import hu.adamfejes.zenmotes.ui.GameOverDialog
import hu.adamfejes.zenmotes.ui.OrientationWarningDialog
import hu.adamfejes.zenmotes.ui.PauseDialog
import hu.adamfejes.zenmotes.ui.theme.AppTheme
import hu.adamfejes.zenmotes.ui.theme.Theme
import kotlinx.coroutines.flow.first
import org.koin.compose.koinInject

val LocalTheme = staticCompositionLocalOf {
    Theme.DARK
}

sealed class Screen(val route: String) {
    data object Game : Screen("game")
    data object Pause : Screen("pause")
    data object GameOver : Screen("gameover")
    data object OrientationWarning : Screen("orientation_warning")
}

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController()
) {
    val gameStateHolder = koinInject<GameStateHolder>()
    val preferencesService = koinInject<PreferencesService>()
    val analyticsService = koinInject<AnalyticsService>()
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route
    val isPaused = currentRoute == Screen.Pause.route
    val isOnOrientationWarning = currentRoute == Screen.OrientationWarning.route

    // Track screen navigation
    DisposableEffect(currentRoute) {
        currentRoute?.let { route ->
            analyticsService.trackScreenView(route)
        }
        onDispose { }
    }

    // Use preferences theme with system fallback
    val appTheme by preferencesService.getTheme.collectAsState(initial = AppTheme.SYSTEM)
    val isSystemDarkTheme = isSystemInDarkTheme()
    val currentTheme = when (appTheme) {
        AppTheme.LIGHT -> Theme.LIGHT
        AppTheme.DARK -> Theme.DARK
        AppTheme.SYSTEM -> if (isSystemDarkTheme) Theme.DARK else Theme.LIGHT
    }

    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner, isPaused) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> {
                    if (!isPaused) {
                        gameStateHolder.onPause()
                        navController.navigate(Screen.Pause.route)
                    }
                }

                else -> {}
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Pause game when orientation changes to landscape
    val configuration = LocalConfiguration.current
    var isLandscape by remember { mutableStateOf(configuration.screenWidthDp > configuration.screenHeightDp) }

    LaunchedEffect(configuration.screenWidthDp, configuration.screenHeightDp) {
        val newIsLandscape = configuration.screenWidthDp > configuration.screenHeightDp
        if (newIsLandscape && !isLandscape) {
            gameStateHolder.onPause()
            navController.navigate(Screen.OrientationWarning.route)
        } else if (!newIsLandscape && currentRoute == Screen.OrientationWarning.route) {
            navController.popBackStack()
            // Check what screen we returned to after popping back
            val previousRoute = navController.currentBackStackEntry?.destination?.route
            if (previousRoute == Screen.Game.route) {
                gameStateHolder.onResume()
            }
        }
        isLandscape = newIsLandscape
    }

    CompositionLocalProvider(LocalTheme provides currentTheme) {
        NavHost(
            navController = navController,
            startDestination = Screen.Game.route
        ) {
            composable(Screen.Game.route) {
                GameScreen(
                    onNavigateToPause = {
                        navController.navigate(Screen.Pause.route)
                    },
                    onNavigateToGameOver = {
                        navController.navigate(Screen.GameOver.route)
                    }
                )
            }

            dialog(
                route = Screen.Pause.route,
                dialogProperties = DialogProperties(
                    usePlatformDefaultWidth = false,
                    dismissOnBackPress = false
                )
            ) {
                PauseDialog(
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }

            dialog(
                route = Screen.GameOver.route,
                dialogProperties = DialogProperties(
                    usePlatformDefaultWidth = false,
                    dismissOnBackPress = false
                )
            ) {
                GameOverDialog(
                    onBack = {
                        navController.navigate(Screen.Game.route) {
                            popUpTo(Screen.Game.route) {
                                inclusive = true
                            }
                        }
                    }
                )
            }

            dialog(
                route = Screen.OrientationWarning.route,
                dialogProperties = DialogProperties(
                    usePlatformDefaultWidth = false,
                    dismissOnBackPress = false
                )
            ) {
                OrientationWarningDialog()
            }
        }
    }
}