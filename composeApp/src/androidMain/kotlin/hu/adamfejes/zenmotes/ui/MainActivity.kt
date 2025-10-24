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
    *   show sand control
    *   show destruction with same color
    *   explain time and scoring
    *   show destruction with different color
    *   show chain reaction
    *  Hire designer

    * Bugs:
    *  Fix obstacle generator to not generate obstacles on each other
    *  iOS doesn't use custom font, except theme switcher
    *  game balance
    *   game is a bit hard in the beginning and after some time there is a threshold where a lot of time is banked in, then suddenly it ends
    *   make not fully random obstacle generation, make patterns?
    *   stop playing sound when game goes to background
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
