package hu.kts.cmetronome.ui.main

import hu.kts.cmetronome.sounds.Sounds


data class AppBarActionState(
    val optionsMenuExpanded: Boolean = false,
    val showConfirmDeleteExerciseDialog: Boolean = false,
    val volumePopupExpanded: Boolean = false,
    val upDownVolume: Float = Sounds.maxVolume,
    val speechVolume: Float = Sounds.maxVolume,
)
