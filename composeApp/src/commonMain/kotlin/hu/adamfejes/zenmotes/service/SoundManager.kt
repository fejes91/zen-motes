package hu.adamfejes.zenmotes.service

enum class SoundSample(val isGameScene: Boolean, val fileName: String, val durationMillis: Long, val volume: Float = 1f) {
    SAND_BLAST(isGameScene = true, "sand-blast.m4a", 1000),
    SAND_BLAST_SHORT(isGameScene = true, "sand-blast-short.m4a", 700),
    GAIN1(isGameScene = true, "gain1.m4a", 150),
    GAIN2(isGameScene = true, "gain2.m4a", 150),
    LOSS1(isGameScene = true, "loss1.m4a", 150, volume = 0.15f),
    LOSS2(isGameScene = true, "loss2.m4a", 150, volume = 0.15f),
    BONUS1(isGameScene = true, "bonus1.m4a", 150),
    BONUS2(isGameScene = true, "bonus2.m4a", 150),
    CLOCK_FAST(isGameScene = true, "clock-ticking-fast.mp3", 5000),
    CLOCK_SLOW(isGameScene = true, "clock-ticking-slow.m4a", 7000, volume = 0.5f),
    GAME_OVER(isGameScene = false, "game-over.mp3", 3000)
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