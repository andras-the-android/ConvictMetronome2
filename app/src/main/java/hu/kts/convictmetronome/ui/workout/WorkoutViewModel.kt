package hu.kts.convictmetronome.ui.workout

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import hu.kts.convictmetronome.core.Sounds
import hu.kts.convictmetronome.core.TickProvider
import hu.kts.convictmetronome.core.tickPeriod
import hu.kts.convictmetronome.persistency.Exercise
import hu.kts.convictmetronome.repository.ExerciseRepository
import hu.kts.convictmetronome.ui.workout.WorkoutPhase.BetweenSets
import hu.kts.convictmetronome.ui.workout.WorkoutPhase.Countdown
import hu.kts.convictmetronome.ui.workout.WorkoutPhase.InProgress
import hu.kts.convictmetronome.ui.workout.WorkoutPhase.Initial
import hu.kts.convictmetronome.ui.workout.WorkoutPhase.Paused
import hu.kts.convictmetronome.uilogic.BetweenSetsCalculator
import hu.kts.convictmetronome.uilogic.CountdownCalculator
import hu.kts.convictmetronome.uilogic.WorkoutInProgressCalculator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class WorkoutViewModel @Inject constructor(
    private val tickProvider: TickProvider,
    private val sounds: Sounds,
    private val exerciseRepository: ExerciseRepository,
    private val countdownCalculator: CountdownCalculator,
    private val workoutInProgressCalculator: WorkoutInProgressCalculator,
    private val betweenSetsCalculator: BetweenSetsCalculator,
): ViewModel() {

    private var phase: WorkoutPhase = Initial
    private lateinit var exercise: Exercise

    private val _state = MutableStateFlow<WorkoutScreenState>(WorkoutScreenState.Loading)
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            tickProvider.tickFlow.collect { onTick() }
        }
        viewModelScope.launch {
            exerciseRepository.selectedExercise.collect { exercise ->
                initWorkout(exercise)
            }
        }
        viewModelScope.launch {
            _state.collect {
                Log.i(tagWorkout, it.toString())
            }
        }
    }

    private fun initWorkout(exercise: Exercise) {
        tickProvider.stop()
        phase = Initial
        this.exercise = exercise
        _state.value = WorkoutScreenState.Content(
            animationTargetState = exercise.getInitialAnimationTargetState()
        )
    }

    fun onCounterClick() {
        if (!this::exercise.isInitialized) throw IllegalStateException("Click events should not happen before exercise initialization")
        when (val localPhase = phase) {
            is Initial -> {
                phase = Countdown()
                tickProvider.start()
            }
            is Countdown -> {
                tickProvider.stop()
                phase = Paused(ticksFromPreviousPhase = localPhase.ticksFromPreviousPhase)
            }
            is InProgress -> {
                sounds.stop()
                tickProvider.stop()
                phase = Paused(
                    ticksFromPreviousPhase =
                    workoutInProgressCalculator.removeLatestRepFromTicks(exercise, localPhase.ticks))
                resetAnimation()

            }
            is Paused -> {
                phase = Countdown(ticksFromPreviousPhase = localPhase.ticksFromPreviousPhase)
                tickProvider.start()
            }
            is BetweenSets -> {
                phase = Countdown()
            }
        }
    }

    fun onCounterLongClick(): Boolean {
        if (!this::exercise.isInitialized) throw IllegalStateException("Click events should not happen before exercise initialization")
        when (phase) {
            is InProgress, is Paused -> {
                sounds.stop()
                phase = BetweenSets()
                resetAnimation()
            }

            is BetweenSets -> {
                initWorkout(exercise)
            }

            is Countdown, Initial -> {}
        }
        return false
    }

    private fun onTick() {
        when (val localPhase = ++phase) {

            is Initial -> throw IllegalStateException("Tick provider should not run when state is initial")

            is Countdown -> {
                Log.v(tagWorkout, "countdown")
                val repCounter = countdownCalculator.getCounter(exercise, localPhase.ticks)
                if (repCounter > 0) {
                    _state.update {
                        (it as WorkoutScreenState.Content)
                            .copy(
                                repCounter = repCounter,
                                interSetClock = "",
                                countdownInProgress = true
                            )
                    }
                } else {
                    phase = InProgress(localPhase.ticksFromPreviousPhase)
                    onTick()
                }
            }

            is InProgress -> {
                val (repCounter, newAnimationTargetState) = workoutInProgressCalculator.getCounterAndAnimationTarget(exercise, localPhase.ticks)
                Log.v(tagWorkout, "in progress, rep counter: $repCounter, animation state: $newAnimationTargetState")
                _state.update { (it as WorkoutScreenState.Content)
                    .copy(
                        repCounter = repCounter,
                        animationTargetState = newAnimationTargetState,
                        countdownInProgress = false
                    )
                }
            }

            is Paused -> throw IllegalStateException("Tick provider should not run when state is paused")

            is BetweenSets -> {
                val clock = betweenSetsCalculator.getClock(localPhase.ticks)
                _state.update {
                    (it as WorkoutScreenState.Content).copy(
                        interSetClock = clock,
                        // increase the completed sets if this is the first tick in the phase
                        completedSets = if (localPhase.ticks == 0) it.completedSets + 1 else it.completedSets
                    )
                }
                // beep after every elapsed minutes
                if (localPhase.ticks > 0 && (localPhase.ticks % beepTicks) == 0) sounds.beep()
            }
        }
    }

    private fun Exercise.getInitialAnimationTargetState(): WorkoutAnimationTargetState {
        return if (this.startWithUp)
            WorkoutAnimationTargetState.Bottom(animationResetDuration)
        else
            WorkoutAnimationTargetState.Top(animationResetDuration)
    }

    private fun resetAnimation() {
        _state.update {
            (it as WorkoutScreenState.Content).copy(
                animationTargetState = exercise.getInitialAnimationTargetState(),
                countdownInProgress = false
            )
        }
    }

    companion object {
        private val beepTicks = TimeUnit.MINUTES.toMillis(1).toInt() / tickPeriod
        const val animationResetDuration = 200
        const val tagWorkout = "tagworkout"
    }

}
