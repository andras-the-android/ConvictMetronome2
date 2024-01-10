package hu.kts.cmetronome.workoutlogic

import hu.kts.cmetronome.persistency.Exercise
import hu.kts.cmetronome.ui.workout.WorkoutAnimationTargetState
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream


class WorkoutInProgressCalculatorTest {

    @ParameterizedTest
    @MethodSource("provideValuesForStartDown")
    fun `start down`(tickCount: Int, expected:  Pair<Int, WorkoutAnimationTargetState?>) {
        val underTest = WorkoutInProgressCalculator()
        assertEquals(
            expected,
            underTest.getCounterAndAnimationTarget(exerciseStartDown, tickCount),
            "tick #$tickCount"
        )
    }

    @ParameterizedTest
    @MethodSource("provideValuesForStartUp")
    fun `start up`(tickCount: Int, expected:  Pair<Int, WorkoutAnimationTargetState?>) {
        val underTest = WorkoutInProgressCalculator()
        assertEquals(
            expected,
            underTest.getCounterAndAnimationTarget(exerciseStartUp, tickCount),
            "tick #$tickCount"
        )
    }

    @Test
    fun testRemoveLatestRepFromTicks() {
        val underTest = WorkoutInProgressCalculator()
        assertEquals(0, underTest.removeLatestRepFromTicks(exerciseStartDown, 6))
        assertEquals(0, underTest.removeLatestRepFromTicks(exerciseStartDown, 12))
        assertEquals(13, underTest.removeLatestRepFromTicks(exerciseStartDown, 13))
        assertEquals(13, underTest.removeLatestRepFromTicks(exerciseStartDown, 14))
    }

    companion object {
        @JvmStatic
        private fun provideValuesForStartDown(): Stream<Arguments?>? {
            return Stream.of(
                // down
                Arguments.of(0, Pair(0, animationTargetStateBottom)),
                Arguments.of(1, Pair(0, animationTargetStateBottom)),
                Arguments.of(2, Pair(0, animationTargetStateBottom)),
                Arguments.of(3, Pair(0, animationTargetStateBottom)),

                // lower hold
                Arguments.of(4, Pair(0, animationTargetStateBottom)),
                Arguments.of(5, Pair(0, animationTargetStateBottom)),

                // up
                Arguments.of(6, Pair(0, animationTargetStateTop)),
                Arguments.of(7, Pair(0, animationTargetStateTop)),
                Arguments.of(8, Pair(0, animationTargetStateTop)),
                Arguments.of(9, Pair(0, animationTargetStateTop)),
                Arguments.of(10, Pair(0, animationTargetStateTop)),
                Arguments.of(11, Pair(0, animationTargetStateTop)),

                // upper hold
                Arguments.of(12, Pair(1, animationTargetStateTop)),

                // next rep
                Arguments.of(13, Pair(1, animationTargetStateBottom)),
                Arguments.of(14, Pair(1, animationTargetStateBottom)),
                Arguments.of(19, Pair(1, animationTargetStateTop)),
            )
        }

        @JvmStatic
        private fun provideValuesForStartUp(): Stream<Arguments?>? {
            return Stream.of(
                // up
                Arguments.of(0, Pair(0, animationTargetStateTop)),
                Arguments.of(1, Pair(0, animationTargetStateTop)),
                Arguments.of(2, Pair(0, animationTargetStateTop)),
                Arguments.of(3, Pair(0, animationTargetStateTop)),
                Arguments.of(4, Pair(0, animationTargetStateTop)),
                Arguments.of(5, Pair(0, animationTargetStateTop)),

                // upper hold
                Arguments.of(6, Pair(0, animationTargetStateTop)),

                // down
                Arguments.of(7, Pair(0, animationTargetStateBottom)),
                Arguments.of(8, Pair(0, animationTargetStateBottom)),
                Arguments.of(9, Pair(0, animationTargetStateBottom)),
                Arguments.of(10, Pair(0, animationTargetStateBottom)),

                // lower hold
                Arguments.of(11, Pair(1, animationTargetStateBottom)),
                Arguments.of(12, Pair(1, animationTargetStateBottom)),

                // next rep
                Arguments.of(13, Pair(1, animationTargetStateTop)),
                Arguments.of(14, Pair(1, animationTargetStateTop)),
                Arguments.of(20, Pair(1, animationTargetStateBottom)),
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

        private val animationTargetStateTop = WorkoutAnimationTargetState.Top(exerciseStartDown.upMillis)
        private val animationTargetStateBottom = WorkoutAnimationTargetState.Bottom(exerciseStartDown.downMillis)
    }
}

