package hu.kts.cmetronome.workoutlogic

import hu.kts.cmetronome.persistency.Exercise
import hu.kts.cmetronome.sounds.Sounds
import hu.kts.cmetronome.timer.SecondsTimer
import kotlinx.coroutines.CoroutineScope
import javax.inject.Inject
import javax.inject.Provider

class WorkoutFactory @Inject constructor(
    private val secondsTimer: Provider<SecondsTimer>,
    private val sounds: Provider<Sounds>,
    private val countdownCalculator: Provider<CountdownCalculator>,
    private val workoutInProgressCalculator: Provider<WorkoutInProgressCalculator>,
    private val coroutineScopeProvider: Provider<CoroutineScope>
) {

    private var previousWorkout: Workout? = null

    fun create(exercise: Exercise): Workout {
        previousWorkout?.dispose()

        return Workout(
            secondsTimer = secondsTimer.get(),
            sounds = sounds.get(),
            countdownCalculator = countdownCalculator.get(),
            workoutInProgressCalculator = workoutInProgressCalculator.get(),
            exercise = exercise,
            coroutineScope = coroutineScopeProvider.get()
        ).also { previousWorkout = it }
    }
}
