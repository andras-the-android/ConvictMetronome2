package hu.kts.cmetronome.di

import android.content.Context
import android.speech.tts.TextToSpeech
import android.widget.Toast
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import hu.kts.cmetronome.R
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

    @Provides
    fun provideGson(): Gson {
        return Gson()
    }

    @Provides
    fun provideTextToSpeech(@ApplicationContext context: Context): TextToSpeech {
        return TextToSpeech(context) { status ->
            if (status == TextToSpeech.ERROR) {
                Toast.makeText(context, R.string.text_to_speech_error, Toast.LENGTH_SHORT).show()
            }
        }
    }

}
