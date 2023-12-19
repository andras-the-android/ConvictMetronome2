package hu.kts.convictmetronome.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import java.time.Clock
import javax.inject.Qualifier
import kotlin.coroutines.CoroutineContext

@Qualifier
annotation class IoDispatcher

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    fun provideClock(): Clock {
        return Clock.systemDefaultZone()
    }

    @Provides
    fun provideCoroutineScope(): CoroutineScope {
        return CoroutineScope(Dispatchers.IO)
    }

    @Provides
    @IoDispatcher
    fun provideIoDispatcher(): CoroutineContext {
        return Dispatchers.IO
    }

}