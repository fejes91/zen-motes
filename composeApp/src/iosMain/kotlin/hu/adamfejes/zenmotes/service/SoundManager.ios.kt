package hu.adamfejes.zenmotes.service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.ExperimentalResourceApi
import platform.AVFoundation.AVAudioPlayer
import platform.Foundation.NSBundle
import platform.Foundation.NSData
import platform.Foundation.dataWithContentsOfFile
import zenmotescmp.composeapp.generated.resources.Res

@OptIn(ExperimentalResourceApi::class)
class IOSSoundManager : SoundManager {
    private val audioPlayers = mutableMapOf<SoundSample, AVAudioPlayer>()
    
    override suspend fun init() = withContext(Dispatchers.Default) {
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
    
    override suspend fun play(sample: SoundSample, loop: Boolean) = withContext(Dispatchers.Main) {
        audioPlayers[sample]?.let { player ->
            player.stop()
            player.currentTime = 0.0
            player.numberOfLoops = if (loop) -1 else 0
            player.play()
        }
    }
    
    override suspend fun stop(sample: SoundSample) = withContext(Dispatchers.Main) {
        audioPlayers[sample]?.stop()
    }
    
    override suspend fun stopAll() = withContext(Dispatchers.Main) {
        audioPlayers.values.forEach { player ->
            player.stop()
        }
    }
}