package hu.kts.cmetronome.ui.main

import android.content.Context
import androidx.compose.ui.text.AnnotatedString
import androidx.core.text.HtmlCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import hu.kts.cmetronome.R
import hu.kts.cmetronome.persistency.Preferences
import hu.kts.cmetronome.ui.toAnnotatedString
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class WhatsNewDialogContent @Inject constructor(
    @ApplicationContext context: Context,
    preferences: Preferences,
    coroutineScope: CoroutineScope
) {

    private val _state = MutableStateFlow<AnnotatedString?>(null)
    val state = _state.asStateFlow()

    init {
        coroutineScope.launch {
            // new install, we don't show the dialog
            if (preferences.whatsNewSeenOnVersion < 0) {
                preferences.updateWhatsNewSeenVersion()
                return@launch
            }

            if (preferences.whatsNewSeenOnVersion < LATEST_VERSION_WITH_WHATS_NEW_RECORD) {
                val inputStream = context.resources.openRawResource(R.raw.release_notes)
                val bytes = ByteArray(inputStream.available())
                inputStream.read(bytes)

                _state.value = HtmlCompat.fromHtml(String(bytes), HtmlCompat.FROM_HTML_MODE_COMPACT)
                    .toAnnotatedString()
                preferences.updateWhatsNewSeenVersion()
            }
        }
    }

    fun dismissDialog() {
        _state.value = null
    }

    companion object {
        private const val LATEST_VERSION_WITH_WHATS_NEW_RECORD = 15
    }

}
