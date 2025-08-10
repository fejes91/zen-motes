package hu.adamfejes.zenmotes.utils

import android.util.Log
import hu.adamfejes.zenmotes.BuildConfig

actual object Logger {
    actual val isDebugBuild: Boolean = false//BuildConfig.DEBUG

    actual fun d(tag: String, message: String) {
        if (isDebugBuild) {
            Log.d(tag, message)
        }
    }

    actual fun i(tag: String, message: String) {
        if (isDebugBuild) {
            Log.i(tag, message)
        }
    }

    actual fun w(tag: String, message: String) {
        if (isDebugBuild) {
            Log.w(tag, message)
        }
    }

    actual fun e(tag: String, message: String) {
        if (isDebugBuild) {
            Log.e(tag, message)
        }
    }
}