package hu.adamfejes.zenmotes.service

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import hu.adamfejes.zenmotes.utils.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
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
    private var paused: Boolean = false
    private var gameScenePaused = false
    private var soundEnabled: Boolean = true
    
    override fun init() {
        Logger.d("AndroidSoundManager", "Initializing SoundManager")
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

    override fun setSoundEnabled(enabled: Boolean) {
        Logger.d("AndroidSoundManager", "Setting sound enabled to $enabled")
        soundEnabled = enabled
        if (!enabled) {
            stopAll()
        }
    }

    override fun playAsync(sample: SoundSample, loop: Boolean) {
        scope.launch(Dispatchers.Default) {
            play(sample, loop)
        }
    }

    override suspend fun play(sample: SoundSample, loop: Boolean) {
        Logger.d("AndroidSoundManager", "Playing sound: ${sample.fileName}")
        if(paused) {
            Logger.d("AndroidSoundManager", "SoundManager is paused, not playing sound: ${sample.fileName}")
            return
        }

        if(gameScenePaused && sample.isGameScene) {
            Logger.d("AndroidSoundManager", "Game scene is paused, not playing game scene sound: ${sample.fileName}")
            return
        }

        if(!soundEnabled) {
            Logger.d("AndroidSoundManager", "Sound is disabled, not playing sound: ${sample.fileName}")
            return
        }

        soundIds[sample]?.let { soundId ->
            val streamId = soundPool.play(
                soundId,
                1.0f, // left volume
                1.0f, // right volume
                1, // priority
                if(loop) -1 else 0, // loop
                1.0f // rate
            )

            if (streamId != 0) {
                streamIds[sample] = streamId
            }

            delay(sample.durationMillis)

            Logger.d("AndroidSoundManager", "Finished playing sound: ${sample.fileName}")
        }
    }

    override fun stop(sample: SoundSample) {
        Logger.d("AndroidSoundManager", "Stopping sound: ${sample.fileName}")
        scope.launch(Dispatchers.Default) {
            streamIds[sample]?.let { streamId ->
                soundPool.stop(streamId)
            }
        }
    }
    
    override fun stopAll() {
        Logger.d("AndroidSoundManager", "Stopping all sounds")
        scope.launch(Dispatchers.Default) {
            streamIds.values.forEach { streamId ->
                soundPool.stop(streamId)
            }
        }
    }
    
    override fun dispose() {
        Logger.d("AndroidSoundManager", "Disposing SoundManager")
        scope.launch {
            soundPool.release()
        }
        scope.cancel()
    }

    override fun onPause() {
        stopAll()
        paused = true
    }

    override fun onResume() {
        paused = false
        gameScenePaused = false
    }

    override fun stopGameSceneSounds() {
        SoundSample.entries.onEach {
            stop(it)
        }
        gameScenePaused = true
    }
}