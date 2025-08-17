package hu.adamfejes.zenmotes.service

enum class SoundSample(val fileName: String) {
    SAND_BEGIN("sand-begin.m4a"),
    SAND_MIDDLE("sand-middle.m4a")
}

interface SoundManager {
    fun init()

    fun setVolume(volume: Float)
    fun play(sample: SoundSample, loop: Boolean = false)
    fun stop(sample: SoundSample)
    fun stopAll()
    fun dispose()
}