package hu.adamfejes.zenmotes.utils

import platform.Foundation.NSDate
import platform.Foundation.timeIntervalSince1970
import startMark

actual object TimeUtils {
    actual fun currentTimeMillis(): Long {
        return (NSDate().timeIntervalSince1970 * 1000).toLong()
    }

    actual fun nanoTime(): Long {
        return startMark.elapsedNow().inWholeNanoseconds
    }
}