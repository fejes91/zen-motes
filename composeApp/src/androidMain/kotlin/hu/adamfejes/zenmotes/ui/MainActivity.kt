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
    *   implement consent dialog for GDPR https://developers.google.com/admob/android/privacy
    *   implement about screen with privacy policy link and 3rd party licenses
    *   add redesigned app icon with multiple castles depicted

    * Bugs:
    *  After restart sand color indicator is inconsistent until next color change
    *  Fix obstacle generator to not generate obstacles on each other
    *  iOS doesn't use custom font, except theme switcher
    *  game balance
    *   game is a bit hard in the beginning and after some time there is a threshold where a lot of time is banked in, then suddenly it ends
    *   make not fully random obstacle generation, make patterns?
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
