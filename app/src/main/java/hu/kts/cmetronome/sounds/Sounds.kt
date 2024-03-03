package hu.kts.cmetronome.sounds

import android.media.AudioTrack
import android.media.ToneGenerator
import android.speech.tts.TextToSpeech
import androidx.core.os.bundleOf
import hu.kts.cmetronome.persistency.Preferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.pow


class Sounds @Inject constructor(
    private val coroutineScope: CoroutineScope,
    private val toneGenerator: ToneGenerator,
    private val audioTrack: AudioTrack,
    private val soundArrayGenerator: SoundArrayGenerator,
    private val preferences: Preferences,
    private val textToSpeech: TextToSpeech,
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

    fun announceRepCounter(reps: Int) {
        val params = bundleOf(
            TextToSpeech.Engine.KEY_PARAM_VOLUME to preferences.speechVolumeStep * speechVolumeStepMultiplier
        )
        textToSpeech.speak(reps.toString(), TextToSpeech.QUEUE_FLUSH, params, null)
    }

    fun stop() {
        tryStop()
    }

    fun beep() {
        if (preferences.upDownVolumeStep > 0) {
            toneGenerator.startTone(ToneGenerator.TONE_CDMA_ABBR_ALERT, 150)
            toneGenerator.startTone(ToneGenerator.TONE_CDMA_ABBR_ALERT, 150)
            toneGenerator.startTone(ToneGenerator.TONE_CDMA_ABBR_ALERT, 150)
        }
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
            audioTrack.run {
                setVolume(preferences.upDownVolumeStep.toRealVolume())
                if (playState != AudioTrack.PLAYSTATE_PLAYING) {
                    play()
                }
            }
            audioTrack.write(if (up) sampleArrayUp else sampleArrayDown, 0, if (up) sampleArrayUp.size else sampleArrayDown.size)

            tryStop()
        }
    }

    private fun tryStop() {
        audioTrack.run {
            if (playState != AudioTrack.PLAYSTATE_STOPPED) {
                pause()
                flush()
            }
        }
    }

    private fun Int.toRealVolume(): Float {
        if (this == 0) return 0f

        return baseVolume * volumeStepMultiplier.pow(this - 1)
    }

    companion object {
        const val volumeSteps = 10
        const val maxVolume = volumeSteps - 1f
        // every volume step is volumeStepMultiplier times louder than the previous one
        private const val volumeStepMultiplier = 1.8f

        // given that the max volume is 1.0, the base volume is about 0.09
        // we need to subtract 2 because the zeroth step is silence and the steps are indexed from 0
        private val baseVolume =
            AudioTrack.getMaxVolume() / volumeStepMultiplier.pow(volumeSteps - 2)
        private const val speechVolumeStepMultiplier = 0.1f
    }

}
