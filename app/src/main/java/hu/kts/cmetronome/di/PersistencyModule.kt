package hu.kts.cmetronome.di

import android.content.Context
import android.content.SharedPreferences
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import hu.kts.cmetronome.persistency.CmDatabase
import hu.kts.cmetronome.persistency.ExerciseDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PersistencyModule {

    @Provides
    fun provideSharedPreferences(@ApplicationContext context: Context) : SharedPreferences {
        return context.getSharedPreferences("prefs", Context.MODE_PRIVATE)
    }

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): CmDatabase {
        return Room.databaseBuilder(
            context,
            CmDatabase::class.java, "cmetronome.db"
        ).build()
    }

    @Provides
    @Singleton
    fun provideSessionDao(database: CmDatabase): ExerciseDao {
        return database.exerciseDao()
    }
}
