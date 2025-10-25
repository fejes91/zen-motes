package hu.adamfejes.zenmotes.utils

/**
 * Efficiently calculates average FPS without storing individual samples.
 * Optimized for high-frequency reporting (e.g., 60+ Hz).
 */
class FpsAverageCalculator {
    private var sum: Long = 0L
    private var count: Long = 0L

    fun record(fps: Int) {
        sum += fps
        count++
    }

    fun average(): Double = if (count == 0L) 0.0 else sum.toDouble() / count

    fun sampleCount(): Long = count

    fun reset() {
        sum = 0L
        count = 0
    }
}
