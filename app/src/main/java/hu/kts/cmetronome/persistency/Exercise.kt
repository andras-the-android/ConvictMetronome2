package hu.kts.cmetronome.persistency

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "exercise")
data class Exercise(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @ColumnInfo(name = "name")
    val name: String,
    @ColumnInfo(name = "countdown_from_millis")
    val countdownFromMillis: Int,
    @ColumnInfo(name = "start_with_up")
    val startWithUp: Boolean,
    @ColumnInfo(name = "up_millis")
    val upMillis: Int,
    @ColumnInfo(name = "upper_hold_millis")
    val upperHoldMillis: Int,
    @ColumnInfo(name = "down_millis")
    val downMillis: Int,
    @ColumnInfo(name = "lower_hold_millis")
    val lowerHoldMillis: Int,
) {

    fun calcRepDuration() = downMillis + lowerHoldMillis + upMillis + upperHoldMillis

    companion object {
        const val defaultId = 0
        val default = Exercise(
            id = defaultId,
            name = "default",
            countdownFromMillis = 3000,
            startWithUp = false,
            upMillis = 2000,
            upperHoldMillis = 1000,
            downMillis = 2000,
            lowerHoldMillis = 1000
        )

        const val emptyId = -1
        val empty = default.copy(id = emptyId, name = "")
    }
}
