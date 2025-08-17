package hu.adamfejes.zenmotes.service

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import hu.adamfejes.zenmotes.utils.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.ExperimentalResourceApi
import java.util.concurrent.ConcurrentHashMap

@OptIn(ExperimentalResourceApi::class)
class AndroidSoundManager(private val context: Context) : SoundManager {
    private val soundPool: SoundPool = SoundPool.Builder()
        .setMaxStreams(10)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .build()
    
    private val soundIds = ConcurrentHashMap<SoundSample, Int>()
    private val streamIds = ConcurrentHashMap<SoundSample, Int>()
    
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    override fun init() {
        scope.launch {
            SoundSample.entries.forEach { sample ->
                try {
                    val assetFileDescriptor = context.assets.openFd("composeResources/zenmotescmp.composeapp.generated.resources/files/${sample.fileName}")
                    val soundId = soundPool.load(assetFileDescriptor, 1)
                    soundIds[sample] = soundId
                    assetFileDescriptor.close()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun setVolume(volume: Float) {
        Logger.d("AndroidSoundManager", "Setting volume to $volume")
        soundIds.values.forEach { soundId ->
            soundPool.setVolume(soundId, volume, volume)
        }
    }

    override fun play(sample: SoundSample, loop: Boolean) {
        scope.launch(Dispatchers.Main) {
            Logger.d("AndroidSoundManager", "Playing sound: ${sample.fileName}, loop: $loop")
            soundIds[sample]?.let { soundId ->
                // Stop any existing stream for this sample
                streamIds[sample]?.let { streamId ->
                    soundPool.stop(streamId)
                }

                val streamId = soundPool.play(
                    soundId,
                    1.0f, // left volume
                    1.0f, // right volume
                    1, // priority
                    if (loop) -1 else 0, // loop (-1 = infinite, 0 = no loop)
                    1.0f // rate
                )
                
                if (streamId != 0) {
                    streamIds[sample] = streamId
                }
            }
        }
    }
    
    override fun stop(sample: SoundSample) {
        Logger.d("AndroidSoundManager", "Stopping sound: ${sample.fileName}")
        scope.launch(Dispatchers.Main) {
            streamIds[sample]?.let { streamId ->
                soundPool.stop(streamId)
                streamIds.remove(sample)
            }
        }
    }
    
    override fun stopAll() {
        Logger.d("AndroidSoundManager", "Stopping all sounds")
        scope.launch(Dispatchers.Main) {
            streamIds.values.forEach { streamId ->
                soundPool.stop(streamId)
            }
            streamIds.clear()
        }
    }
    
    override fun dispose() {
        Logger.d("AndroidSoundManager", "Disposing SoundManager")
        scope.launch {
            soundPool.release()
        }
        scope.cancel()
    }
}