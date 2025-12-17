package hu.adamfejes.zenmotes.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import hu.adamfejes.zenmotes.model.LicenseInfo
import hu.adamfejes.zenmotes.navigation.LocalTheme
import hu.adamfejes.zenmotes.rememberOpenUrl
import hu.adamfejes.zenmotes.ui.theme.toColorScheme
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun InfoDialog(
    viewModel: InfoViewModel = koinViewModel(),
    onDismiss: () -> Unit
) {
    val licenses by viewModel.licenses.collectAsState()
    val colorScheme = LocalTheme.current.toColorScheme()
    val openUrl = rememberOpenUrl()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colorScheme.background.copy(alpha = 0.85f))
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Third-Party Licenses",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.textColorOnBackground
                    )

                    ControlButton(onClick = onDismiss) {
                        Text(
                            text = "X",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Privacy and Terms",
                    fontSize = 14.sp,
                    color = colorScheme.textColorOnBackground,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier.clickable {
                        openUrl("https://fejes91.github.io/castle-blaster/")
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(licenses) { license ->
                        LicenseItem(license, colorScheme)
                    }
                }
            }
        }
    }
}

@Composable
private fun LicenseItem(license: LicenseInfo, colorScheme: hu.adamfejes.zenmotes.ui.theme.ColorScheme) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = colorScheme.pauseButtonBackground.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = license.name,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = colorScheme.textColorOnBackground
            )

            Text(
                text = "${license.groupId}:${license.artifactId}:${license.version}",
                fontSize = 12.sp,
                color = colorScheme.textColorOnBackground.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(8.dp))

            license.licenses.forEach { spdxLicense ->
                Text(
                    text = "License: ${spdxLicense.name}",
                    fontSize = 12.sp,
                    color = colorScheme.textColorOnBackground.copy(alpha = 0.8f)
                )
            }
        }
    }
}