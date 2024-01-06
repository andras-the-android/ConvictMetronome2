package hu.kts.convictmetronome.ui.exercise

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.text.HtmlCompat
import hu.kts.convictmetronome.R
import hu.kts.convictmetronome.ui.theme.CmTheme
import hu.kts.convictmetronome.ui.toAnnotatedString

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseSheet(
    onDismissRequest: () -> Unit,
    state: ExerciseSheetState.Showing,
    callbacks: ExerciseSheetCallbacks
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = state.name,
                onValueChange = { callbacks.onNameChange(it) },
                label = { Text(stringResource(id = R.string.generic_name)) }
            )

            ExerciseItem(
                titleResId = R.string.exercise_countdown_from,
                value = state.countdownFromSeconds,
                position = state.countdownFromPosition
            ) { callbacks.onCountdownFromChange(it) }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = stringResource(id = R.string.exercise_start_with_up), color = MaterialTheme.colorScheme.outline)
                Checkbox(checked = state.startWithUp, onCheckedChange = { callbacks.onStartWithUpChange(it) })
            }

            ExerciseItem(
                titleResId = R.string.exercise_down_duration,
                value = state.downSeconds,
                position = state.downPosition
            ) { callbacks.onDownChange(it) }

            ExerciseItem(
                titleResId = R.string.exercise_lower_hold_duration,
                value = state.lowerHoldSeconds,
                position = state.lowerHoldPosition
            ) { callbacks.onLowerHoldChange(it) }

            ExerciseItem(
                titleResId = R.string.exercise_up_duration,
                value = state.upSeconds,
                position = state.upPosition
            ) { callbacks.onUpChange(it) }

            ExerciseItem(
                titleResId = R.string.exercise_upper_hold_duration,
                value = state.upperHoldSeconds,
                position = state.upperHoldPosition
            ) { callbacks.onUpperHoldChange(it) }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                Button(onClick = { callbacks.onSaveClicked() }) {
                    Text(text = stringResource(id = R.string.generic_save))
                }
            }
        }
    }
}

@Composable
private fun ExerciseItem(
    @StringRes titleResId: Int,
    value: Float,
    position: Int,
    onValueChange: (Int) -> Unit
) {
    Column(
        Modifier.padding(top = 12.dp)
    ) {
        val title = HtmlCompat
            .fromHtml(
                stringResource(id = titleResId, value),
                HtmlCompat.FROM_HTML_MODE_COMPACT,
            )
            .toAnnotatedString()
        Text(text = title, color = MaterialTheme.colorScheme.outline)
        Slider(
            value = position.toFloat(),
            onValueChange = { onValueChange(it.toInt()) },
            valueRange = 0f..steps - 1f,
            steps = steps,
            modifier = Modifier.padding(start = 16.dp, end = 16.dp)
        )
    }
}

private val steps = ExerciseProperties.valuesInSeconds.count()

@Preview(showBackground = true)
@Composable
fun PreviewExerciseItem() {
    val positions = arrayListOf(0f, 1.5f, 5f, 10f, 15f)
    var value by remember { mutableFloatStateOf(0f) }
    var position by remember { mutableIntStateOf(0) }
    Box(Modifier.padding(8.dp)) {
        ExerciseItem(
            titleResId = R.string.exercise_down_duration,
            value = value,
            position = position
        ) {
            position = it
            value = positions[it]
        }
    }
}

@Preview(showSystemUi = true)
@Composable
fun PreviewExerciseSheet() {
    CmTheme {
        val state = ExerciseSheetState.Showing(
            id = 0,
            name = "Sample",
            
            countdownFromSeconds = 3.0F,
            countdownFromPosition = 3,

            startWithUp = false,

            downSeconds = 3.0F,
            downPosition = 3,

            lowerHoldSeconds = 3.0F,
            lowerHoldPosition = 3,

            upSeconds = 3.0F,
            upPosition = 3,

            upperHoldSeconds = 3.0F,
            upperHoldPosition = 3,
        )

        ExerciseSheet(
            onDismissRequest = {},
            state = state,
            callbacks = object : ExerciseSheetCallbacks {
                override fun onNameChange(value: String) {}
                override fun onStartWithUpChange(value: Boolean) {}
                override fun onCountdownFromChange(value: Int) {}
                override fun onDownChange(value: Int) {}
                override fun onLowerHoldChange(value: Int) {}
                override fun onUpChange(value: Int) {}
                override fun onUpperHoldChange(value: Int) {}
                override fun onSaveClicked() {}
            }
        )
    }
}
