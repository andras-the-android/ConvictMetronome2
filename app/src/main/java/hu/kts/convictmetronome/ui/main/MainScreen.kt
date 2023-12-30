package hu.kts.convictmetronome.ui.main

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import hu.kts.convictmetronome.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    title: String,
    openDrawer: () -> Unit,
    optionsMenuExpanded: Boolean,
    deleteEnabled: Boolean,
    showConfirmDeleteExerciseDialog: Boolean,
    appBarActionCallbacks: AppBarActionCallbacks,
    onEditExerciseClicked: () -> Unit,
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = openDrawer) {
                        Icon(Icons.Outlined.Menu, stringResource(id = R.string.drawer_open))
                    }
                },
                actions = {
                    OptionsMenu(
                        optionsMenuExpanded = optionsMenuExpanded,
                        deleteEnabled = deleteEnabled,
                        actionCallbacks = appBarActionCallbacks,
                        onEditExerciseClicked = onEditExerciseClicked
                    )
                }

            )
        },
        content = content
    )

    if (showConfirmDeleteExerciseDialog) {
        AlertDialog(
            onDismissRequest = { appBarActionCallbacks.dismissConfirmDeleteExerciseDialog() },
            confirmButton = { TextButton(onClick = { appBarActionCallbacks.onConfirmDeleteExercise() }) {
                Text(stringResource(id = R.string.generic_ok))
            } },
            dismissButton = { TextButton(onClick = { appBarActionCallbacks.dismissConfirmDeleteExerciseDialog() }) {
                Text(stringResource(id = R.string.generic_cancel))
            } },
            title = { Text(stringResource(id = R.string.exercise_delete)) },
            text = { Text(stringResource(id = R.string.generic_are_you_sure)) }
        )
    }
}

@Composable
private fun OptionsMenu(
    optionsMenuExpanded: Boolean,
    deleteEnabled: Boolean,
    actionCallbacks: AppBarActionCallbacks,
    onEditExerciseClicked: () -> Unit,
) {
    IconButton(onClick = { actionCallbacks.onOptionsActionClicked() }) {
        Icon(Icons.Outlined.MoreVert, stringResource(id = R.string.exercise_options_menu))
    }

    DropdownMenu(
        expanded = optionsMenuExpanded,
        onDismissRequest = { actionCallbacks.dismissOptionsMenu() }
    ) {
        DropdownMenuItem(
            text = { Text(stringResource(id = R.string.exercise_edit)) },
            onClick = {
                actionCallbacks.dismissOptionsMenu()
                onEditExerciseClicked()
            }
        )
        DropdownMenuItem(
            text = { Text(stringResource(id = R.string.exercise_delete)) },
            enabled = deleteEnabled,
            onClick = {
                actionCallbacks.onDeleteExerciseClicked()
            }
        )

    }
}
