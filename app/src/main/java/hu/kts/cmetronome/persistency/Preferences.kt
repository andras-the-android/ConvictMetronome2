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

    var volumeStep: Int
        get() = sharedPreferences.getInt(keyVolume, Sounds.volumeSteps - 1)
        set(value) {
            sharedPreferences.edit().apply {
                putInt(keyVolume, value)
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
        private const val keyVolume = "keyVolume"
        private const val keyWorkoutState = "keyWorkoutState"
    }

}
