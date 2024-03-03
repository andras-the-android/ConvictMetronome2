package hu.kts.cmetronome.workoutlogic

import android.util.Log
import hu.kts.cmetronome.persistency.Exercise
import hu.kts.cmetronome.persistency.getInitialAnimationTargetState
import hu.kts.cmetronome.persistency.phaseDurationMillis
import hu.kts.cmetronome.sounds.Sounds
import hu.kts.cmetronome.timer.ExercisePhase
import hu.kts.cmetronome.timer.ExerciseTimer
import hu.kts.cmetronome.timer.SecondsTimer
import hu.kts.cmetronome.ui.workout.WorkoutAnimationTargetState
import hu.kts.cmetronome.ui.workout.WorkoutPhase
import hu.kts.cmetronome.ui.workout.WorkoutPhase.BetweenSets
import hu.kts.cmetronome.ui.workout.WorkoutPhase.Countdown
import hu.kts.cmetronome.ui.workout.WorkoutPhase.InProgress
import hu.kts.cmetronome.ui.workout.WorkoutPhase.Initial
import hu.kts.cmetronome.ui.workout.WorkoutPhase.Paused
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Clock
import java.util.concurrent.TimeUnit

class Workout(
    private val secondsTimer: SecondsTimer,
    private val exerciseTimer: ExerciseTimer,
    private val sounds: Sounds,
    private val exercise: Exercise,
    private val coroutineScope: CoroutineScope,
    private val clock: Clock,
    savedState: WorkoutPersistentState,
) {

    private val _persistentState = MutableStateFlow(savedState.initFromSavedState())
    val persistentState = _persistentState.asStateFlow()

    private val animationTargetState = MutableStateFlow(exercise.getInitialAnimationTargetState())
    private var countdownValue = MutableStateFlow(0)
    private var interSetClockMillis = MutableStateFlow<Int?>(null)

    var phase: WorkoutPhase
        get() = _persistentState.value.phase
        private set(value) {
            // reset animation if we switch from InProgress
            if (_persistentState.value.phase == InProgress) {
                animationTargetState.value = exercise.getInitialAnimationTargetState()
            }
            interSetClockMillis.value = null

            when (value) {
                BetweenSets -> {
                    sounds.stop()
                    exerciseTimer.stop()
                    interSetClockMillis.value = 0
                    _persistentState.update {
                        it.copy(
                            phase = value,
                            interSetTimerStartedUtc = clock.millis(),
                            completedSets = it.completedSets.inc()
                        )
                    }
                    secondsTimer.start()
                }

                Countdown -> {
                    // reset reps if the previous state was BetweenSets
                    val reps =
                        if (_persistentState.value.phase == BetweenSets) 0 else _persistentState.value.reps
                    secondsTimer.stop()
                    countdownValue.value =
                        TimeUnit.MILLISECONDS.toSeconds(exercise.countdownFromMillis.toLong())
                            .toInt()
                    _persistentState.update { it.copy(phase = value, reps = reps) }
                    secondsTimer.start()
                }

                InProgress -> {
                    _persistentState.update { it.copy(phase = value) }
                    exerciseTimer.start(exercise)
                }

                Initial -> {
                    _persistentState.update { it.copy(phase = value) }
                }

                Paused -> {
                    sounds.stop()
                    exerciseTimer.stop()
                    secondsTimer.stop()
                    _persistentState.update { it.copy(phase = value) }
                }
            }
        }

    val state = combine(
        _persistentState,
        animationTargetState,
        countdownValue,
        interSetClockMillis,
    ) { persistentState, animationTargetState, countdownValue, interSetClockMillis ->
        WorkoutState(
            animationTargetState = animationTargetState,
            repCounter = if (persistentState.phase == Countdown) countdownValue else persistentState.reps,
            interSetClockMillis = interSetClockMillis,
            completedSets = persistentState.completedSets,
            phase = persistentState.phase
        )
    }

    init {
        coroutineScope.launch {
            secondsTimer.tickFlow.collect { onSecondTick() }
        }
        coroutineScope.launch {
            exerciseTimer.eventFlow.collect { onExercisePhaseChanged(it) }
        }
        if (phase == BetweenSets) secondsTimer.start()
    }

    fun onCounterClick() {
        Log.d(tagWorkout, "onCounterClick")
        when (phase) {
            Initial -> {
                phase = Countdown
            }

            Countdown -> {
                phase = Paused
            }

            InProgress -> {
                phase = Paused
            }

            Paused -> {
                phase = Countdown
            }

            BetweenSets -> {
                phase = Countdown
            }
        }
    }

    fun onCounterLongClick(): Boolean {
        Log.d(tagWorkout, "onCounterLongClick")
        when (phase) {
            InProgress -> {
                phase = BetweenSets
                return true
            }

            Paused -> {
                phase = BetweenSets
                return true
            }

            BetweenSets -> {
                throw IllegalStateException("Create a new workout instead!")
            }

            Countdown, Initial -> {}
        }
        return false
    }

    fun dispose() {
        coroutineScope.cancel()
    }

    private fun onSecondTick() {
        if (_persistentState.value.phase == BetweenSets) {
            val interSetClockMillisLong =
                clock.millis() - _persistentState.value.interSetTimerStartedUtc
            val seconds = TimeUnit.MILLISECONDS.toSeconds(interSetClockMillisLong)
            if (seconds > 0 && seconds % beepSeconds == 0L) sounds.beep()
            interSetClockMillis.value = interSetClockMillisLong.toInt()
        }

        if (_persistentState.value.phase == Countdown) {
            if (countdownValue.value == 1) {
                phase = InProgress
                return
            }
            countdownValue.update { it.dec() }
        }
    }

    private fun onExercisePhaseChanged(phase: ExercisePhase) {
        when (phase) {
            ExercisePhase.Down -> {
                animationTargetState.value = WorkoutAnimationTargetState.Bottom(exercise.phaseDurationMillis(phase))
                sounds.makeDownSound()
            }
            ExercisePhase.LowerHold -> {
                if (exercise.startWithUp) {
                    _persistentState.update { it.copy(reps = it.reps.inc()) }
                    sounds.announceRepCounter(_persistentState.value.reps)
                }
            }

            ExercisePhase.Up -> {
                animationTargetState.value =
                    WorkoutAnimationTargetState.Top(exercise.phaseDurationMillis(phase))
                sounds.makeUpSound()
            }

            ExercisePhase.UpperHold -> {
                if (!exercise.startWithUp) {
                    _persistentState.update { it.copy(reps = it.reps.inc()) }
                    sounds.announceRepCounter(_persistentState.value.reps)
                }
            }
        }
    }

    private fun WorkoutPersistentState.initFromSavedState(): WorkoutPersistentState {
        val phase = when (this.phase) {
            Initial -> Initial
            Countdown -> Paused
            InProgress -> Paused
            Paused -> Paused
            BetweenSets -> BetweenSets
        }
        return this.copy(phase = phase)
    }

    companion object {
        private const val beepSeconds = 60
        const val animationResetDuration = 200
        const val tagWorkout = "tagworkout"
    }

}
