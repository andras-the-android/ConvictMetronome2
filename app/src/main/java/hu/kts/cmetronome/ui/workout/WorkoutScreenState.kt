package hu.kts.cmetronome.ui.workout

import androidx.annotation.StringRes
import hu.kts.cmetronome.R

sealed interface WorkoutScreenState {
    data object Loading: WorkoutScreenState
    data class Content(
        val animationTargetState: WorkoutAnimationTargetState,
        val repCounter: Int = 0,
        val interSetClock: String = "",
        val completedSets: Int = 0,
        val countdownInProgress: Boolean = false,
        @StringRes val helpTextResourceId: Int = R.string.empty_string,
        val keepScreenAlive: Boolean = false,
        val showConfirmResetWorkoutDialog: Boolean = false,
    ): WorkoutScreenState
}
