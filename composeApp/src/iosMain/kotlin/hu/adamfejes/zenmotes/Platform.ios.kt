package hu.adamfejes.zenmotes

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.LocalWindowInfo
import kotlinx.cinterop.ExperimentalForeignApi
import platform.CoreGraphics.CGRectGetHeight
import platform.CoreGraphics.CGRectGetWidth
import platform.Foundation.NSBundle
import platform.Foundation.NSURL
import platform.UIKit.UIApplication
import platform.UIKit.UIDevice
import platform.UIKit.UIScreen
import kotlin.math.roundToInt

class IOSPlatform : Platform {
    override val name: String =
        UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion

    override val appVersion: String =
        NSBundle.mainBundle.objectForInfoDictionaryKey("CFBundleShortVersionString") as? String ?: "0.0.1"
}

actual fun getPlatform(): Platform = IOSPlatform()

@OptIn(ExperimentalForeignApi::class)
actual fun getScreenWidth(): Int {
    val bounds = UIScreen.mainScreen.bounds
    return CGRectGetWidth(bounds).roundToInt()
}

@OptIn(ExperimentalComposeUiApi::class, ExperimentalForeignApi::class)
@Composable
actual fun rememberIsLandscape(): State<Boolean> {
    val windowInfo = LocalWindowInfo.current
    val bounds = UIScreen.mainScreen.bounds
    val width = CGRectGetWidth(bounds)
    val height = CGRectGetHeight(bounds)

    return remember(windowInfo.containerSize, width, height) {
        derivedStateOf {
            width > height
        }
    }
}

@Composable
actual fun BackHandler(enabled: Boolean, onBack: () -> Unit) {
    // iOS doesn't have a system back button like Android
    // No-op implementation
}

@Composable
actual fun rememberOpenUrl(): (String) -> Unit {
    return remember {
        { url: String ->
            NSURL.URLWithString(url)?.let { nsUrl ->
                UIApplication.sharedApplication.openURL(nsUrl)
            }
        }
    }
}