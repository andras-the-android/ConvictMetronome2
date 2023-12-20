package hu.kts.convictmetronome.uilogic

import hu.kts.convictmetronome.persistency.Exercise
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test


class CountdownCalculatorTest {

    @Test
    fun getCounter() {
        val underTest = CountdownCalculator()
        assertEquals(3, underTest.getCounter(Exercise.default, 0))
        assertEquals(3, underTest.getCounter(Exercise.default, 1))
        assertEquals(2, underTest.getCounter(Exercise.default, 2))
        assertEquals(2, underTest.getCounter(Exercise.default, 3))
        assertEquals(1, underTest.getCounter(Exercise.default, 4))
        assertEquals(1, underTest.getCounter(Exercise.default, 5))
        assertEquals(0, underTest.getCounter(Exercise.default, 6))

    }
}
