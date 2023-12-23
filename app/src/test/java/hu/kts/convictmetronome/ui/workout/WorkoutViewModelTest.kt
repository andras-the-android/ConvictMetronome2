package hu.kts.convictmetronome.ui.workout

import app.cash.turbine.test
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
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
    private val sounds = mockk<Sounds>()
    private val exerciseRepository = mockk<ExerciseRepository>()
    private val countdownCalculator = CountdownCalculator()
    private val inProgressCalculator = WorkoutInProgressCalculator()
    private val betweenSetsCalculator = BetweenSetsCalculator()
    private val exerciseFlow = MutableStateFlow(Exercise.default)
    private val tickFlow = MutableSharedFlow<Unit>()
    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var underTest: WorkoutViewModel

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { exerciseRepository.selectedExercise } returns exerciseFlow
        every { tickProvider.tickFlow } returns tickFlow
        every { tickProvider.start() } just runs
        every { tickProvider.stop() } just runs

        underTest = WorkoutViewModel(tickProvider, sounds, exerciseRepository, countdownCalculator, inProgressCalculator, betweenSetsCalculator)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun test() = runTest {
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

    // the parameter doesn't do anything but improves test readability
    private suspend fun tick(count: Int) {
        tickFlow.emit(Unit)
    }
}
