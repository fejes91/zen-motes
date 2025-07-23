package hu.adamfejes.zenmotes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PauseOverlay(
    onResume: () -> Unit,
    onRestart: () -> Unit,
    currentTheme: Theme,
    onThemeChange: (Theme) -> Unit
) {
    val colorScheme = getColorScheme(currentTheme)

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
                        color = colorScheme.themeSwitchText,
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
                        color = colorScheme.themeSwitchText,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }
    }
}