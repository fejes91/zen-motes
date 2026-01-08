package hu.adamfejes.zenmotes.service

import android.content.Context
import android.media.MediaPlayer
import hu.adamfejes.zenmotes.utils.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import org.jetbrains.compose.resources.ExperimentalResourceApi

@OptIn(ExperimentalResourceApi::class)
class AndroidMusicManager(private val context: Context) : MusicManager {
    private var scope: CoroutineScope? = null
    private var globalVolume: Float = 1f
    private val mediaPlayer: MediaPlayer = MediaPlayer()

    override fun init() {
        Logger.d("AndroidMusicManager", "Initializing MusicManager")
        scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    }

    private fun applyVolume() {
        Logger.d("AndroidMusicManager", "Applying global volume: $globalVolume")
        try {
            mediaPlayer.setVolume(globalVolume, globalVolume)
        } catch (e: IllegalStateException) {
            Logger.e("AndroidMusicManager", "Error applying volume: ${e.message}")
        }
    }

    override fun setMusicEnabled(enabled: Boolean) {
        Logger.d("AndroidMusicManager", "Setting music enabled to $enabled")
        globalVolume = if (enabled) 1f else 0f
        applyVolume()
    }

    override fun play(track: MusicTrack, loop: Boolean) {
        Logger.d("AndroidMusicManager", "Playing music: ${track.fileName}, loop: $loop")

        try {
            mediaPlayer.reset()

            val assetFileDescriptor =
                context.assets.openFd("composeResources/zenmotescmp.composeapp.generated.resources/files/${track.fileName}")

            mediaPlayer.setDataSource(
                assetFileDescriptor.fileDescriptor,
                assetFileDescriptor.startOffset,
                assetFileDescriptor.length
            )
            assetFileDescriptor.close()

            mediaPlayer.prepare()
            mediaPlayer.isLooping = loop
            applyVolume()
            mediaPlayer.start()

            Logger.d("AndroidMusicManager", "Started playing music: ${track.fileName}")
        } catch (e: Exception) {
            Logger.e("AndroidMusicManager", "Error playing music: ${track.fileName} - ${e.message}")
            e.printStackTrace()
        }
    }

    override fun stop(track: MusicTrack) {
        Logger.d("AndroidMusicManager", "Stopping music: ${track.fileName}")

        try {
            if (mediaPlayer.isPlaying) {
                mediaPlayer.pause()
                mediaPlayer.seekTo(0)
            }
        } catch (e: IllegalStateException) {
            Logger.e("AndroidMusicManager", "Error stopping music: ${e.message}")
        }
    }

    override fun dispose() {
        Logger.d("AndroidMusicManager", "Disposing MusicManager")
        try {
            if (mediaPlayer.isPlaying) {
                mediaPlayer.stop()
            }
            mediaPlayer.release()
        } catch (e: IllegalStateException) {
            Logger.e("AndroidMusicManager", "Error disposing MediaPlayer: ${e.message}")
        }
        scope?.cancel()
        scope = null
    }
}
