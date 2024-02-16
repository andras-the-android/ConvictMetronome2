package hu.kts.cmetronome.repository

import android.util.Log
import hu.kts.cmetronome.persistency.Preferences
import hu.kts.cmetronome.workoutlogic.Workout.Companion.tagWorkout
import hu.kts.cmetronome.workoutlogic.WorkoutFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkoutRepository @Inject constructor(
    exerciseRepository: ExerciseRepository,
    workoutFactory: WorkoutFactory,
    private val coroutineScope: CoroutineScope,
    private val preferences: Preferences
) {
    private val resetWorkout = MutableSharedFlow<Unit>(replay = 1)

    val activeWorkout = combine(
        exerciseRepository.selectedExercise,
        resetWorkout,
    ) { selectedExercise, _ ->
        Log.v(tagWorkout, "WorkoutRepository state triggered")
        val workout = workoutFactory.create(selectedExercise, preferences.workoutState)
        coroutineScope.launch {
            workout.persistentState.collect {
                preferences.workoutState = it
            }
        }
        return@combine workout
    }.shareIn(coroutineScope, SharingStarted.Lazily, 1)

    init {
        // we have to send one event to make combine work
        triggerResetEvent()
    }

    fun resetWorkout() {
        preferences.clearSavedWorkoutState()
        triggerResetEvent()
    }

    private fun triggerResetEvent() {
        coroutineScope.launch {
            resetWorkout.emit(Unit)
        }
    }


}
