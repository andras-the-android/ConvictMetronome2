package hu.kts.convictmetronome.ui.main

import hu.kts.convictmetronome.core.Sounds

data class AppBarActionState(
    val optionsMenuExpanded: Boolean = false,
    val showConfirmDeleteExerciseDialog: Boolean = false,
    val volumePopupExpanded: Boolean = false,
    val volume: Float = Sounds.maxVolume,
)
