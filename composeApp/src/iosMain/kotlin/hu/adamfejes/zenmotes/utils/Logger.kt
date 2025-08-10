package hu.adamfejes.zenmotes.utils

import kotlin.experimental.ExperimentalNativeApi
import kotlin.native.Platform
import platform.Foundation.NSLog

actual object Logger {
    @OptIn(ExperimentalNativeApi::class)
    actual val isDebugBuild: Boolean = Platform.isDebugBinary

    actual fun d(tag: String, message: String) {
        if (isDebugBuild) {
            NSLog("[$tag] $message")
        }
    }

    actual fun i(tag: String, message: String) {
        if (isDebugBuild) {
            NSLog("[$tag] $message")
        }
    }

    actual fun w(tag: String, message: String) {
        if (isDebugBuild) {
            NSLog("[$tag] WARNING: $message")
        }
    }

    actual fun e(tag: String, message: String) {
        if (isDebugBuild) {
            NSLog("[$tag] ERROR: $message")
        }
    }
}