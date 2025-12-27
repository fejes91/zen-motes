package hu.adamfejes.zenmotes.utils

fun Long.formatTime(): String {
    val totalSeconds = this / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "${minutes}:${seconds.toString().padStart(2, '0')}"
}

fun Int.formatScore(): String {
    return this.toString().reversed().chunked(3).joinToString(".").reversed()
}