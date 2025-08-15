package hu.adamfejes.zenmotes.service

enum class SoundSample(val fileName: String) {
    SAND_BEGIN("sand-begin.m4a"),
    SAND_MIDDLE("sand-middle.m4a")
}

interface SoundManager {
    suspend fun init()
    suspend fun play(sample: SoundSample, loop: Boolean = false)
    suspend fun stop(sample: SoundSample)
    suspend fun stopAll()
}