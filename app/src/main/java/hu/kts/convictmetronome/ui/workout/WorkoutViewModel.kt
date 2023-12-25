package hu.kts.convictmetronome.ui.workout

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
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
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

    private val _sideEffect = MutableSharedFlow<WorkoutSideEffect>()
    val sideEffect = _sideEffect.asSharedFlow()

    init {
        viewModelScope.launch {
            tickProvider.tickFlow.collect { onTick() }
        }
        viewModelScope.launch {
            exerciseRepository.selectedExercise.collect { exercise ->
                initWorkout(exercise)
            }
        }
    }

    private fun initWorkout(exercise: Exercise) {
        tickProvider.stop()
        phase = Initial
        this.exercise = exercise
        _state.value = WorkoutScreenState.Content()
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
        val localePhase = phase
        if (localePhase is InProgress) {
            sounds.stop()
            phase = BetweenSets()
        }
        return false
    }

    private suspend fun onTick() {
        when (val localPhase = ++phase) {

            is Initial -> throw IllegalStateException("Tick provider should not run when state is initial")

            is Countdown -> {
                val repCounter = countdownCalculator.getCounter(exercise, localPhase.ticks)
                if (repCounter > 0) {
                    _state.update { (it as WorkoutScreenState.Content).copy(repCounter = repCounter, interSetClock = "") }
                } else {
                    phase = InProgress(localPhase.ticksFromPreviousPhase)
                    onTick()
                }
            }

            is InProgress -> {
                val (repCounter, sideEffect) = workoutInProgressCalculator.getCounterAndSideEffect(exercise, localPhase.ticks)
                _state.update { (it as WorkoutScreenState.Content).copy(repCounter = repCounter) }
                sideEffect?.let {
                    when (it) {
                        WorkoutSideEffect.animationUp -> sounds.makeUpSound()
                        WorkoutSideEffect.animationDown -> sounds.makeDownSound()
                    }
                    _sideEffect.emit(it)
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

    companion object {
        private val beepTicks = TimeUnit.MINUTES.toMillis(1).toInt() / tickPeriod
    }

}
