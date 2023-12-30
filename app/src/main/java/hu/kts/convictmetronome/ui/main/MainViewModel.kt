package hu.kts.convictmetronome.ui.main

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import hu.kts.convictmetronome.repository.ExerciseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val exerciseRepository: ExerciseRepository
): ViewModel(), AppBarActionCallbacks {

    private val actionState = MutableStateFlow(AppBarActionState())

    val state = combine(
        exerciseRepository.allExercises,
        exerciseRepository.selectedExercise,
        actionState
    ) { exercises, selectedExercise, actionState ->
        Log.d("tagmain", "${exercises.count()} exercises, ${selectedExercise.name} selected, action state: $actionState")
        MainScreenState.Content(
            exercises = exercises,
            selectedExerciseId = selectedExercise.id,
            title = selectedExercise.name,
            optionsMenuExpanded = actionState.optionsMenuExpanded,
            showConfirmDeleteExerciseDialog = actionState.showConfirmDeleteExerciseDialog
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        MainScreenState.Loading
    )

    fun selectExercise(id: Int) {
        exerciseRepository.selectExercise(id)
    }

    override fun onOptionsActionClicked() {
        actionState.update { it.copy(optionsMenuExpanded = it.optionsMenuExpanded.not()) }
    }

    override fun dismissOptionsMenu() {
        actionState.update { it.copy(optionsMenuExpanded = false) }
    }

    override fun onDeleteExerciseClicked() {
        actionState.update { it.copy(optionsMenuExpanded = false, showConfirmDeleteExerciseDialog = true) }
    }

    override fun onConfirmDeleteExercise() {
        dismissConfirmDeleteExerciseDialog()
        exerciseRepository.deleteSelectedExercise()
    }

    override fun dismissConfirmDeleteExerciseDialog() {
        actionState.update { it.copy(showConfirmDeleteExerciseDialog = false) }
    }

}
