package hu.kts.cmetronome.workoutlogic

import hu.kts.cmetronome.persistency.Exercise
import hu.kts.cmetronome.timer.ticksToMs
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
