package hu.kts.cmetronome.persistency

import android.content.SharedPreferences
import com.google.gson.Gson
import hu.kts.cmetronome.sounds.Sounds
import hu.kts.cmetronome.workoutlogic.WorkoutPersistentState
import javax.inject.Inject

class Preferences @Inject constructor(
    private val sharedPreferences: SharedPreferences,
    private val gson: Gson,
) {

    var selectedExerciseId: Int
        get() = sharedPreferences.getInt(keySelectedExerciseId, Exercise.defaultId)
        set(value) {
            sharedPreferences.edit().apply {
                putInt(keySelectedExerciseId, value)
            }.apply()
        }

    var upDownVolumeStep: Int
        get() = sharedPreferences.getInt(keyUpDownVolumeStep, Sounds.volumeSteps - 1)
        set(value) {
            sharedPreferences.edit().apply {
                putInt(keyUpDownVolumeStep, value)
            }.apply()
        }

    var speechVolumeStep: Int
        get() = sharedPreferences.getInt(keySpeechVolumeStep, Sounds.volumeSteps - 1)
        set(value) {
            sharedPreferences.edit().apply {
                putInt(keySpeechVolumeStep, value)
            }.apply()
        }

    var workoutState: WorkoutPersistentState
        get() {
            if (!sharedPreferences.contains(keyWorkoutState)) return WorkoutPersistentState()

            return gson.fromJson(
                sharedPreferences.getString(keyWorkoutState, ""),
                WorkoutPersistentState::class.java
            )
        }
        set(value) {
            sharedPreferences.edit().apply {
                putString(keyWorkoutState, gson.toJson(value))
            }.apply()
        }

    fun clearSavedWorkoutState() {
        sharedPreferences.edit().apply {
            remove(keyWorkoutState)
        }.apply()
    }

    companion object {
        private const val keySelectedExerciseId = "keySelectedExerciseId"
        private const val keyUpDownVolumeStep = "keyVolume"
        private const val keySpeechVolumeStep = "keySpeechVolume"
        private const val keyWorkoutState = "keyWorkoutState"
    }

}
