package hu.adamfejes.zenmotes.service

enum class SoundSample(val fileName: String, val durationMillis: Long) {
    SAND_BLAST("sand-blast.m4a", 1000),
    SAND_BLAST_SHORT("sand-blast-short.m4a", 700),
    POSITIVE("positive.m4a", 300),
    NEGATIVE("negative.m4a", 200)
}

interface SoundManager {
    fun init()

    fun setVolume(volume: Float)
    fun playAsync(sample: SoundSample)

    suspend fun play(sample: SoundSample)
    fun stop(sample: SoundSample)
    fun stopAll()
    fun dispose()
}