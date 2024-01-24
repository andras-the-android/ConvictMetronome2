package hu.kts.cmetronome.workoutlogic

import android.util.Log
import hu.kts.cmetronome.persistency.Exercise
import hu.kts.cmetronome.sounds.Sounds
import hu.kts.cmetronome.timer.SecondsTimer
import hu.kts.cmetronome.timer.tickPeriod
import hu.kts.cmetronome.timer.ticksToMs
import hu.kts.cmetronome.ui.workout.WorkoutAnimationTargetState
import hu.kts.cmetronome.ui.workout.WorkoutPhase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class Workout(
    private val secondsTimer: SecondsTimer,
    private val sounds: Sounds,
    private val countdownCalculator: CountdownCalculator,
    private val workoutInProgressCalculator: WorkoutInProgressCalculator,
    private val exercise: Exercise,
    private val coroutineScope: CoroutineScope,
) {

    var phase: WorkoutPhase
        get() = _state.value.phase
        private set(value) {
            // reset animation if we switch from InProgress
            val animationTargetState = if (_state.value.phase is WorkoutPhase.InProgress) {
                exercise.getInitialAnimationTargetState()
            } else {
                _state.value.animationTargetState
            }
            _state.update { it.copy(phase = value, animationTargetState = animationTargetState) }
        }


    private val _state = MutableStateFlow(WorkoutState(
        animationTargetState = exercise.getInitialAnimationTargetState()
    ))
    val state = _state.asStateFlow()

    init {
        coroutineScope.launch {
            secondsTimer.tickFlow.collect { onTick() }
        }
    }

    fun onCounterClick() {
        Log.d(tagWorkout, "onCounterClick")
        when (val localPhase = phase) {
            is WorkoutPhase.Initial -> {
                phase = WorkoutPhase.Countdown()
                secondsTimer.start()
            }
            is WorkoutPhase.Countdown -> {
                secondsTimer.stop()
                phase =
                    WorkoutPhase.Paused(ticksFromPreviousPhase = localPhase.ticksFromPreviousPhase)
            }
            is WorkoutPhase.InProgress -> {
                sounds.stop()
                secondsTimer.stop()
                phase = WorkoutPhase.Paused(
                    ticksFromPreviousPhase =
                    workoutInProgressCalculator.removeLatestRepFromTicks(exercise, localPhase.ticks)
                )

            }
            is WorkoutPhase.Paused -> {
                phase =
                    WorkoutPhase.Countdown(ticksFromPreviousPhase = localPhase.ticksFromPreviousPhase)
                secondsTimer.start()
            }
            is WorkoutPhase.BetweenSets -> {
                phase = WorkoutPhase.Countdown()
            }
        }
    }

    fun onCounterLongClick(): Boolean {
        Log.d(tagWorkout, "onCounterLongClick")
        when (phase) {
            is WorkoutPhase.InProgress-> {
                sounds.stop()
                phase = WorkoutPhase.BetweenSets()
                return true
            }

            is WorkoutPhase.Paused -> {
                phase = WorkoutPhase.BetweenSets()
                secondsTimer.start()
                return true
            }

            is WorkoutPhase.BetweenSets -> {
                throw IllegalStateException("Create a new workout instead!")
            }

            is WorkoutPhase.Countdown, WorkoutPhase.Initial -> {}
        }
        return false
    }

    fun dispose() {
        coroutineScope.cancel()
    }

    private fun onTick() {
        when (val localPhase = phase.inc()) {

            is WorkoutPhase.Initial -> throw IllegalStateException("Tick provider should not run when state is initial")

            is WorkoutPhase.Countdown -> {
                val repCounter = countdownCalculator.getCounter(exercise, localPhase.ticks)
                if (repCounter > 0) {
                    _state.update {
                        it.copy(
                            repCounter = repCounter,
                            interSetClockMillis = null,
                            phase = localPhase,
                        )
                    }
                } else {
                    phase = WorkoutPhase.InProgress(localPhase.ticksFromPreviousPhase)
                    onTick()
                }
            }

            is WorkoutPhase.InProgress -> {
                val (repCounter, newAnimationTargetState) = workoutInProgressCalculator.getCounterAndAnimationTarget(exercise, localPhase.ticks)
                if (_state.value.animationTargetState != newAnimationTargetState) {
                    when (newAnimationTargetState) {
                        is WorkoutAnimationTargetState.Bottom -> sounds.makeDownSound()
                        is WorkoutAnimationTargetState.Top -> sounds.makeUpSound()
                    }
                }
                _state.update {
                    it.copy(
                        repCounter = repCounter,
                        animationTargetState = newAnimationTargetState,
                        phase = localPhase,
                    )
                }
            }

            is WorkoutPhase.Paused -> throw IllegalStateException("Tick provider should not run when state is paused")

            is WorkoutPhase.BetweenSets -> {
                _state.update {
                    it.copy(
                        interSetClockMillis = localPhase.ticks.ticksToMs(),
                        // increase the completed sets if this is the first tick in the phase
                        completedSets = if (localPhase.ticks == 0) it.completedSets + 1 else it.completedSets,
                        phase = localPhase,
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

    companion object {
        private val beepTicks = TimeUnit.MINUTES.toMillis(1).toInt() / tickPeriod
        const val animationResetDuration = 200
        const val tagWorkout = "tagworkout"
    }

}
