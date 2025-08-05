package hu.adamfejes.zenmotes.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.geometry.Offset

actual fun DrawScope.drawTextLines(
    lines: List<String>,
    color: Color,
    textSize: Float,
    startOffset: Offset,
    lineHeight: Float
) {
    // For iOS, we'll use a simple text rendering approach
    // In a real app, you might want to use CoreText or skia-based text rendering
    // For now, let's create a simpler version that just displays the text as rectangles
    // This is a placeholder - in production you'd want proper text rendering
    
    var yPos = startOffset.y
    for (line in lines) {
        // Draw a colored line as a placeholder for text
        drawLine(
            color = color,
            start = Offset(startOffset.x, yPos),
            end = Offset(startOffset.x + line.length * textSize * 0.6f, yPos),
            strokeWidth = 2f
        )
        yPos += lineHeight
    }
}