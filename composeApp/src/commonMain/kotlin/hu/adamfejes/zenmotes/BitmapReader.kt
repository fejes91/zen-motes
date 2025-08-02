package hu.adamfejes.zenmotes

import androidx.compose.ui.graphics.ImageBitmap

/**
 * Data class representing a 2D array of grayscale pixel values.
 * @param width Width of the bitmap in pixels
 * @param height Height of the bitmap in pixels 
 * @param pixels 2D array where [y][x] represents the darkness value (0.0 = white, 1.0 = black)
 */
data class BitmapData(
    val width: Int,
    val height: Int,
    val pixels: Array<FloatArray>
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as BitmapData

        if (width != other.width) return false
        if (height != other.height) return false
        if (!pixels.contentDeepEquals(other.pixels)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = width
        result = 31 * result + height
        result = 31 * result + pixels.contentDeepHashCode()
        return result
    }
}

/**
 * Exception thrown when bitmap reading fails
 */
class BitmapReadException(message: String, cause: Throwable? = null) : Exception(message, cause)

/**
 * Cross-platform bitmap reader that converts ImageBitmap to grayscale data
 */
expect object BitmapReader {
    /**
     * Converts an ImageBitmap to grayscale BitmapData.
     * 
     * @param imageBitmap The ImageBitmap to convert (from imageResource())
     * @return BitmapData containing width, height, and 2D array of darkness values (0.0-1.0)
     * @throws BitmapReadException if the bitmap cannot be processed
     */
    fun readBitmap(imageBitmap: ImageBitmap): BitmapData
}