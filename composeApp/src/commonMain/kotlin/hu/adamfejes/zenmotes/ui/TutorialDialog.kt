package hu.adamfejes.zenmotes.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import hu.adamfejes.zenmotes.navigation.LocalTheme
import hu.adamfejes.zenmotes.ui.theme.ZenMotesTheme
import hu.adamfejes.zenmotes.ui.theme.toColorScheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.StringResource
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

sealed class TutorialPage(
    val firstTextRes: StringResource,
    val secondTextRes: StringResource,
    val gifFileName: String
) {
    data object Page1 : TutorialPage(
        firstTextRes = Res.string.tutorial_dialog_instruction_1,
        secondTextRes = Res.string.tutorial_dialog_instruction_2,
        gifFileName = "files/sand_color.gif"
    )

    data object Page2 : TutorialPage(
        firstTextRes = Res.string.tutorial_dialog_instruction_3,
        secondTextRes = Res.string.tutorial_dialog_instruction_4,
        gifFileName = "files/sand_color.gif"
    )

    data object Page3 : TutorialPage(
        firstTextRes = Res.string.tutorial_dialog_instruction_5,
        secondTextRes = Res.string.tutorial_dialog_instruction_6,
        gifFileName = "files/sand_color.gif"
    )
}

@Composable
private fun TutorialGifPlayer(
    gifFileName: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f)
            .clip(RoundedCornerShape(8.dp))
    ) {
        AsyncImage(
            model = Res.getUri(gifFileName),
            contentDescription = null,
            modifier = Modifier.fillMaxSize()
        )
    }
}

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
    val pages = remember { listOf(TutorialPage.Page1, TutorialPage.Page2, TutorialPage.Page3) }
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val coroutineScope = rememberCoroutineScope()

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
        Box {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(Res.string.tutorial_dialog_title),
                    fontSize = 48.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 48.sp,
                    color = colorScheme.pausedTitleText
                )

                Spacer(modifier = Modifier.height(48.dp))

                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxWidth()
                ) { pageIndex ->
                    val page = pages[pageIndex]
                    Column(
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        TutorialGifPlayer(
                            gifFileName = page.gifFileName,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            modifier = Modifier.fillMaxWidth(),
                            text = stringResource(page.firstTextRes),
                            fontSize = 22.sp,
                            lineHeight = 22.sp,
                            color = when (pageIndex) {
                                0 -> colorScheme.pausedTitleText
                                1 -> colorScheme.pausedTitleText
                                2 -> colorScheme.negativeText
                                else -> colorScheme.pausedTitleText
                            }
                        )

                        Text(
                            modifier = Modifier.fillMaxWidth(),
                            text = stringResource(page.secondTextRes),
                            fontSize = 22.sp,
                            lineHeight = 22.sp,
                            color = when (pageIndex) {
                                0 -> sandColor
                                1 -> colorScheme.pausedTitleText
                                2 -> colorScheme.positiveText
                                else -> colorScheme.pausedTitleText
                            }
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 32.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage - 1)
                        }
                    },
                    enabled = pagerState.currentPage > 0,
                    modifier = Modifier.weight(1f),
                    shape = RectangleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorScheme.primaryButtonBackground
                    )
                ) {
                    Text(
                        text = "<",
                        color = colorScheme.primaryButtonText,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .weight(1f)
                        .then(
                            if (pagerState.currentPage < pages.size - 1) {
                                Modifier.padding(0.dp)
                            } else {
                                Modifier
                            }
                        ),
                    shape = RectangleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (pagerState.currentPage < pages.size - 1) {
                            colorScheme.primaryButtonBackground.copy(alpha = 0f)
                        } else {
                            colorScheme.primaryButtonBackground
                        }
                    )
                ) {
                    Text(
                        text = stringResource(Res.string.tutorial_dialog_button),
                        color = if (pagerState.currentPage < pages.size - 1) {
                            colorScheme.primaryButtonText.copy(alpha = 0f)
                        } else {
                            colorScheme.primaryButtonText
                        },
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Button(
                    onClick = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    },
                    enabled = pagerState.currentPage < pages.size - 1,
                    modifier = Modifier.weight(1f),
                    shape = RectangleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorScheme.primaryButtonBackground
                    )
                ) {
                    Text(
                        text = ">",
                        color = colorScheme.primaryButtonText,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
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