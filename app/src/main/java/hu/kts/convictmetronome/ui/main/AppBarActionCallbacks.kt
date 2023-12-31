package hu.kts.convictmetronome.ui.main

import androidx.compose.runtime.Stable

@Stable
interface AppBarActionCallbacks {

    fun onOptionsActionClicked()
    fun dismissOptionsMenu()

    fun onDeleteExerciseClicked()
    fun onConfirmDeleteExercise()
    fun dismissConfirmDeleteExerciseDialog()

    fun onVolumeActionClicked()
    fun dismissVolumePopup()
    fun onVolumeChange(value: Float)
}
