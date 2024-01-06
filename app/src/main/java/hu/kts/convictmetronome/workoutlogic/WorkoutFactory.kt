package hu.kts.convictmetronome.workoutlogic

import hu.kts.convictmetronome.core.Sounds
import hu.kts.convictmetronome.core.TickProvider
import hu.kts.convictmetronome.persistency.Exercise
import kotlinx.coroutines.CoroutineScope
import javax.inject.Inject
import javax.inject.Provider

class WorkoutFactory @Inject constructor(
    private val tickProvider: Provider<TickProvider>,
    private val sounds: Provider<Sounds>,
    private val countdownCalculator: Provider<CountdownCalculator>,
    private val workoutInProgressCalculator: Provider<WorkoutInProgressCalculator>,
    private val coroutineScopeProvider: Provider<CoroutineScope>
) {

    private var previousWorkout: Workout? = null

    fun create(exercise: Exercise): Workout {
        previousWorkout?.dispose()

        return Workout(
            tickProvider = tickProvider.get(),
            sounds = sounds.get(),
            countdownCalculator = countdownCalculator.get(),
            workoutInProgressCalculator = workoutInProgressCalculator.get(),
            exercise = exercise,
            coroutineScope = coroutineScopeProvider.get()
        ).also { previousWorkout = it }
    }
}
