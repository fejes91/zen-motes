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
    * Configure admob after publishing
    * Control ads from Firebase remote config
    * Setup Firebase analytics for iOS

    * TODOs for release
    *   Fix sand collision when two different heap intersects, one should fall down? Or mix? Or disappear?
    *   Hardest difficulty could be harder
    *       Faster castles?
    *       Lower chance for matching color?

    * Bugs:
    *  Fix obstacle generator to not generate obstacles on each other
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
