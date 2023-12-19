package hu.kts.convictmetronome.persistency

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "exercise")
data class Exercise(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @ColumnInfo(name = "name")
    val name: String,
    @ColumnInfo(name = "countdown_from")
    val countdownFrom: Int,
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
)