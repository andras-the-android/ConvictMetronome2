package hu.kts.cmetronome.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineExceptionHandler
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
        // we don't have Crashlytics, so at least this way the exceptions won't be swallowed
        val exceptionHandler = CoroutineExceptionHandler { _, throwable -> throw throwable }
        return CoroutineScope(Dispatchers.IO + exceptionHandler)
    }

    @Provides
    @IoDispatcher
    fun provideIoDispatcher(): CoroutineContext {
        return Dispatchers.IO
    }

}
