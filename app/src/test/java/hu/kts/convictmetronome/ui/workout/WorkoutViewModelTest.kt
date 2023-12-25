package hu.kts.convictmetronome.ui.workout

import app.cash.turbine.test
import app.cash.turbine.turbineScope
import hu.kts.convictmetronome.core.Sounds
import hu.kts.convictmetronome.core.TickProvider
import hu.kts.convictmetronome.persistency.Exercise
import hu.kts.convictmetronome.repository.ExerciseRepository
import hu.kts.convictmetronome.uilogic.BetweenSetsCalculator
import hu.kts.convictmetronome.uilogic.CountdownCalculator
import hu.kts.convictmetronome.uilogic.WorkoutInProgressCalculator
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class WorkoutViewModelTest {

    private val tickProvider = mockk<TickProvider>()
    private val sounds = mockk<Sounds>(relaxed = true)
    private val exerciseRepository = mockk<ExerciseRepository>()
    private val countdownCalculator = CountdownCalculator()
    private val inProgressCalculator = WorkoutInProgressCalculator()
    private val betweenSetsCalculator = BetweenSetsCalculator()
    private val exerciseFlow = MutableStateFlow(Exercise.default)
    private val tickFlow = MutableSharedFlow<Unit>()
    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var underTest: WorkoutViewModel
    private lateinit var sideEffectScope: CoroutineScope
    private lateinit var sideEffectFlow: Flow<WorkoutSideEffect>


    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { exerciseRepository.selectedExercise } returns exerciseFlow
        every { tickProvider.tickFlow } returns tickFlow
        every { tickProvider.start() } just runs
        every { tickProvider.stop() } just runs

        underTest = WorkoutViewModel(tickProvider, sounds, exerciseRepository, countdownCalculator, inProgressCalculator, betweenSetsCalculator)
        // we need this workaround because turbine unable to test a SharedFlow with 0 replay
        // TODO find a cleaner solution
        sideEffectScope = CoroutineScope(Dispatchers.Main)
        sideEffectFlow = underTest.sideEffect.shareIn(
            sideEffectScope,
            SharingStarted.Eagerly,
            replay = 1
        )
    }

    @AfterEach
    fun tearDown() {
        sideEffectScope.cancel()
        Dispatchers.resetMain()
    }

    @Test
    fun countdown() = runTest {
        underTest.state.test {
            awaitItem() // consume the initial state
            underTest.onCounterClick()
            tick(0)
            assertEquals(WorkoutScreenState.Content(3), awaitItem())
            tick(1)
            tick(2)
            assertEquals(WorkoutScreenState.Content(2), awaitItem())
            tick(3)
            tick(4)
            assertEquals(WorkoutScreenState.Content(1), awaitItem())
        }
    }

    @Test
    fun `countdown interrupted by pause and then restart`() = runTest {
        underTest.state.test {
            awaitItem() // consume the initial state
            underTest.onCounterClick() // start
            tickRange(0, 4)
            skipItems(2)
            underTest.onCounterClick() // pause
            underTest.onCounterClick() // restart
            tick(0)
            assertEquals(WorkoutScreenState.Content(3), awaitItem())
            tick(1)
            tick(2)
            assertEquals(WorkoutScreenState.Content(2), awaitItem())
            tick(3)
            tick(4)
            assertEquals(WorkoutScreenState.Content(1), awaitItem())
        }
    }

    @Test
    fun `workout in progress`() = runTest {
        underTest.onCounterClick()
        tickRange(0, 6) // countdown
        turbineScope {
            tick(6)
            val stateReceiver = underTest.state.testIn(backgroundScope)
            val sideEffectReceiver = sideEffectFlow.testIn(backgroundScope)
            assertEquals(WorkoutScreenState.Content(), stateReceiver.awaitItem())
            assertEquals(WorkoutSideEffect.animationDown, sideEffectReceiver.awaitItem())
            tickRange(7, 6) // half range
            assertEquals(WorkoutSideEffect.animationUp, sideEffectReceiver.awaitItem())
            tickRange(13, 6) // half range
            assertEquals(WorkoutScreenState.Content(repCounter = 1), stateReceiver.awaitItem())
            assertEquals(WorkoutSideEffect.animationDown, sideEffectReceiver.awaitItem())
            stateReceiver.expectNoEvents()
            sideEffectReceiver.expectNoEvents()
        }
        verify(exactly = 1) { sounds.makeUpSound() }
        verify(exactly = 2) { sounds.makeDownSound() }
    }

    @Test
    fun `workout in progress interrupted by pause and then restart`() = runTest {
        underTest.onCounterClick()
        tickRange(0, 40) // countdown + 2 rep + 3 tick
        underTest.state.test {
            assertEquals(WorkoutScreenState.Content(repCounter = 2), awaitItem())
            underTest.onCounterClick()
            underTest.onCounterClick()
            // countdown
            tick(0)
            assertEquals(WorkoutScreenState.Content(3), awaitItem())
            tick(1)
            tick(2)
            assertEquals(WorkoutScreenState.Content(2), awaitItem())
            tick(3)
            tick(4)
            assertEquals(WorkoutScreenState.Content(1), awaitItem())
            tick(5)
            tick(6)

            // set continues from rep 2
            assertEquals(WorkoutScreenState.Content(repCounter = 2), awaitItem())
            tickRange(7, 10)
            expectNoEvents()
            tick(17)
            assertEquals(WorkoutScreenState.Content(repCounter = 3), awaitItem())
        }
    }

    @Test
    fun `complete a set and then start the next one`() = runTest {
        underTest.onCounterClick()
        tickRange(0, 40) // countdown + 2 rep + 3 tick
        underTest.state.test {
            assertEquals(WorkoutScreenState.Content(repCounter = 2), awaitItem())
            underTest.onCounterLongClick()
            // between sets
            tick(0)
            assertEquals(WorkoutScreenState.Content(repCounter = 2, interSetClock = "00:00", completedSets = 1), awaitItem())
            tickRange(1, 120)
            skipItems(59)
            assertEquals(WorkoutScreenState.Content(repCounter = 2, interSetClock = "01:00", completedSets = 1), awaitItem())
            underTest.onCounterClick()
            // countdown
            tick(0)
            assertEquals(WorkoutScreenState.Content(3, completedSets = 1), awaitItem())
            tick(1)
            tick(2)
            assertEquals(WorkoutScreenState.Content(2, completedSets = 1), awaitItem())
            tick(3)
            tick(4)
            assertEquals(WorkoutScreenState.Content(1, completedSets = 1), awaitItem())
            tick(5)
            tick(6)

            // set continues from rep 2
            assertEquals(WorkoutScreenState.Content(repCounter = 0, completedSets = 1), awaitItem())
            tickRange(7, 11)
            expectNoEvents()
            tick(18)
            assertEquals(WorkoutScreenState.Content(repCounter = 1, completedSets = 1), awaitItem())
        }
        verify(exactly = 1) { sounds.beep() }
    }

    @Test
    fun `complete a workout and then start a new one`() = runTest {
        underTest.onCounterClick()
        tickRange(0, 40) // countdown + 2 rep + 3 tick
        underTest.state.test {
            assertEquals(WorkoutScreenState.Content(repCounter = 2), awaitItem())
            underTest.onCounterLongClick()
            underTest.onCounterLongClick()
            assertEquals(WorkoutScreenState.Content(), awaitItem())
        }
    }



    private suspend fun tickRange(start: Int, times: Int) {
        for (i in 0 ..< times) {
            tick(start + i)
        }
    }

    // the parameter doesn't do anything but improves test readability
    private suspend fun tick(count: Int) {
        tickFlow.emit(Unit)
    }
}
