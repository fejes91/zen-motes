package hu.adamfejes.zenmotes.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
    *  Translate strings
    *  Hire designer

    * Bugs:
    *  Fix obstacle generator to not generate obstacles on each other
    *  do not play failed sound on main menu
    *  choose font which support accents in spanish and hungarian
    *  iOS doesn't use custom font, except theme switcher
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
