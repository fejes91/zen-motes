package hu.adamfejes.zenmotes.navigation

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
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
import hu.adamfejes.zenmotes.ui.GameScreen
import hu.adamfejes.zenmotes.ui.PauseDialog
import hu.adamfejes.zenmotes.ui.theme.Theme

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
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route
    val isPaused = currentRoute == Screen.Pause.route

    // Use system theme
    val isSystemDarkTheme = isSystemInDarkTheme()
    val currentTheme = if (isSystemDarkTheme) Theme.DARK else Theme.LIGHT

    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner, isPaused) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> {
                    if(!isPaused) {
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
                    isPaused = isPaused,
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