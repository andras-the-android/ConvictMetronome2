package hu.kts.cmetronome.ui.main

import hu.kts.cmetronome.sounds.Sounds


data class AppBarActionState(
    val optionsMenuExpanded: Boolean = false,
    val showConfirmDeleteExerciseDialog: Boolean = false,
    val volumePopupExpanded: Boolean = false,
    val volume: Float = Sounds.maxVolume,
)
