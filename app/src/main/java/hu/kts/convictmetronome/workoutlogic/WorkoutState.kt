package hu.kts.convictmetronome.workoutlogic

import hu.kts.convictmetronome.ui.workout.WorkoutAnimationTargetState
import hu.kts.convictmetronome.ui.workout.WorkoutPhase

data class WorkoutState(
    val animationTargetState: WorkoutAnimationTargetState,
    val repCounter: Int = 0,
    val interSetClockMillis: Int? = null,
    val completedSets: Int = 0,
    val phase: WorkoutPhase = WorkoutPhase.Initial,
)
