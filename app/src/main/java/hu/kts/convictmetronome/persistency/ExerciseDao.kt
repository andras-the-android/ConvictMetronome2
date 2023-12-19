package hu.kts.convictmetronome.persistency

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface ExerciseDao {

    @Query("SELECT * FROM exercise")
    suspend fun getAll(): List<Exercise>

    @Insert
    suspend fun insertAll(exercise: Exercise)
}