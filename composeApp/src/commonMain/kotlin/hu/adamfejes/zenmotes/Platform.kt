package hu.adamfejes.zenmotes

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform