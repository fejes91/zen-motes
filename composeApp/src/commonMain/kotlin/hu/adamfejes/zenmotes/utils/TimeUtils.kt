package hu.adamfejes.zenmotes.utils

expect object TimeUtils {
    fun currentTimeMillis(): Long
    fun nanoTime(): Long
}