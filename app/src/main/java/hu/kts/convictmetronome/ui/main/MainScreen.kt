package hu.kts.convictmetronome.ui.main

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import hu.kts.convictmetronome.R
import hu.kts.convictmetronome.core.Sounds

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
                        value = state.volume
                    )
                    OptionsMenu(
                        expanded = state.optionsMenuExpanded,
                        deleteEnabled = state.deleteEnabled,
                        actionCallbacks = appBarActionCallbacks,
                        onEditExerciseClicked = onEditExerciseClicked
                    )
                }

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
        Icon(Icons.Outlined.MoreVert, stringResource(id = R.string.app_bar_options_menu))
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
    value: Float
) {
    val imageVector = when (value) {
        Sounds.maxVolume -> Icons.Outlined.VolumeUp
        0f -> Icons.Outlined.VolumeOff
        else -> Icons.Outlined.VolumeDown
    }
    IconButton(onClick = actionCallbacks::onVolumeActionClicked) {
        Icon(imageVector, stringResource(id = R.string.app_bar_options_menu))
    }

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = actionCallbacks::dismissVolumePopup,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(stringResource(id = R.string.app_bar_volume_explanation))
        Slider(
            value = value,
            onValueChange = actionCallbacks::onVolumeChange,
            valueRange = 0f..Sounds.maxVolume,
            steps = 4
        )
    }
}
