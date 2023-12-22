package hu.kts.convictmetronome.repository

import hu.kts.convictmetronome.persistency.Exercise
import hu.kts.convictmetronome.persistency.ExerciseDao
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

class ExerciseRepository @Inject constructor(
    exerciseDao: ExerciseDao
) {

    val selectedExercise = flowOf(Exercise.default)

}
