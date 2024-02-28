package hu.kts.cmetronome.ui.exercise

object ExerciseProperties {

    val exercisePhaseLengthSeconds = listOf(0.5f, 1f, 1.5f, 2f, 2.5f, 3f, 4f, 5f)
    val countdownLengthSeconds = listOf(0f, 2f, 3f, 4f, 5f, 7f)

    fun getExercisePhaseLengthPositionFromSecond(value: Float) =
        exercisePhaseLengthSeconds.indexOf(value)

    fun getCountdownLengthPositionFromSecond(value: Float) = countdownLengthSeconds.indexOf(value)

}
