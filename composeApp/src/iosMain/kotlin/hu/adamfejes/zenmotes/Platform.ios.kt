package hu.adamfejes.zenmotes

import kotlinx.cinterop.ExperimentalForeignApi
import platform.CoreGraphics.CGRectGetWidth
import platform.UIKit.UIDevice
import platform.UIKit.UIScreen
import kotlin.math.roundToInt

class IOSPlatform : Platform {
    override val name: String =
        UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
}

actual fun getPlatform(): Platform = IOSPlatform()

@OptIn(ExperimentalForeignApi::class)
actual fun getScreenWidth(): Int {
    val bounds = UIScreen.mainScreen.bounds
    return CGRectGetWidth(bounds).roundToInt()
}