package hu.adamfejes.zenmotes.service

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.ExperimentalResourceApi
import platform.AVFAudio.AVAudioPlayer
import platform.Foundation.NSBundle
import platform.Foundation.NSData
import platform.Foundation.dataWithContentsOfFile

@OptIn(ExperimentalResourceApi::class)
class IOSSoundManager : SoundManager {
    private val audioPlayers = mutableMapOf<SoundSample, AVAudioPlayer>()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var paused: Boolean = false
    private var soundEnabled: Boolean = true

    @OptIn(ExperimentalForeignApi::class)
    override fun init() {
        scope.launch {
            SoundSample.entries.forEach { sample ->
                try {
                    val bundle = NSBundle.mainBundle
                    val path = bundle.pathForResource(
                        name = sample.fileName.substringBeforeLast("."),
                        ofType = sample.fileName.substringAfterLast(".")
                    )

                    path?.let { filePath ->
                        val data = NSData.dataWithContentsOfFile(filePath)
                        data?.let { audioData ->
                            val player = AVAudioPlayer(audioData, null)
                            player.prepareToPlay()
                            audioPlayers[sample] = player
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun setVolume(volume: Float) {
        scope.launch(Dispatchers.Main) {
            audioPlayers.values.forEach { player ->
                player.volume = volume
            }
        }
    }

    override fun setSoundEnabled(enabled: Boolean) {
        soundEnabled = enabled
    }

    override fun playAsync(sample: SoundSample) {
        scope.launch(Dispatchers.Main) {
            play(sample)
        }
    }

    override suspend fun play(sample: SoundSample) {
        if (paused) return

        if (!soundEnabled) return

        audioPlayers[sample]?.let { player ->
            player.stop()
            player.currentTime = 0.0
            player.numberOfLoops = 0
            player.play()
        }
        // iOS audio implementation not available - just delay for the duration
        delay(sample.durationMillis)
    }

    override fun stop(sample: SoundSample) {
        scope.launch(Dispatchers.Main) {
            audioPlayers[sample]?.stop()
        }
    }

    override fun stopAll() {
        scope.launch(Dispatchers.Main) {
            audioPlayers.values.forEach { player ->
                player.stop()
            }
        }
    }

    override fun dispose() {
        scope.launch {
            audioPlayers.clear()
        }
        scope.cancel()
    }

    override fun onPause() {
        paused = true
        stopAll()
    }

    override fun onResume() {
        paused = false
    }
}