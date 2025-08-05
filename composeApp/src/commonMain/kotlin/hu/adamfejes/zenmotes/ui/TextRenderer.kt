package hu.adamfejes.zenmotes.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.geometry.Offset

expect fun DrawScope.drawTextLines(
    lines: List<String>,
    color: Color,
    textSize: Float,
    startOffset: Offset,
    lineHeight: Float
)