package hu.kts.cmetronome.ui.workout

import android.annotation.SuppressLint
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import hu.kts.cmetronome.R
import hu.kts.cmetronome.repository.WorkoutRepository
import hu.kts.cmetronome.ui.workout.WorkoutPhase.BetweenSets
import hu.kts.cmetronome.ui.workout.WorkoutPhase.Countdown
import hu.kts.cmetronome.workoutlogic.Workout
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class WorkoutViewModel @Inject constructor(
    private val workoutRepository: WorkoutRepository,
): ViewModel(), WorkoutActionCallbacks {

    private val showConfirmResetWorkoutDialog = MutableStateFlow(false)

    @OptIn(ExperimentalCoroutinesApi::class)
    val state = workoutRepository
        .activeWorkout
        .flatMapLatest {
            it.state
        }
        .combine(showConfirmResetWorkoutDialog) { workoutState, showConfirmResetWorkoutDialog ->
            Log.v(Workout.tagWorkout, "WorkoutViewModel state triggered $workoutState")
            val interSetClock = when (workoutState.phase) {
                is BetweenSets -> interSetClockFormat.format(Date(workoutState.interSetClockMillis?.toLong() ?: 0L))
                else -> ""
            }

            WorkoutScreenState.Content(
                animationTargetState = workoutState.animationTargetState,
                repCounter = workoutState.repCounter,
                interSetClock = interSetClock,
                completedSets = workoutState.completedSets,
                countdownInProgress = workoutState.phase is Countdown,
                helpTextResourceId = workoutState.phase.helpTextId(),
                keepScreenAlive = workoutState.phase.shouldKeepScreenAlive(),
                showConfirmResetWorkoutDialog = showConfirmResetWorkoutDialog,
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), WorkoutScreenState.Loading)

    override fun onClick() {
        viewModelScope.launch {
            getWorkout().onCounterClick()
        }
    }

    override fun onLongClick(eventConsumed: () -> Unit) {
        viewModelScope.launch {
            val workout = getWorkout()
            if (workout.phase is BetweenSets) {
                showConfirmResetWorkoutDialog.value = true
                eventConsumed()
            } else {
                if (workout.onCounterLongClick()) { eventConsumed() }
            }
        }
    }

    override fun confirmReset() {
        workoutRepository.resetWorkout()
        dismissConfirmResetDialog()
    }

    override fun dismissConfirmResetDialog() {
        showConfirmResetWorkoutDialog.value = false
    }

    private fun WorkoutPhase.helpTextId(): Int {
        return when (this) {
            is BetweenSets -> R.string.help_between_sets
            is WorkoutPhase.InProgress -> R.string.help_in_progress
            WorkoutPhase.Initial -> R.string.help_initial
            is WorkoutPhase.Paused -> R.string.help_paused
            else -> R.string.empty_string
        }
    }

    private fun WorkoutPhase.shouldKeepScreenAlive(): Boolean {
        return this is WorkoutPhase.InProgress || this is Countdown || this is BetweenSets
    }

    private suspend fun getWorkout() = workoutRepository.activeWorkout.first()

    companion object {
        @SuppressLint("SimpleDateFormat")
        private val interSetClockFormat = SimpleDateFormat("mm:ss")
    }

}
