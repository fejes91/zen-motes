package hu.adamfejes.zenmotes.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import hu.adamfejes.zenmotes.navigation.AppNavigation
import hu.adamfejes.zenmotes.ui.theme.ZenMotesTheme

/*
    * ZenMotes
    * TODOs
    * Settings, shared preferences
    *   Game mode: Zen / Time attack?
    *   High Score
    * Scoring
    *   Game is until the timer reaches zero, the score will be written into a leaderboard
    * More obstacle designs?
    * Play ticking sound when time is running out
    *
    * Bugs:
    *  Fix obstacle generator to not generate obstacles on each other
    *  Block back navigation on pause and game over dialogs
 */

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        enableEdgeToEdge()
        setContent {
            ZenMotesTheme {
                AppNavigation()
            }
        }
    }
}
