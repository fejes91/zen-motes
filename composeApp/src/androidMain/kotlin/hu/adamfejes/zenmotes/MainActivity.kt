package hu.adamfejes.zenmotes

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
    * Decorated obstacles, built up from sand with shading
    * Settings, shared preferences
    *   Theme
    *   Game mode: Zen / Time attack?
    *   High Score
    * Scoring
    * Continously changing sand color?
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
