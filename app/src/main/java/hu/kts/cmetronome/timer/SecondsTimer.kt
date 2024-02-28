package hu.kts.cmetronome.timer

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import java.util.Timer
import java.util.TimerTask
import javax.inject.Inject

class SecondsTimer @Inject constructor(
    private val coroutineScope: CoroutineScope
) {

    private val _tickFlow = MutableSharedFlow<Unit>(replay = 0)
    val tickFlow: SharedFlow<Unit> = _tickFlow

    private var task: TimerTask? = null
    private val timer = Timer()

    fun start() {
        stop()
        task = object : TimerTask() {
            override fun run() {
                coroutineScope.launch {
                    _tickFlow.emit(Unit)
                }
            }
        }
        timer.schedule(task, tickPeriod, tickPeriod)
    }

    fun stop() {
        task?.cancel()
    }

}

const val tickPeriod = 1000L
