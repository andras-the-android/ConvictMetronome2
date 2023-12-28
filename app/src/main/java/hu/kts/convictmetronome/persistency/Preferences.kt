package hu.kts.convictmetronome.persistency

import android.content.SharedPreferences
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

    companion object {
        private const val keySelectedExerciseId = "keySelectedExerciseId"
    }

}
