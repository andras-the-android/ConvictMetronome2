package hu.kts.cmetronome.timer

enum class ExercisePhase {
    Down {
        override fun next() = LowerHold
    },
    LowerHold {
        override fun next() = Up
    },
    Up {
        override fun next() = UpperHold
    },
    UpperHold {
        override fun next() = Down
    };

    abstract fun next(): ExercisePhase
}
