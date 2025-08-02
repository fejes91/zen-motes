package hu.adamfejes.zenmotes

import androidx.compose.ui.graphics.ImageBitmap

/**
 * Test utility class for bitmap reading functionality
 */
object BitmapReaderTest {
    
    /**
     * Tests reading the sample bitmap and prints basic information
     */
    fun testSampleBitmap(imageBitmap: ImageBitmap): String {
        return try {
            val bitmapData = BitmapReader.readBitmap(imageBitmap)
            
            val report = buildString {
                appendLine("✅ Bitmap loaded successfully!")
                appendLine("📏 Dimensions: ${bitmapData.width} x ${bitmapData.height}")
                appendLine("🎨 Sample pixel values:")
                
                // Sample a few pixels from different areas
                val samplePoints = listOf(
                    Triple(0, 0, "Top-left"),
                    Triple(bitmapData.width - 1, 0, "Top-right"),
                    Triple(bitmapData.width / 2, bitmapData.height / 2, "Center"),
                    Triple(0, bitmapData.height - 1, "Bottom-left"),
                    Triple(bitmapData.width - 1, bitmapData.height - 1, "Bottom-right")
                )
                
                for ((x, y, label) in samplePoints) {
                    val darkness = bitmapData.pixels[y][x]
                    val percentage = (darkness * 100).toInt()
                    appendLine("   $label ($x, $y): $darkness ($percentage% dark)")
                }
                
                // Calculate average darkness
                var totalDarkness = 0.0
                var pixelCount = 0
                for (row in bitmapData.pixels) {
                    for (pixel in row) {
                        totalDarkness += pixel
                        pixelCount++
                    }
                }
                val avgDarkness = totalDarkness / pixelCount
                val avgPercentage = (avgDarkness * 100).toInt()
                appendLine("📊 Average darkness: $avgDarkness ($avgPercentage% dark)")
            }
            
            Logger.d("BitmapReader", report)
            report
        } catch (e: BitmapReadException) {
            val errorMsg = "❌ Failed to read bitmap: ${e.message}"
            Logger.e("BitmapReader", errorMsg)
            errorMsg
        }
    }
}