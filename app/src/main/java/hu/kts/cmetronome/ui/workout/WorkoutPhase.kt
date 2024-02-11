package hu.kts.cmetronome.ui.workout

sealed interface WorkoutPhase {

    data object Initial: WorkoutPhase

    data object Countdown: WorkoutPhase

    data object InProgress: WorkoutPhase

    data object Paused: WorkoutPhase

    data object BetweenSets: WorkoutPhase
}
