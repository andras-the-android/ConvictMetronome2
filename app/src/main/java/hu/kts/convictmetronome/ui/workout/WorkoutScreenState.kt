package hu.kts.convictmetronome.ui.workout

import androidx.annotation.StringRes
import hu.kts.convictmetronome.R

sealed interface WorkoutScreenState {
    data object Loading: WorkoutScreenState
    data class Content(
        val animationTargetState: WorkoutAnimationTargetState,
        val repCounter: Int = 0,
        val interSetClock: String = "",
        val completedSets: Int = 0,
        val countdownInProgress: Boolean = false,
        @StringRes val helpTextResourceId: Int = R.string.empty_string,
    ): WorkoutScreenState
}
