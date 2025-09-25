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
    * TODOs:
    *  Game mode: Zen / Time attack?
    *  More obstacle designs?
    *  Play ticking sound when time is running out
    *  Play game over sound
    *  Add tutorial in the beginning
    *  Basic Firebase analytics and Crashlytics integration
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
