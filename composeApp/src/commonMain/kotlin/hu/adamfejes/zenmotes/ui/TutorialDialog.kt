package hu.adamfejes.zenmotes.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import hu.adamfejes.zenmotes.navigation.LocalTheme
import hu.adamfejes.zenmotes.ui.theme.ZenMotesTheme
import hu.adamfejes.zenmotes.ui.theme.toColorScheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
import zenmotescmp.composeapp.generated.resources.Res
import zenmotescmp.composeapp.generated.resources.tutorial_dialog_button
import zenmotescmp.composeapp.generated.resources.tutorial_dialog_instruction_1
import zenmotescmp.composeapp.generated.resources.tutorial_dialog_instruction_2
import zenmotescmp.composeapp.generated.resources.tutorial_dialog_instruction_3
import zenmotescmp.composeapp.generated.resources.tutorial_dialog_instruction_4
import zenmotescmp.composeapp.generated.resources.tutorial_dialog_instruction_5
import zenmotescmp.composeapp.generated.resources.tutorial_dialog_instruction_6
import zenmotescmp.composeapp.generated.resources.tutorial_dialog_title

@Composable
fun TutorialDialog(
    viewModel: TutorialViewModel = koinViewModel(),
    onDismiss: () -> Unit
) {
    LaunchedEffect(Unit) {
        viewModel.initializeTutorial()
    }

    TutorialDialogContent(
        onDismiss = onDismiss
    )
}

@Composable
private fun TutorialDialogContent(
    onDismiss: () -> Unit
) {
    val colorScheme = LocalTheme.current.toColorScheme()

    var sandColor by remember { mutableStateOf(colorScheme.sandColors.random()) }

    LaunchedEffect(Unit) {
        while (isActive) {
            sandColor = colorScheme.sandColors.random()
            delay(3000L)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                onClick = onDismiss
            )
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(Res.string.tutorial_dialog_title),
                fontSize = 48.sp,
                textAlign = TextAlign.Center,
                lineHeight = 48.sp,
                color = colorScheme.pausedTitleText
            )

            Text(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(Res.string.tutorial_dialog_instruction_1),
                fontSize = 22.sp,
                lineHeight = 22.sp,
                color = colorScheme.pausedTitleText
            )

            Text(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(Res.string.tutorial_dialog_instruction_2),
                fontSize = 22.sp,
                lineHeight = 22.sp,
                color = sandColor
            )

            Text(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(Res.string.tutorial_dialog_instruction_3),
                fontSize = 22.sp,
                lineHeight = 22.sp,
                color = colorScheme.pausedTitleText
            )

            Text(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(Res.string.tutorial_dialog_instruction_4),
                fontSize = 22.sp,
                lineHeight = 22.sp,
                color = colorScheme.pausedTitleText
            )

            Text(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(Res.string.tutorial_dialog_instruction_5),
                fontSize = 22.sp,
                lineHeight = 22.sp,
                color = colorScheme.negativeText
            )

            Text(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(Res.string.tutorial_dialog_instruction_6),
                fontSize = 22.sp,
                lineHeight = 22.sp,
                color = colorScheme.positiveText
            )

            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
                shape = RectangleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorScheme.primaryButtonBackground
                )
            ) {
                Text(
                    text = stringResource(Res.string.tutorial_dialog_button),
                    color = colorScheme.primaryButtonText,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Preview
@Composable
fun TutorialDialogPreview() {
    ZenMotesTheme {
        TutorialDialogContent(
            onDismiss = {}
        )
    }
}