package hu.kts.convictmetronome.ui.workout

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import hu.kts.convictmetronome.core.Sounds
import hu.kts.convictmetronome.core.Timer
import hu.kts.convictmetronome.repository.ExerciseRepository
import javax.inject.Inject

@HiltViewModel
class WorkoutViewModel @Inject constructor(
    private val timer: Timer,
    private val sounds: Sounds,
    private val exerciseRepository: ExerciseRepository,
): ViewModel() {

}