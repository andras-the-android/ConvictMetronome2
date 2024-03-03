package hu.kts.cmetronome.ui.main

import android.content.res.Configuration
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.VolumeDown
import androidx.compose.material.icons.outlined.VolumeOff
import androidx.compose.material.icons.outlined.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import hu.kts.cmetronome.R
import hu.kts.cmetronome.sounds.Sounds
import hu.kts.cmetronome.ui.theme.CmTheme
import hu.kts.cmetronome.ui.workout.WorkoutActionCallbacks
import hu.kts.cmetronome.ui.workout.WorkoutAnimationTargetState
import hu.kts.cmetronome.ui.workout.WorkoutScreen
import hu.kts.cmetronome.ui.workout.WorkoutScreenState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    openDrawer: () -> Unit,
    state: MainScreenState.Content,
    appBarActionCallbacks: AppBarActionCallbacks,
    onEditExerciseClicked: () -> Unit,
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.title) },
                navigationIcon = {
                    IconButton(onClick = openDrawer) {
                        Icon(Icons.Outlined.Menu, stringResource(id = R.string.drawer_open))
                    }
                },
                actions = {
                    VolumePopup(
                        expanded = state.volumePopupExpanded,
                        actionCallbacks = appBarActionCallbacks,
                        upDownVolume = state.upDownVolume,
                        speechVolume = state.speechVolume,
                    )
                    OptionsMenu(
                        expanded = state.optionsMenuExpanded,
                        deleteEnabled = state.deleteEnabled,
                        actionCallbacks = appBarActionCallbacks,
                        onEditExerciseClicked = onEditExerciseClicked
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primaryContainer)

            )
        },
        content = content
    )

    if (state.showConfirmDeleteExerciseDialog) {
        AlertDialog(
            onDismissRequest = { appBarActionCallbacks.dismissConfirmDeleteExerciseDialog() },
            confirmButton = { TextButton(onClick = { appBarActionCallbacks.onConfirmDeleteExercise() }) {
                Text(stringResource(id = R.string.generic_ok))
            } },
            dismissButton = { TextButton(onClick = { appBarActionCallbacks.dismissConfirmDeleteExerciseDialog() }) {
                Text(stringResource(id = R.string.generic_cancel))
            } },
            title = { Text(stringResource(id = R.string.app_bar_delete)) },
            text = { Text(stringResource(id = R.string.generic_are_you_sure)) }
        )
    }
}

@Composable
private fun OptionsMenu(
    expanded: Boolean,
    deleteEnabled: Boolean,
    actionCallbacks: AppBarActionCallbacks,
    onEditExerciseClicked: () -> Unit,
) {
    IconButton(onClick = actionCallbacks::onOptionsActionClicked) {
        Icon(
            imageVector = Icons.Outlined.MoreVert,
            contentDescription = stringResource(id = R.string.app_bar_options_menu),
            tint = MaterialTheme.colorScheme.secondary,
        )
    }

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = actionCallbacks::dismissOptionsMenu
    ) {
        DropdownMenuItem(
            text = { Text(stringResource(id = R.string.app_bar_edit)) },
            onClick = {
                actionCallbacks.dismissOptionsMenu()
                onEditExerciseClicked()
            }
        )
        DropdownMenuItem(
            text = { Text(stringResource(id = R.string.app_bar_delete)) },
            enabled = deleteEnabled,
            onClick = actionCallbacks::onDeleteExerciseClicked,
        )

    }
}

@Composable
private fun VolumePopup(
    expanded: Boolean,
    actionCallbacks: AppBarActionCallbacks,
    upDownVolume: Float,
    speechVolume: Float,
) {
    val imageVector = when {
        minOf(upDownVolume, speechVolume) == Sounds.maxVolume -> Icons.Outlined.VolumeUp
        maxOf(upDownVolume, speechVolume) == 0f -> Icons.Outlined.VolumeOff
        else -> Icons.Outlined.VolumeDown
    }
    AnimatedContent(
        targetState = imageVector,
        label = "volume",
        transitionSpec = { scaleIn() togetherWith scaleOut() },
    ) {
        IconButton(onClick = actionCallbacks::onVolumeActionClicked) {
            Icon(
                it,
                stringResource(id = R.string.app_bar_volume),
                tint = MaterialTheme.colorScheme.secondary
            )
        }
    }

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = actionCallbacks::dismissVolumePopup,
        modifier = Modifier
            .widthIn(max = 600.dp)
            .fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp)) {
            Text(
                text = stringResource(id = R.string.app_bar_volume_explanation),
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
            )
            Text(
                text = stringResource(id = R.string.app_bar_volume_up_down),
                color = MaterialTheme.colorScheme.outline,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 16.dp)
            )
            Slider(
                value = upDownVolume,
                onValueChange = actionCallbacks::onUpDownVolumeChange,
                valueRange = 0f..Sounds.volumeSteps - 1f,
                steps = Sounds.volumeSteps - 2,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp)
            )
            Text(
                text = stringResource(id = R.string.app_bar_volume_speech),
                color = MaterialTheme.colorScheme.outline,
                fontWeight = FontWeight.Bold
            )
            Slider(
                value = speechVolume,
                onValueChange = actionCallbacks::onSpeechVolumeChange,
                valueRange = 0f..Sounds.volumeSteps - 1f,
                steps = Sounds.volumeSteps - 2,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp)
            )
        }
    }
}

@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL,
    showSystemUi = true
)
@Preview(
    showSystemUi = true
)
@Composable
fun PreviewMainScreen() {
    CmTheme {
        MainScreen(
            openDrawer = {},
            state = MainScreenState.Content(
                title = "Push up",
                exercises = emptyList(),
                selectedExerciseId = 0,
                optionsMenuExpanded = false,
                showConfirmDeleteExerciseDialog = false,
                volumePopupExpanded = false,
                upDownVolume = 1f,
                speechVolume = 1f,
            ),
            appBarActionCallbacks = object : AppBarActionCallbacks {
                override fun onOptionsActionClicked() {}
                override fun dismissOptionsMenu() {}
                override fun onDeleteExerciseClicked() {}
                override fun onConfirmDeleteExercise() {}
                override fun dismissConfirmDeleteExerciseDialog() {}
                override fun onVolumeActionClicked() {}
                override fun dismissVolumePopup() {}
                override fun onUpDownVolumeChange(value: Float) {}
                override fun onSpeechVolumeChange(value: Float) {}
            },
            onEditExerciseClicked = {})
        {
            WorkoutScreen(
                state = WorkoutScreenState.Content(
                    repCounter = 5,
                    interSetClock = "00:00",
                    completedSets = 2,
                    animationTargetState = WorkoutAnimationTargetState.Bottom(0)
                ),
                callbacks = object : WorkoutActionCallbacks {
                    override fun onClick() {}
                    override fun onLongClick(eventConsumed: () -> Unit) {}
                    override fun confirmReset() {}
                    override fun dismissConfirmResetDialog() {}
                }
            )
        }
    }
}
