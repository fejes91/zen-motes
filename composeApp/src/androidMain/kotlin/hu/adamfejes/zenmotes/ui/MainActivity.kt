package hu.adamfejes.zenmotes.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import hu.adamfejes.zenmotes.ui.theme.ZenMotesTheme

/*
    * ZenMotes
    * TODOs
    * Play sounds for: sea, sand, obstacle destruction, score change?
    * Settings, shared preferences
    *   Game mode: Zen / Time attack?
    *   High Score
    * Scoring
    *   Timer is counting down under the score
    *   When obstacle is destroyed, time is increased, when an obstacle is missed, time is decreased
    *   Game is until the timer reaches zero, the score will be written into a leaderboard
    * More obstacle designs?
    *
    * Bugs:
    *  Fix obstacle generator to not generate obstacles on each other
    *  Sound doesn't stop when game is paused or backgrounded
 */

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        enableEdgeToEdge()
        setContent {
            ZenMotesTheme {
                SandSimulation(
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
