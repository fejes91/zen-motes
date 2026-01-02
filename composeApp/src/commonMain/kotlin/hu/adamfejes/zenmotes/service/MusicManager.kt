package hu.adamfejes.zenmotes.service

enum class MusicTrack(val fileName: String, val volume: Float = 1f) {
    MAIN_MENU("main-menu-music.m4a", volume = 1f)
}

interface MusicManager {
    fun init()
    fun setMusicEnabled(enabled: Boolean)
    fun play(track: MusicTrack, loop: Boolean = false)
    fun stop(track: MusicTrack)
    fun dispose()
}
