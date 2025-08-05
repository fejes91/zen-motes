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
    * Enable Logging only in debug builds
    * Color picker selected state + redesign?
    *   Colors are randomly changing. Next one is show on the top in a colorbar and warns about the change
    * Go full screen, immersive mode
    * Play sounds for: sea, sand, obstacle destruction, score change?
    * Fix obstacle generator to not generate obstacles on each other
    * Settings, shared preferences
    *   Theme
    *   Game mode: Zen / Time attack?
    *   High Score
    * Scoring
    *   Scores are adding when obstacles are destroyed.
    *   Scores are deducted when obstacles are leaving the screen without being destroyed.
    *   Score is shown in the top center of the screen.
    *   Additional score is animating into the center from the left
    *   Deducted score is animating out of the center to the right
    *   Timer is counting under the score
    *   The longer you can keep the score above 0, the higher rank you get
    *   When score is below 0 for 5 secs, game is over
    * Continuously changing sand color?
    * More obstacle designs?
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
