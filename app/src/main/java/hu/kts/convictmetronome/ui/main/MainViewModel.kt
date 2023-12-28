package hu.kts.convictmetronome.ui.main

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import hu.kts.convictmetronome.persistency.Exercise
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(): ViewModel() {

    private val _state = MutableStateFlow<MainScreenState>(MainScreenState.Loading)
    val state = _state.asStateFlow()

    init {
        _state.value = MainScreenState.Content(
            exercises = listOf(
                Exercise.default.copy(id = 0, name = "Push up"),
                Exercise.default.copy(id = 1, name = "Pull up"),
                Exercise.default.copy(id = 2, name = "Squat"),
            ),
            selectedExerciseId = 1,
            title = "Pull up"
        )
    }



}
