package hu.kts.cmetronome.ui.exercise

import hu.kts.cmetronome.persistency.Exercise
import hu.kts.cmetronome.ui.millisToSecs
import hu.kts.cmetronome.ui.secsToMillis

sealed interface ExerciseSheetState {
    data object Hidden: ExerciseSheetState

    data class Showing(
        val id: Int,
        val name: String,

        val countdownFromSeconds: Float,
        val countdownFromPosition: Int,

        val startWithUp: Boolean,

        val downSeconds: Float,
        val downPosition: Int,

        val lowerHoldSeconds: Float,
        val lowerHoldPosition: Int,

        val upSeconds: Float,
        val upPosition: Int,

        val upperHoldSeconds: Float,
        val upperHoldPosition: Int,
    ): ExerciseSheetState {


        fun toExercise(): Exercise {
            return this.run {
                Exercise(
                    id = id,
                    name = name,
                    countdownFromMillis = countdownFromSeconds.secsToMillis(),
                    startWithUp = startWithUp,
                    downMillis = downSeconds.secsToMillis(),
                    lowerHoldMillis = lowerHoldSeconds.secsToMillis(),
                    upMillis = upSeconds.secsToMillis(),
                    upperHoldMillis = upperHoldSeconds.secsToMillis()
                )
            }
        }

        companion object {

            fun fromExercise(exercise: Exercise): Showing {
                return exercise.run {
                    Showing(
                        id = id,
                        name = name,
                        countdownFromSeconds = countdownFromMillis.millisToSecs(),
                        countdownFromPosition = ExerciseProperties.getPositionFromSecond(countdownFromMillis.millisToSecs()),
                        startWithUp = startWithUp,
                        downSeconds = downMillis.millisToSecs(),
                        downPosition = ExerciseProperties.getPositionFromSecond(downMillis.millisToSecs()),
                        lowerHoldSeconds = lowerHoldMillis.millisToSecs(),
                        lowerHoldPosition = ExerciseProperties.getPositionFromSecond(lowerHoldMillis.millisToSecs()),
                        upSeconds = upMillis.millisToSecs(),
                        upPosition = ExerciseProperties.getPositionFromSecond(upMillis.millisToSecs()),
                        upperHoldSeconds = upperHoldMillis.millisToSecs(),
                        upperHoldPosition = ExerciseProperties.getPositionFromSecond(upperHoldMillis.millisToSecs()),

                    )
                }

            }

        }
    }
}
