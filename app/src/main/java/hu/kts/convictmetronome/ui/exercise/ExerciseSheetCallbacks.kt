package hu.kts.convictmetronome.ui.exercise

interface ExerciseSheetCallbacks {
    fun onNameChange(value: String)
    fun onStartWithUpChange(value: Boolean)
    fun onCountdownFromChange(value: Int)
    fun onDownChange(value: Int)
    fun onLowerHoldChange(value: Int)
    fun onUpChange(value: Int)
    fun onUpperHoldChange(value: Int)
    fun onSaveClicked()
}
