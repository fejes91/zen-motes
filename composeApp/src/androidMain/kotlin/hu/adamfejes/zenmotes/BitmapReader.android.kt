package hu.adamfejes.zenmotes

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap

actual object BitmapReader {
    
    /**
     * Converts an ImageBitmap to grayscale BitmapData on Android.
     * Uses the Android Bitmap API to read pixel data.
     */
    actual fun readBitmap(imageBitmap: ImageBitmap): BitmapData {
        try {
            // Convert to Android Bitmap for pixel access
            val androidBitmap = imageBitmap.asAndroidBitmap()
            
            val width = androidBitmap.width
            val height = androidBitmap.height
            val pixels = Array(height) { FloatArray(width) }
            
            // Process each pixel
            for (y in 0 until height) {
                for (x in 0 until width) {
                    val pixel = androidBitmap.getPixel(x, y)
                    
                    // Extract RGB components
                    val red = (pixel shr 16) and 0xFF
                    val green = (pixel shr 8) and 0xFF
                    val blue = pixel and 0xFF
                    
                    // Convert to grayscale using luminance formula
                    val luminance = (0.299 * red + 0.587 * green + 0.114 * blue) / 255.0
                    
                    // Convert to darkness (0.0 = white, 1.0 = black)
                    pixels[y][x] = (1.0 - luminance).toFloat()
                }
            }
            
            return BitmapData(width, height, pixels)
        } catch (e: Exception) {
            throw BitmapReadException("Failed to read ImageBitmap on Android", e)
        }
    }
}