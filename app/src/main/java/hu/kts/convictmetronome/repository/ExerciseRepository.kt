package hu.kts.convictmetronome.repository

import android.util.Log
import hu.kts.convictmetronome.persistency.Exercise
import hu.kts.convictmetronome.persistency.ExerciseDao
import hu.kts.convictmetronome.persistency.Preferences
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

class ExerciseRepository @Inject constructor(
    private val dao: ExerciseDao,
    private val preferences: Preferences,
    private val coroutineScope: CoroutineScope
) {

    private val _selectedExercise = MutableStateFlow(Exercise.empty)
    val selectedExercise = _selectedExercise.asStateFlow()

    val allExercises = dao.getAll().map { it.ifEmpty { listOf(Exercise.default) } }

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Log.e("tagpersitency", throwable.message ?: "unknown error")
    }

    init {
        coroutineScope.launch(exceptionHandler) {
            loadSelectedFromDatabase()
        }
    }

    suspend fun getById(id: Int) = dao.getById(id) ?: Exercise.default

    fun selectExercise(id: Int) {
        coroutineScope.launch(exceptionHandler) {
            preferences.selectedExerciseId = id
            loadSelectedFromDatabase()
        }
    }

    fun saveExercise(exercise: Exercise) {
        coroutineScope.launch(exceptionHandler) {
            val id = dao.upsert(exercise).toInt()
            preferences.selectedExerciseId = id
            loadSelectedFromDatabase()
        }
    }

    private suspend fun loadSelectedFromDatabase() {
        _selectedExercise.value = dao.getById(preferences.selectedExerciseId) ?: Exercise.default
    }



}
