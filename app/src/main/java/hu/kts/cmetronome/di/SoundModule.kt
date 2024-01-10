package hu.kts.cmetronome.di

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.media.ToneGenerator
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import hu.kts.cmetronome.core.SoundArrayGenerator

@Module
@InstallIn(SingletonComponent::class)
object SoundModule {

    @Provides
    fun provideToneGenerator(): ToneGenerator {
        return ToneGenerator(AudioManager.STREAM_ALARM, 100)
    }

    @Provides
    fun provideAudioTrack(): AudioTrack {
        val bufferSize = AudioTrack.getMinBufferSize(
            SoundArrayGenerator.SAMPLE_RATE,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_8BIT)

        return AudioTrack(
           AudioAttributes.Builder()
               .setLegacyStreamType(AudioManager.STREAM_MUSIC)
               .build(),
           AudioFormat.Builder()
               .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
               .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
               .setSampleRate(SoundArrayGenerator.SAMPLE_RATE)
               .build(),
           bufferSize,
           AudioTrack.MODE_STREAM,
           AudioManager.AUDIO_SESSION_ID_GENERATE
        )
    }
}
