package hu.kts.cmetronome.workoutlogic

import android.media.AudioTrack
import android.util.Log
import app.cash.turbine.test
import hu.kts.cmetronome.persistency.Exercise
import hu.kts.cmetronome.sounds.Sounds
import hu.kts.cmetronome.timer.ExercisePhase
import hu.kts.cmetronome.timer.ExerciseTimer
import hu.kts.cmetronome.timer.SecondsTimer
import hu.kts.cmetronome.ui.workout.WorkoutAnimationTargetState.Bottom
import hu.kts.cmetronome.ui.workout.WorkoutAnimationTargetState.Top
import hu.kts.cmetronome.ui.workout.WorkoutPhase.Countdown
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.runs
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Clock

@OptIn(ExperimentalCoroutinesApi::class)
class WorkoutTest {

    private val secondsTimerMock = mockk<SecondsTimer>()
    private val secondsTimerFlow = MutableSharedFlow<Unit>()
    private val exerciseTimerMock = mockk<ExerciseTimer>()
    private val exerciseTimerFlow = MutableSharedFlow<ExercisePhase>()
    private lateinit var soundsMock: Sounds
    private val coroutineScope = CoroutineScope(UnconfinedTestDispatcher())
    private val clockMock = mockk<Clock>()

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

    private val exerciseStartUp = exerciseStartDown.copy(startWithUp = true)

    private val animationTargetStateTop = Top(exerciseStartDown.upMillis)
    private val animationTargetStateBottom = Bottom(exerciseStartDown.downMillis)

    private lateinit var underTest: Workout

    @BeforeEach
    fun setUp() {
        every { secondsTimerMock.tickFlow } returns secondsTimerFlow
        every { secondsTimerMock.start() } just runs
        every { secondsTimerMock.stop() } just runs
        every { exerciseTimerMock.eventFlow } returns exerciseTimerFlow
        every { exerciseTimerMock.start(any()) } just runs
        every { exerciseTimerMock.stop() } just runs

        mockkStatic(AudioTrack::class)
        every { AudioTrack.getMaxVolume() } returns 1.0f
        soundsMock = mockk<Sounds>()

        mockLog()

        underTest = Workout(
            secondsTimer = secondsTimerMock,
            exerciseTimer = exerciseTimerMock,
            sounds = soundsMock,
            exercise = exerciseStartDown,
            coroutineScope = coroutineScope,
            clock = clockMock,
            savedState = WorkoutPersistentState()
        )
    }

    @AfterEach
    fun tearDown() {
    }

    @Test
    fun countdown() = runTest {
        underTest.state.test {
            awaitItem() // consume the initial state
            underTest.onCounterClick()
            var nextExpectedState = WorkoutState(
                animationTargetState = Top(200),
                repCounter = 0,
                interSetClockMillis = null,
                completedSets = 0,
                phase = Countdown
            )
            assertEquals(nextExpectedState, awaitItem())

            secondsTimerFlow.emit(Unit)
            nextExpectedState = nextExpectedState.copy(repCounter = 3)
            assertEquals(nextExpectedState, awaitItem())

            secondsTimerFlow.emit(Unit)
            nextExpectedState = nextExpectedState.copy(repCounter = 2)
            assertEquals(nextExpectedState, awaitItem())

            secondsTimerFlow.emit(Unit)
            nextExpectedState = nextExpectedState.copy(repCounter = 1)
            assertEquals(nextExpectedState, awaitItem())
        }
    }

    private fun mockLog() {
        mockkStatic(Log::class)
        every { Log.v(any(), any()) } returns 0
        every { Log.d(any(), any()) } returns 0
        every { Log.i(any(), any()) } returns 0
        every { Log.e(any(), any()) } returns 0
    }

}
