package hu.adamfejes.zenmotes.service

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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
    
    override suspend fun init() = withContext(Dispatchers.IO) {
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
    
    override suspend fun play(sample: SoundSample, loop: Boolean): Unit = withContext(Dispatchers.Main) {
        println("Playing sound: ${sample.fileName}, loop: $loop")
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
    
    override suspend fun stop(sample: SoundSample): Unit = withContext(Dispatchers.Main) {
        streamIds[sample]?.let { streamId ->
            soundPool.stop(streamId)
            streamIds.remove(sample)
        }
    }
    
    override suspend fun stopAll() = withContext(Dispatchers.Main) {
        streamIds.values.forEach { streamId ->
            soundPool.stop(streamId)
        }
        streamIds.clear()
    }
}