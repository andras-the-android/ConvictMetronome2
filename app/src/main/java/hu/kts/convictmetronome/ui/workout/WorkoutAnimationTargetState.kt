package hu.kts.convictmetronome.ui.workout

sealed interface WorkoutAnimationTargetState {

    data class Top(override val durationMillis: Int): WorkoutAnimationTargetState
    data class Bottom(override val durationMillis: Int): WorkoutAnimationTargetState

    val durationMillis: Int
}
