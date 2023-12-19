package hu.kts.convictmetronome.persistency

import androidx.room.Database
import androidx.room.RoomDatabase

private const val version = 1

@Database(entities = [Exercise::class], version = version)
abstract class CmDatabase: RoomDatabase() {

    abstract fun exerciseDao(): ExerciseDao

}