package hu.kts.cmetronome.workoutlogic

import hu.kts.cmetronome.core.tickPeriod
import hu.kts.cmetronome.core.ticksToMs
import hu.kts.cmetronome.persistency.Exercise
import hu.kts.cmetronome.ui.workout.WorkoutAnimationTargetState
import javax.inject.Inject

class WorkoutInProgressCalculator @Inject constructor() {

    fun getCounterAndAnimationTarget(exercise: Exercise, ticks: Int): Pair<Int, WorkoutAnimationTargetState> {
        return if (exercise.startWithUp) {
            getCounterAndAnimationTargetForStartWithUp(exercise, ticks)
        } else {
            getCounterAndAnimationTargetForStartWithDown(exercise, ticks)
        }
    }

    private fun getCounterAndAnimationTargetForStartWithUp(exercise: Exercise, ticks: Int): Pair<Int, WorkoutAnimationTargetState>  {
        val repDuration = exercise.calcRepDuration()
        // the first rep starts with up, but the others starts with lower hold,
        // so we have to add a lower hold duration to the elapsedTimeSinceSetStart to keep
        // the calculations accurate
        val elapsedTimeSinceSetStart = ticks.ticksToMs() + exercise.lowerHoldMillis
        val counter = elapsedTimeSinceSetStart / repDuration
        val elapsedTimeFromCurrentRep = elapsedTimeSinceSetStart % repDuration
        val topStateStart = exercise.lowerHoldMillis
        val topStateEnd = exercise.lowerHoldMillis + exercise.upMillis + exercise.upperHoldMillis
        val targetState = when (elapsedTimeFromCurrentRep) {
            in topStateStart..<topStateEnd -> WorkoutAnimationTargetState.Top(exercise.upMillis)
            else -> WorkoutAnimationTargetState.Bottom(exercise.downMillis)
        }
        return Pair(counter, targetState)
    }

    private fun getCounterAndAnimationTargetForStartWithDown(exercise: Exercise, ticks: Int): Pair<Int, WorkoutAnimationTargetState>  {
        val repDuration = exercise.calcRepDuration()
        // the first rep starts with down, but the others starts with upper hold,
        // so we have to add an upper hold duration to the elapsedTimeSinceSetStart to keep
        // the calculations accurate
        val elapsedTimeSinceSetStart = ticks.ticksToMs() + exercise.upperHoldMillis
        val counter = elapsedTimeSinceSetStart / repDuration
        val elapsedTimeFromCurrentRep = elapsedTimeSinceSetStart % repDuration
        val bottomStateStart = exercise.upperHoldMillis
        val bottomStateEnd = exercise.lowerHoldMillis + exercise.downMillis + exercise.upperHoldMillis
        val targetState = when (elapsedTimeFromCurrentRep) {
            in bottomStateStart..<bottomStateEnd -> WorkoutAnimationTargetState.Bottom(exercise.downMillis)
            else -> WorkoutAnimationTargetState.Top(exercise.upMillis)
        }
        return Pair(counter, targetState)
    }

    fun removeLatestRepFromTicks(exercise: Exercise, ticks: Int): Int {
        val repDurationInTicks = exercise.calcRepDuration() / tickPeriod
        return ticks / repDurationInTicks * repDurationInTicks
    }

}
