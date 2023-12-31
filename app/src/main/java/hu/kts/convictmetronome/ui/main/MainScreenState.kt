package hu.kts.convictmetronome.ui.main

import hu.kts.convictmetronome.persistency.Exercise

sealed interface MainScreenState {
    data object Loading: MainScreenState
    data class Content(
        val title: String,
        val exercises: List<Exercise>,
        val selectedExerciseId: Int,
        val optionsMenuExpanded: Boolean,
        val showConfirmDeleteExerciseDialog: Boolean,
        val volumePopupExpanded: Boolean,
        val volume: Float,
    ): MainScreenState {

        val deleteEnabled
            get() = exercises.count() > 1
    }
}
