package hu.kts.convictmetronome.ui.workout

import android.annotation.SuppressLint
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import hu.kts.convictmetronome.R
import hu.kts.convictmetronome.repository.WorkoutRepository
import hu.kts.convictmetronome.ui.workout.WorkoutPhase.BetweenSets
import hu.kts.convictmetronome.ui.workout.WorkoutPhase.Countdown
import hu.kts.convictmetronome.workoutlogic.Workout
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
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

    @OptIn(ExperimentalCoroutinesApi::class)
    val state = workoutRepository
        .activeWorkout
        .flatMapLatest {
            it.state
        }
        .map { workoutState ->
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
                keepScreenAlive = workoutState.phase.shouldKeepScreenAlive()
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
                workoutRepository.resetWorkout()
                eventConsumed()
            } else {
                if (workout.onCounterLongClick()) { eventConsumed() }
            }
        }
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
