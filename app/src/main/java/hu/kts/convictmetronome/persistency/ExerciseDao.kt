package hu.kts.convictmetronome.persistency

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface ExerciseDao {

    @Query("SELECT * FROM exercise")
    fun getAll(): Flow<List<Exercise>>

    @Query("SELECT * FROM exercise WHERE id == :id")
    suspend fun getById(id: Int): Exercise?

    @Upsert
    suspend fun upsert(exercise: Exercise): Long
}
