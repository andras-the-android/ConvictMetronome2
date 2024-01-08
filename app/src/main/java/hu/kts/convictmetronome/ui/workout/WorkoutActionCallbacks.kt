package hu.kts.convictmetronome.ui.workout

interface WorkoutActionCallbacks {

    fun onClick()
    fun onLongClick(eventConsumed: () -> Unit)
    fun confirmReset()
    fun dismissConfirmResetDialog()
}
