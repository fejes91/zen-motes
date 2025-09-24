package hu.adamfejes.zenmotes.navigation

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.staticCompositionLocalOf
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
import hu.adamfejes.zenmotes.logic.GameStateHolder
import hu.adamfejes.zenmotes.service.PreferencesService
import hu.adamfejes.zenmotes.ui.GameScreen
import hu.adamfejes.zenmotes.ui.PauseDialog
import hu.adamfejes.zenmotes.ui.theme.AppTheme
import hu.adamfejes.zenmotes.ui.theme.Theme
import org.koin.compose.koinInject

val LocalTheme = staticCompositionLocalOf {
    Theme.DARK
}

sealed class Screen(val route: String) {
    data object Game : Screen("game")
    data object Pause : Screen("pause")
}

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController()
) {
    val gameStateHolder = koinInject<GameStateHolder>()
    val preferencesService = koinInject<PreferencesService>()
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route
    val isPaused = currentRoute == Screen.Pause.route

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
                    if(!isPaused) {
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

    CompositionLocalProvider(LocalTheme provides currentTheme) {
        NavHost(
            navController = navController,
            startDestination = Screen.Game.route
        ) {
            composable(Screen.Game.route) {
                GameScreen(
                    onNavigateToPause = {
                        navController.navigate(Screen.Pause.route)
                    }
                )
            }

            dialog(route = Screen.Pause.route,
                dialogProperties = DialogProperties(
                    usePlatformDefaultWidth = false
                )
            ) {
                PauseDialog(
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}