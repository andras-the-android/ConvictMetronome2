package hu.kts.cmetronome.timer

import hu.kts.cmetronome.persistency.Exercise
import hu.kts.cmetronome.persistency.initialPhase
import hu.kts.cmetronome.persistency.phaseDurationMillis
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

class ExerciseTimer @Inject constructor(
    private val coroutineScope: CoroutineScope
) {

    private val _eventFlow = MutableSharedFlow<ExercisePhase>(replay = 0)
    val eventFlow: SharedFlow<ExercisePhase> = _eventFlow

    private var job: Job? = null

    fun start(exercise: Exercise) {
        stop()
        job = coroutineScope.launch {
            var phase = exercise.initialPhase()
            while (isActive) {
                _eventFlow.emit(phase)
                delay(exercise.phaseDurationMillis(phase).toLong())
                phase = phase.next()
            }
        }
    }

    fun stop() {
        job?.cancel()
    }

}
