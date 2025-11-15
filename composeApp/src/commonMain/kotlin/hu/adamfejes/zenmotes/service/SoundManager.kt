package hu.adamfejes.zenmotes.service

enum class SoundSample(val isGameScene: Boolean, val fileName: String, val durationMillis: Long) {
    SAND_BLAST(isGameScene = true,"sand-blast.m4a", 1000),
    SAND_BLAST_SHORT(isGameScene = true,"sand-blast-short.m4a", 700),
    POSITIVE(isGameScene = true,"positive.m4a", 300),
    NEGATIVE(isGameScene = true,"negative.m4a", 200),
    CLOCK_FAST(isGameScene = true,"clock-ticking-fast.mp3", 5000),
    CLOCK_SLOW(isGameScene = true,"clock-ticking-slow.m4a", 7000),
    GAME_OVER(isGameScene = false,"game-over.mp3", 1000)
}

interface SoundManager {
    fun init()
    fun setVolume(volume: Float)
    fun setSoundEnabled(enabled: Boolean)
    fun playAsync(sample: SoundSample, loop: Boolean = false)
    suspend fun play(sample: SoundSample, loop: Boolean = false)
    fun stop(sample: SoundSample)
    fun stopAll()
    fun dispose()
    fun onPause()

    fun onResume()
    fun stopGameSceneSounds()
}