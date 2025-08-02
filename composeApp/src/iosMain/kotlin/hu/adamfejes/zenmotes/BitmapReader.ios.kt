package hu.adamfejes.zenmotes

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asSkiaBitmap
import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.ColorAlphaType
import org.jetbrains.skia.ColorType
import org.jetbrains.skia.ImageInfo

actual object BitmapReader {
    
    /**
     * Converts an ImageBitmap to grayscale BitmapData on iOS.
     * Uses Skia bitmap operations for pixel access.
     */
    actual fun readBitmap(imageBitmap: ImageBitmap): BitmapData {
        try {
            // Convert to Skia Bitmap for pixel access
            val skiaBitmap = imageBitmap.asSkiaBitmap()
            
            val width = skiaBitmap.width
            val height = skiaBitmap.height
            val pixels = Array(height) { FloatArray(width) }
            
            // Read pixels into buffer
            val pixelArray = IntArray(width * height)
            val imageInfo = ImageInfo(
                width = width,
                height = height,
                colorType = ColorType.RGBA_8888,
                alphaType = ColorAlphaType.UNPREMUL
            )
            
            if (skiaBitmap.readPixels(imageInfo, pixelArray, width * 4)) {
                // Process each pixel
                for (y in 0 until height) {
                    for (x in 0 until width) {
                        val pixelIndex = y * width + x
                        val pixel = pixelArray[pixelIndex]
                        
                        // Extract RGB components (RGBA format)
                        val red = (pixel shr 0) and 0xFF
                        val green = (pixel shr 8) and 0xFF
                        val blue = (pixel shr 16) and 0xFF
                        // Alpha is at (pixel shr 24) and 0xFF but we don't need it
                        
                        // Convert to grayscale using luminance formula
                        val luminance = (0.299 * red + 0.587 * green + 0.114 * blue) / 255.0
                        
                        // Convert to darkness (0.0 = white, 1.0 = black)
                        pixels[y][x] = (1.0 - luminance).toFloat()
                    }
                }
            } else {
                throw BitmapReadException("Failed to read pixels from Skia bitmap")
            }
            
            return BitmapData(width, height, pixels)
        } catch (e: Exception) {
            throw BitmapReadException("Failed to read ImageBitmap on iOS", e)
        }
    }
}