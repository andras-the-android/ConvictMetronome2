package hu.kts.convictmetronome.ui.exercise

object ExerciseProperties {

    val valuesInSeconds = listOf(0.5f, 1f, 1.5f, 2f, 2.5f, 3f, 4f, 5f)

    fun getPositionFromSecond(value: Float) = valuesInSeconds.indexOf(value)

}
