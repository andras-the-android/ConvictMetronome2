package hu.kts.convictmetronome.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import hu.kts.convictmetronome.repository.ExerciseRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val exerciseRepository: ExerciseRepository
): ViewModel() {

    val state = exerciseRepository.allExercises.combine(exerciseRepository.selectedExercise) { exercises, selectedExercise ->
        println()
        MainScreenState.Content(
            exercises = exercises,
            selectedExerciseId = selectedExercise.id,
            title = selectedExercise.name,
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        MainScreenState.Loading
    )

    fun selectExercise(id: Int) {
        exerciseRepository.selectExercise(id)
    }

}
