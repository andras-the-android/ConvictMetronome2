package hu.kts.convictmetronome.ui.exercise

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import hu.kts.convictmetronome.persistency.Exercise
import hu.kts.convictmetronome.repository.ExerciseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExerciseViewModel @Inject constructor(
    private val repository: ExerciseRepository
): ViewModel(), ExerciseSheetCallbacks {

    private val _state = MutableStateFlow<ExerciseSheetState>(ExerciseSheetState.Hidden)
    val state = _state.asStateFlow()

    fun createNewExercise() {
        viewModelScope.launch {
            _state.value = ExerciseSheetState.Showing.fromExercise(Exercise.default.copy(name = ""))
        }
    }

    fun dismissSheet() {
        _state.value = ExerciseSheetState.Hidden
    }


    override fun onNameChange(value: String) {
        _state.update { (it as ExerciseSheetState.Showing).copy(name = value) }
    }

    override fun onStartWithUpChange(value: Boolean) {
        _state.update { (it as ExerciseSheetState.Showing).copy(startWithUp = value) }
    }

    override fun onCountdownFromChange(value: Int) {
        _state.update {
            (it as ExerciseSheetState.Showing)
            .copy(
                countdownFromSeconds = ExerciseProperties.valuesInSeconds[value],
                countdownFromPosition = value
            )
        }
    }

    override fun onDownChange(value: Int) {
        _state.update {
            (it as ExerciseSheetState.Showing)
                .copy(
                    downSeconds = ExerciseProperties.valuesInSeconds[value],
                    downPosition = value
                )
        }
    }

    override fun onLowerHoldChange(value: Int) {
        _state.update {
            (it as ExerciseSheetState.Showing)
                .copy(
                    lowerHoldSeconds = ExerciseProperties.valuesInSeconds[value],
                    lowerHoldPosition = value
                )
        }
    }

    override fun onUpChange(value: Int) {
        _state.update {
            (it as ExerciseSheetState.Showing)
                .copy(
                    upSeconds = ExerciseProperties.valuesInSeconds[value],
                    upPosition = value
                )
        }
    }

    override fun onUpperHoldChange(value: Int) {
        _state.update {
            (it as ExerciseSheetState.Showing)
                .copy(
                    upperHoldSeconds = ExerciseProperties.valuesInSeconds[value],
                    upperHoldPosition = value
                )
        }
    }

    override fun onSaveClicked() {
        repository.saveExercise((_state.value as ExerciseSheetState.Showing).toExercise())
        dismissSheet()
    }

}
