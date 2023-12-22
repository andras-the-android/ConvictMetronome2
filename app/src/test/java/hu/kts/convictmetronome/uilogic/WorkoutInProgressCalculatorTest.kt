package hu.kts.convictmetronome.uilogic

import hu.kts.convictmetronome.persistency.Exercise
import hu.kts.convictmetronome.ui.workout.WorkoutSideEffect
import hu.kts.convictmetronome.ui.workout.WorkoutSideEffect.animationDown
import hu.kts.convictmetronome.ui.workout.WorkoutSideEffect.animationUp
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream


class WorkoutInProgressCalculatorTest {

    @ParameterizedTest
    @MethodSource("provideValuesForStartDown")
    fun `start down`(tickCount: Int, expected:  Pair<Int, WorkoutSideEffect?>) {
        val underTest = WorkoutInProgressCalculator()
        assertEquals(
            expected,
            underTest.getCounterAndSideEffect(exerciseStartDown, tickCount),
            "tick #$tickCount"
        )
    }

    @ParameterizedTest
    @MethodSource("provideValuesForStartUp")
    fun `start up`(tickCount: Int, expected:  Pair<Int, WorkoutSideEffect?>) {
        val underTest = WorkoutInProgressCalculator()
        assertEquals(
            expected,
            underTest.getCounterAndSideEffect(exerciseStartUp, tickCount),
            "tick #$tickCount"
        )
    }

    companion object {
        @JvmStatic
        private fun provideValuesForStartDown(): Stream<Arguments?>? {
            return Stream.of(
                // down
                Arguments.of(0, Pair(0, animationDown)),
                Arguments.of(1, Pair(0, null)),
                Arguments.of(2, Pair(0, null)),
                Arguments.of(3, Pair(0, null)),

                // lower hold
                Arguments.of(4, Pair(0, null)),
                Arguments.of(5, Pair(0, null)),

                // up
                Arguments.of(6, Pair(0, animationUp)),
                Arguments.of(7, Pair(0, null)),
                Arguments.of(8, Pair(0, null)),
                Arguments.of(9, Pair(0, null)),
                Arguments.of(10, Pair(0, null)),
                Arguments.of(11, Pair(0, null)),

                // upper hold
                Arguments.of(12, Pair(0, null)),

                // next rep
                Arguments.of(13, Pair(1, animationDown)),
                Arguments.of(14, Pair(1, null)),
                Arguments.of(19, Pair(1, animationUp)),
            )
        }

        @JvmStatic
        private fun provideValuesForStartUp(): Stream<Arguments?>? {
            return Stream.of(
                // up
                Arguments.of(0, Pair(0, animationUp)),
                Arguments.of(1, Pair(0, null)),
                Arguments.of(2, Pair(0, null)),
                Arguments.of(3, Pair(0, null)),
                Arguments.of(4, Pair(0, null)),
                Arguments.of(5, Pair(0, null)),

                // upper hold
                Arguments.of(6, Pair(0, null)),

                // down
                Arguments.of(7, Pair(0, animationDown)),
                Arguments.of(8, Pair(0, null)),
                Arguments.of(9, Pair(0, null)),
                Arguments.of(10, Pair(0, null)),

                // lower hold
                Arguments.of(11, Pair(0, null)),
                Arguments.of(12, Pair(0, null)),

                // next rep
                Arguments.of(13, Pair(1, animationUp)),
                Arguments.of(14, Pair(1, null)),
                Arguments.of(20, Pair(1, animationDown)),
            )
        }

        private val exerciseStartDown = Exercise(
            id = 0,
            name = "test",
            countdownFromMillis = 3000,
            startWithUp = false,
            upMillis = 3000,
            upperHoldMillis = 500,
            downMillis = 2000,
            lowerHoldMillis = 1000
        )

        private val exerciseStartUp = exerciseStartDown.copy(startWithUp =  true)
    }
}

