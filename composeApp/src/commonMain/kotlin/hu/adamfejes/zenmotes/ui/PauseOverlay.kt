package hu.adamfejes.zenmotes.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import hu.adamfejes.zenmotes.ui.theme.Theme
import hu.adamfejes.zenmotes.ui.theme.toColorScheme

@Composable
fun PauseOverlay(
    onResume: () -> Unit,
    onRestart: () -> Unit,
    currentTheme: Theme,
    onThemeChange: (Theme) -> Unit
) {
    val colorScheme = LocalTheme.current.toColorScheme()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.pauseOverlayBackground),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .padding(32.dp)
                .widthIn(min = 280.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(
                text = "Paused",
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = colorScheme.pausedTitleText
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // TODO pixelated button design
                Button(
                    onClick = onResume,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorScheme.primaryButtonBackground
                    )
                ) {
                    Text(
                        text = "Resume",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colorScheme.primaryButtonText
                    )
                }

                Button(
                    onClick = onRestart,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorScheme.secondaryButtonBackground
                    )
                ) {
                    Text(
                        text = "Restart",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colorScheme.secondaryButtonText
                    )
                }

                // Theme switch
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Light",
                        fontSize = if (currentTheme == Theme.LIGHT) 16.sp else 14.sp,
                        fontWeight = if (currentTheme == Theme.LIGHT) FontWeight.SemiBold else FontWeight.Normal,
                        color = colorScheme.textColor,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Switch(
                        checked = currentTheme == Theme.DARK,
                        onCheckedChange = { isDarkTheme ->
                            onThemeChange(if (isDarkTheme) Theme.DARK else Theme.LIGHT)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = colorScheme.primaryButtonBackground,
                            checkedTrackColor = colorScheme.primaryButtonText,
                            uncheckedThumbColor = colorScheme.secondaryButtonBackground,
                            uncheckedTrackColor = colorScheme.secondaryButtonText
                        )
                    )
                    Text(
                        text = "Dark",
                        fontSize = if (currentTheme == Theme.DARK) 16.sp else 14.sp,
                        fontWeight = if (currentTheme == Theme.DARK) FontWeight.SemiBold else FontWeight.Normal,
                        color = colorScheme.textColor,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }
    }
}