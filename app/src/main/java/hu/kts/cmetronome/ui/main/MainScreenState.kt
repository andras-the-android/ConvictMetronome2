package hu.kts.cmetronome.ui.main

import androidx.compose.ui.text.AnnotatedString
import hu.kts.cmetronome.persistency.Exercise


sealed interface MainScreenState {
    data object Loading: MainScreenState
    data class Content(
        val title: String,
        val exercises: List<Exercise>,
        val selectedExerciseId: Int,
        val optionsMenuExpanded: Boolean,
        val showConfirmDeleteExerciseDialog: Boolean,
        val whatsNewDialogContent: AnnotatedString?,
        val volumePopupExpanded: Boolean,
        val upDownVolume: Float,
        val speechVolume: Float,
    ): MainScreenState {

        val deleteEnabled
            get() = exercises.count() > 1
    }
}
