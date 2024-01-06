package hu.kts.convictmetronome.repository

import android.util.Log
import hu.kts.convictmetronome.workoutlogic.Workout.Companion.tagWorkout
import hu.kts.convictmetronome.workoutlogic.WorkoutFactory
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
) {
    private val resetWorkout = MutableSharedFlow<Unit>(replay = 1)

    val activeWorkout = combine(
        exerciseRepository.selectedExercise,
        resetWorkout,
    ) { selectedExercise, _ ->
        Log.v(tagWorkout, "WorkoutRepository state triggered")
        workoutFactory.create(selectedExercise)
    }.shareIn(coroutineScope, SharingStarted.WhileSubscribed(5000), 1)

    init {
        // we have to send one event to make combine work
        resetWorkout()
    }

    fun resetWorkout() {
        coroutineScope.launch {
            resetWorkout.emit(Unit)
        }
    }



}
