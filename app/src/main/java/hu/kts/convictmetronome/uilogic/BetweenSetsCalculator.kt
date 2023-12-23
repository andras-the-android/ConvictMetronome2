package hu.kts.convictmetronome.uilogic

import android.annotation.SuppressLint
import hu.kts.convictmetronome.core.ticksToMs
import java.text.SimpleDateFormat
import java.util.Date
import javax.inject.Inject

class BetweenSetsCalculator @Inject constructor() {

    fun getClock(tickCount: Int): String {
        val elapsedTime = tickCount.ticksToMs()
        return format.format(Date(elapsedTime.toLong()))
    }

    companion object {
        @SuppressLint("SimpleDateFormat")
        private val format = SimpleDateFormat("mm:ss")
    }
}
