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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
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
    private val clock: Clock
) {

    private val persistentState = MutableStateFlow(WorkoutPersistentState())
    private val animationTargetState = MutableStateFlow(exercise.getInitialAnimationTargetState())
    private var countdownValue = MutableStateFlow(countdownEmptyValue)
    private var interSetClockMillis = MutableStateFlow<Int?>(null)

    var phase: WorkoutPhase
        get() = persistentState.value.phase
        private set(value) {
            // reset animation if we switch from InProgress
            if (persistentState.value.phase is WorkoutPhase.InProgress) {
                animationTargetState.value = exercise.getInitialAnimationTargetState()
            }
            interSetClockMillis.value = null

            when (value) {
                WorkoutPhase.BetweenSets -> {
                    sounds.stop()
                    exerciseTimer.stop()
                    persistentState.update {
                        it.copy(
                            phase = value,
                            interSetTimerStartedUtc = clock.millis(),
                            completedSets = it.completedSets.inc()
                        )
                    }
                    secondsTimer.start()
                }
                WorkoutPhase.Countdown -> {
                    // reset reps if the previous state was BetweenSets
                    val reps = if (persistentState.value.phase is WorkoutPhase.BetweenSets) 0 else persistentState.value.reps
                    secondsTimer.stop()
                    persistentState.update { it.copy(phase = value, reps = reps) }
                    secondsTimer.start()
                }
                WorkoutPhase.InProgress -> {
                    persistentState.update { it.copy(phase = value) }
                    exerciseTimer.start(exercise)
                }
                WorkoutPhase.Initial -> {
                    persistentState.update { it.copy(phase = value) }
                }
                WorkoutPhase.Paused -> {
                    sounds.stop()
                    exerciseTimer.stop()
                    secondsTimer.stop()
                    persistentState.update { it.copy(phase = value) }
                }
            }
        }

    val state = combine(
        persistentState,
        animationTargetState,
        countdownValue,
        interSetClockMillis,
    ) { persistentState, animationTargetState, countdownValue, interSetClockMillis ->
        WorkoutState(
            animationTargetState = animationTargetState,
            repCounter = if (persistentState.phase is WorkoutPhase.Countdown) countdownValue else persistentState.reps,
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
    }

    fun onCounterClick() {
        Log.d(tagWorkout, "onCounterClick")
        when (phase) {
            is WorkoutPhase.Initial -> {
                phase = WorkoutPhase.Countdown
            }
            is WorkoutPhase.Countdown -> {
                phase = WorkoutPhase.Paused
            }
            is WorkoutPhase.InProgress -> {
                phase = WorkoutPhase.Paused
            }
            is WorkoutPhase.Paused -> {
                phase = WorkoutPhase.Countdown
            }
            is WorkoutPhase.BetweenSets -> {
                phase = WorkoutPhase.Countdown
            }
        }
    }

    fun onCounterLongClick(): Boolean {
        Log.d(tagWorkout, "onCounterLongClick")
        when (phase) {
            is WorkoutPhase.InProgress-> {
                phase = WorkoutPhase.BetweenSets
                return true
            }

            is WorkoutPhase.Paused -> {
                phase = WorkoutPhase.BetweenSets
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

    private fun onSecondTick() {
        if (persistentState.value.phase is WorkoutPhase.BetweenSets) {
            val interSetClockMillisLong = clock.millis() - persistentState.value.interSetTimerStartedUtc
            val seconds = TimeUnit.MILLISECONDS.toSeconds(interSetClockMillisLong)
            if (seconds > 0 && seconds % beepSeconds == 0L) sounds.beep()
            interSetClockMillis.value = interSetClockMillisLong.toInt()
        }

        if (persistentState.value.phase is WorkoutPhase.Countdown) {
            if (countdownValue.value < 0) {
                countdownValue.value = TimeUnit.MILLISECONDS.toSeconds(exercise.countdownFromMillis.toLong()).toInt()
                return
            }
            if (countdownValue.value == 1) {
                phase = WorkoutPhase.InProgress
                countdownValue.value = countdownEmptyValue
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
                if (exercise.startWithUp) persistentState.update { it.copy(reps = it.reps.inc()) }
            }
            ExercisePhase.Up -> {
                animationTargetState.value = WorkoutAnimationTargetState.Top(exercise.phaseDurationMillis(phase))
                sounds.makeUpSound()
            }
            ExercisePhase.UpperHold -> {
                if (!exercise.startWithUp) persistentState.update { it.copy(reps = it.reps.inc()) }
            }
        }
    }

    companion object {
        private const val beepSeconds = 60
        private const val countdownEmptyValue = Int.MIN_VALUE
        const val animationResetDuration = 200
        const val tagWorkout = "tagworkout"
    }

}
