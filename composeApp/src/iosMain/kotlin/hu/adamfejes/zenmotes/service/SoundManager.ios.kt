package hu.adamfejes.zenmotes.service

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.ExperimentalResourceApi
import platform.AVFoundation.AVAudioPlayer
import platform.Foundation.NSBundle
import platform.Foundation.NSData
import platform.Foundation.dataWithContentsOfFile
import zenmotescmp.composeapp.generated.resources.Res

@OptIn(ExperimentalResourceApi::class)
class IOSSoundManager : SoundManager {
    private val audioPlayers = mutableMapOf<SoundSample, AVAudioPlayer>()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    
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

    override fun playAsync(sample: SoundSample, loop: Boolean) {
        scope.launch(Dispatchers.Main) {
            audioPlayers[sample]?.let { player ->
                player.stop()
                player.currentTime = 0.0
                player.numberOfLoops = if (loop) -1 else 0
                player.play()
            }
        }
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
}