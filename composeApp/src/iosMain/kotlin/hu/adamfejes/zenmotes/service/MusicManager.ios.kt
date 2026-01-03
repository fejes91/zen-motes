package hu.adamfejes.zenmotes.service

import hu.adamfejes.zenmotes.utils.Logger
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.ExperimentalResourceApi
import platform.AVFAudio.AVAudioPlayer
import platform.Foundation.NSBundle
import platform.Foundation.NSData
import platform.Foundation.dataWithContentsOfFile

@OptIn(ExperimentalResourceApi::class, ExperimentalForeignApi::class)
class IOSMusicManager : MusicManager {
    private var audioPlayer: AVAudioPlayer? = null
    private var scope: CoroutineScope? = null
    private var globalVolume: Float = 1f
    private var musicEnabled: Boolean = true

    override fun init() {
        Logger.d("IOSMusicManager", "Initializing MusicManager")
        scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    }

    override fun setMusicEnabled(enabled: Boolean) {
        Logger.d("IOSMusicManager", "Setting music enabled to $enabled")
        musicEnabled = enabled
        if (!enabled) {
            audioPlayer?.stop()
        } else {
            applyVolume()
        }
    }

    private fun applyVolume() {
        audioPlayer?.volume = globalVolume
    }

    override fun play(track: MusicTrack, loop: Boolean) {
        Logger.d("IOSMusicManager", "Playing music: ${track.fileName}, loop: $loop")

        if (!musicEnabled) {
            Logger.d("IOSMusicManager", "Music is disabled, not playing music: ${track.fileName}")
            return
        }

        scope?.launch {
            try {
                val bundle = NSBundle.mainBundle
                val path = bundle.pathForResource(
                    name = track.fileName.substringBeforeLast("."),
                    ofType = track.fileName.substringAfterLast(".")
                )

                path?.let { filePath ->
                    val data = NSData.dataWithContentsOfFile(filePath)
                    data?.let { audioData ->
                        // Stop and release existing player
                        audioPlayer?.stop()
                        audioPlayer = null

                        // Create new player
                        val player = AVAudioPlayer(audioData, null)
                        player.prepareToPlay()
                        player.numberOfLoops = if (loop) -1 else 0
                        player.volume = track.volume * globalVolume
                        player.play()

                        audioPlayer = player
                        Logger.d("IOSMusicManager", "Started playing music: ${track.fileName}")
                    } ?: run {
                        Logger.e("IOSMusicManager", "Failed to load audio data for: ${track.fileName}")
                    }
                } ?: run {
                    Logger.e("IOSMusicManager", "Failed to find resource path for: ${track.fileName}")
                }
            } catch (e: Exception) {
                Logger.e("IOSMusicManager", "Error playing music: ${track.fileName} - ${e.message}")
                e.printStackTrace()
            }
        }
    }

    override fun stop(track: MusicTrack) {
        Logger.d("IOSMusicManager", "Stopping music: ${track.fileName}")
        scope?.launch {
            audioPlayer?.stop()
            audioPlayer?.currentTime = 0.0
        }
    }

    override fun dispose() {
        Logger.d("IOSMusicManager", "Disposing MusicManager")
        audioPlayer?.stop()
        audioPlayer = null
        scope?.cancel()
        scope = null
    }
}
