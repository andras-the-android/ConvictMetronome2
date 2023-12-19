package hu.kts.convictmetronome.repository

import hu.kts.convictmetronome.persistency.Exercise
import hu.kts.convictmetronome.persistency.ExerciseDao
import javax.inject.Inject

class ExerciseRepository @Inject constructor(
    exerciseDao: ExerciseDao
) {

    val selectedExercise = Exercise.default

}