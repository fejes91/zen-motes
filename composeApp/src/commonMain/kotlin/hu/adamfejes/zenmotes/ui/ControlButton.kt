package hu.adamfejes.zenmotes.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import hu.adamfejes.zenmotes.navigation.LocalTheme
import hu.adamfejes.zenmotes.ui.theme.toColorScheme

@Composable
fun ControlButton(onClick: () -> Unit, content: @Composable () -> Unit) {
    val colorScheme = LocalTheme.current.toColorScheme()
    Box(
        modifier = Modifier
            .size(48.dp)
            .background(colorScheme.pauseButtonBackground),
        contentAlignment = Alignment.Center
    ) {
        Button(
            onClick = onClick,
            modifier = Modifier.size(48.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                contentColor = colorScheme.pauseButtonIcon
            ),
            contentPadding = PaddingValues(0.dp)
        ) {
            content()
        }
    }
}
