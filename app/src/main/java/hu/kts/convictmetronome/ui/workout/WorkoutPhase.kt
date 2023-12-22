package hu.kts.convictmetronome.ui.workout

sealed interface WorkoutPhase {
    data object Initial: WorkoutPhase {
        override fun inc() = this
    }

    data class Countdown(val ticks: Int = initialClick): WorkoutPhase {
        override fun inc() = copy(ticks = ticks + 1)
    }
    data class InProgress(val ticks: Int = initialClick): WorkoutPhase {
        override fun inc() = copy(ticks = ticks + 1)
    }
    data object Paused: WorkoutPhase {
        override fun inc() = this
    }
    data class BetweenSets(val ticks: Int = initialClick): WorkoutPhase {
        override fun inc() = copy(ticks = ticks + 1)
    }

    operator fun inc(): WorkoutPhase

    companion object {
        private const val initialClick = -1 // -1 to let the 0th tick increase it to 0
    }
}
