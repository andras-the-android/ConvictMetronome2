package hu.kts.convictmetronome.persistency

import android.content.SharedPreferences
import hu.kts.convictmetronome.core.Sounds
import javax.inject.Inject

class Preferences @Inject constructor(
    private val sharedPreferences: SharedPreferences
) {

    var selectedExerciseId: Int
        get() = sharedPreferences.getInt(keySelectedExerciseId, Exercise.defaultId)
        set(value) {
            sharedPreferences.edit().apply {
                putInt(keySelectedExerciseId, value)
            }.apply()
        }

    var volumeStep: Int
        get() = sharedPreferences.getInt(keyVolume, Sounds.volumeSteps - 1)
        set(value) {
            sharedPreferences.edit().apply {
                putInt(keyVolume, value)
            }.apply()
        }

    companion object {
        private const val keySelectedExerciseId = "keySelectedExerciseId"
        private const val keyVolume = "keyVolume"
    }

}
