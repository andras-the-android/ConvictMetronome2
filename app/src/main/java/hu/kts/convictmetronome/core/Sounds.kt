package hu.kts.convictmetronome.core

import android.media.AudioTrack
import android.media.ToneGenerator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject


class Sounds @Inject constructor(
    private val coroutineScope: CoroutineScope,
    private val toneGenerator: ToneGenerator,
    private val audioTrack: AudioTrack,
    private val soundArrayGenerator: SoundArrayGenerator
) {

    private var sampleArrayUp = ShortArray(0)
    private var sampleArrayDown = ShortArray(0)
    private var soundArrayGenerationJob: Job? = null

    init {
        generateUpDownSounds()
    }

    fun makeUpSound() {
        playSound(true)
    }

    fun makeDownSound() {
        playSound(false)
    }

    fun stop() {
        tryStop()
    }

    fun beep() {
        toneGenerator.startTone(ToneGenerator.TONE_CDMA_ABBR_ALERT, 150)
        toneGenerator.startTone(ToneGenerator.TONE_CDMA_ABBR_ALERT, 150)
        toneGenerator.startTone(ToneGenerator.TONE_CDMA_ABBR_ALERT, 150)
    }

    private fun generateUpDownSounds() {
        soundArrayGenerationJob = coroutineScope.launch{
            // TODO get values from db
            sampleArrayUp = soundArrayGenerator.generateSoundArray(2000, true)
            sampleArrayDown = soundArrayGenerator.generateSoundArray(2000,false)
        }
    }

    private fun playSound(up: Boolean) {
        tryStop()
        coroutineScope.launch {
            audioTrack.setVolume(AudioTrack.getMaxVolume())
            audioTrack.play()
            soundArrayGenerationJob?.join()
            audioTrack.write(if (up) sampleArrayUp else sampleArrayDown, 0, if (up) sampleArrayUp.size else sampleArrayDown.size)

            tryStop()
        }
    }

    private fun tryStop() {
        audioTrack.run {
            if (playState != AudioTrack.PLAYSTATE_STOPPED) {
                stop()
                release()
            }
        }
    }



}
