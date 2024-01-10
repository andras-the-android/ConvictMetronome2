package hu.kts.cmetronome.core

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import java.util.Timer
import java.util.TimerTask
import javax.inject.Inject

class TickProvider @Inject constructor(
    private val coroutineScope: CoroutineScope
) {

    private val _tickFlow = MutableSharedFlow<Unit>(replay = 0)
    val tickFlow: SharedFlow<Unit> = _tickFlow

    private var task: TimerTask? = null
    private val timer = Timer()

    fun start() {
        task = object : TimerTask() {
            override fun run() {
                coroutineScope.launch {
                    _tickFlow.emit(Unit)
                }
            }
        }
        timer.schedule(task, 0, tickPeriod.toLong())
    }

    fun stop() {
        task?.cancel()
    }

}

const val tickPeriod = 500

fun Int.ticksToMs() = this * tickPeriod
