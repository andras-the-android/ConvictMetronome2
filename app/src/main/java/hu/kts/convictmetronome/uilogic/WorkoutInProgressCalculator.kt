package hu.kts.convictmetronome.uilogic

import hu.kts.convictmetronome.core.ticksToMs
import hu.kts.convictmetronome.persistency.Exercise
import hu.kts.convictmetronome.ui.workout.WorkoutSideEffect
import javax.inject.Inject

class WorkoutInProgressCalculator @Inject constructor() {

    fun getCounterAndSideEffect(exercise: Exercise, tickCount: Int) : Pair<Int, WorkoutSideEffect?> {
        val repDuration = exercise.run { downMillis + lowerHoldMillis + upMillis + upperHoldMillis  }
        val elapsedTimeSinceSetStart = tickCount.ticksToMs()
        val counter = elapsedTimeSinceSetStart / repDuration
        val elapsedTimeFromCurrentRep = elapsedTimeSinceSetStart % repDuration
        val sideEffect = if (exercise.startWithUp) {
            when {
                elapsedTimeFromCurrentRep == 0 -> WorkoutSideEffect.animationUp
                elapsedTimeFromCurrentRep - exercise.upMillis - exercise.upperHoldMillis == 0 -> WorkoutSideEffect.animationDown
                else -> null
            }
        } else {
            when {
                elapsedTimeFromCurrentRep == 0 -> WorkoutSideEffect.animationDown
                elapsedTimeFromCurrentRep - exercise.downMillis - exercise.lowerHoldMillis == 0 -> WorkoutSideEffect.animationUp
                else -> null
            }
        }
        return Pair(counter, sideEffect)
    }

}