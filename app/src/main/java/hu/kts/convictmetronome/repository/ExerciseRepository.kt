package hu.kts.convictmetronome.repository

import hu.kts.convictmetronome.persistency.Exercise
import hu.kts.convictmetronome.persistency.ExerciseDao
import hu.kts.convictmetronome.persistency.Preferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExerciseRepository @Inject constructor(
    private val dao: ExerciseDao,
    private val preferences: Preferences,
    private val coroutineScope: CoroutineScope
) {

    private val _selectedExercise = MutableStateFlow(Exercise.empty)
    val selectedExercise = _selectedExercise.asStateFlow()

    val allExercises = dao.getAll().map { it.ifEmpty { listOf(Exercise.default) } }

    init {
        coroutineScope.launch {
            loadSelectedFromDatabase()
        }
    }

    suspend fun getById(id: Int) = dao.getById(id) ?: Exercise.default

    fun selectExercise(id: Int) {
        coroutineScope.launch {
            preferences.selectedExerciseId = id
            loadSelectedFromDatabase()
        }
    }

    fun saveExercise(exercise: Exercise) {
        coroutineScope.launch {
            val id = dao.upsert(exercise).toInt()
            if (id > 0) { // update returns -1 id
                preferences.selectedExerciseId = id
            }
            loadSelectedFromDatabase()
        }
    }

    private suspend fun loadSelectedFromDatabase() {
        _selectedExercise.value = dao.getById(preferences.selectedExerciseId) ?: Exercise.default
    }
}
