package hu.kts.convictmetronome.ui.workout

sealed interface WorkoutScreenState {
    data object Loading: WorkoutScreenState
    data class Content(
        val animationTargetState: WorkoutAnimationTargetState,
        val repCounter: Int = 0,
        val interSetClock: String = "",
        val completedSets: Int = 0,
        val countdownInProgress: Boolean = false,
    ): WorkoutScreenState
}
