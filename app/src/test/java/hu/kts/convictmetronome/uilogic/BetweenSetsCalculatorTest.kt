package hu.kts.convictmetronome.uilogic

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class BetweenSetsCalculatorTest {

    @Test
    fun getClock() {
        val underTest = BetweenSetsCalculator()
        assertEquals("00:00", underTest.getClock(0))
        assertEquals("00:00", underTest.getClock(1))
        assertEquals("00:01", underTest.getClock(2))
        assertEquals("01:00", underTest.getClock(oneMinuteInTicks))
        assertEquals("00:00", underTest.getClock(oneHourInTicks))
    }

    companion object {
        private const val oneHourInTicks = 7200
        private const val oneMinuteInTicks = 120
    }
}
