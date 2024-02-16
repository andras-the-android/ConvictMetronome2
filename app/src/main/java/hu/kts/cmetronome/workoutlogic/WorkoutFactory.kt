package hu.kts.cmetronome.workoutlogic

import hu.kts.cmetronome.persistency.Exercise
import hu.kts.cmetronome.sounds.Sounds
import hu.kts.cmetronome.timer.ExerciseTimer
import hu.kts.cmetronome.timer.SecondsTimer
import kotlinx.coroutines.CoroutineScope
import java.time.Clock
import javax.inject.Inject
import javax.inject.Provider

class WorkoutFactory @Inject constructor(
    private val secondsTimer: Provider<SecondsTimer>,
    private val exerciseTimer: Provider<ExerciseTimer>,
    private val sounds: Provider<Sounds>,
    private val coroutineScopeProvider: Provider<CoroutineScope>,
    private val clockProvider: Provider<Clock>,
) {

    private var previousWorkout: Workout? = null

    fun create(exercise: Exercise, savedState: WorkoutPersistentState): Workout {
        previousWorkout?.dispose()

        return Workout(
            secondsTimer = secondsTimer.get(),
            exerciseTimer = exerciseTimer.get(),
            sounds = sounds.get(),
            exercise = exercise,
            coroutineScope = coroutineScopeProvider.get(),
            clock = clockProvider.get(),
            savedState = savedState
        ).also { previousWorkout = it }
    }
}
