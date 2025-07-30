package hu.adamfejes.zenmotes

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.geometry.Offset

actual fun DrawScope.drawTextLines(
    lines: List<String>,
    color: Color,
    textSize: Float,
    startOffset: Offset,
    lineHeight: Float
) {
    val canvas = drawContext.canvas.nativeCanvas
    val paint = android.graphics.Paint().apply {
        this.color = color.toArgb()
        this.textSize = textSize
        isAntiAlias = true
        typeface = android.graphics.Typeface.MONOSPACE
    }

    var yPos = startOffset.y
    for (line in lines) {
        canvas.drawText(line, startOffset.x, yPos, paint)
        yPos += lineHeight
    }
}