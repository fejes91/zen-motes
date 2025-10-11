package hu.adamfejes.zenmotes

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State

interface Platform {
    val name: String
    val appVersion: String
}

expect fun getPlatform(): Platform

expect fun getScreenWidth(): Int

@Composable
expect fun rememberIsLandscape(): State<Boolean>