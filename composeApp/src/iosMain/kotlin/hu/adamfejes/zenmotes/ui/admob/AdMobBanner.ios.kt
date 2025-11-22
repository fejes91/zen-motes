package hu.adamfejes.zenmotes.ui.admob

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
actual fun AdMobBanner(unitId: String, modifier: Modifier) {
    Text(text = "Here comes an AdMob banner", modifier = modifier)
}