package hu.adamfejes.zenmotes.utils

actual object TimeUtils {
    actual fun currentTimeMillis(): Long = System.currentTimeMillis()
    actual fun nanoTime(): Long = System.nanoTime()
}