package hu.kts.convictmetronome.ui.exercise

data class ExerciseScreenState(
    val name: String,

    val countdownFromSeconds: Float,
    val countdownFromPosition: Int,
    val countdownFromPositions: Int,

    val startWithUp: Boolean,

    val downSeconds: Float,
    val downPosition: Int,
    val downPositions: Int,

    val lowerHoldSeconds: Float,
    val lowerHoldPosition: Int,
    val lowerHoldPositions: Int,

    val upSeconds: Float,
    val upPosition: Int,
    val upPositions: Int,

    val upperHoldSeconds: Float,
    val upperHoldPosition: Int,
    val upperHoldPositions: Int,
)
