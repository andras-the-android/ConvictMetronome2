package hu.kts.convictmetronome.uilogic

import hu.kts.convictmetronome.core.ticksToMs
import hu.kts.convictmetronome.persistency.Exercise
import javax.inject.Inject
import kotlin.math.roundToInt

class CountdownCalculator @Inject constructor() {

    fun getCounter(exercise: Exercise, tickCount: Int): Int {
        val timeLeft = exercise.countdownFromMillis - tickCount.ticksToMs()
        return (timeLeft / msToSecRate).roundToInt() // Can't use TimeUnit unit, because it doesn't round
    }

    companion object {
        private const val msToSecRate = 1000.0
    }


}
