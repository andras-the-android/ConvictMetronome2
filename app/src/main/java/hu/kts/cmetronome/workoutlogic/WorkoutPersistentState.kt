package hu.kts.cmetronome.workoutlogic

import hu.kts.cmetronome.ui.workout.WorkoutPhase

data class WorkoutPersistentState(
    val phase: WorkoutPhase = WorkoutPhase.Initial,
    val reps: Int = 0,
    val completedSets: Int = 0,
    val interSetTimerStartedUtc: Long = 0
)
