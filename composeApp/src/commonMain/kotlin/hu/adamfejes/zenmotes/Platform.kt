package hu.adamfejes.zenmotes

interface Platform {
    val name: String
    val appVersion: String
}

expect fun getPlatform(): Platform

expect fun getScreenWidth(): Int