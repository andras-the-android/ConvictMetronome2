package hu.kts.convictmetronome.ui.main

import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import hu.kts.convictmetronome.R
import hu.kts.convictmetronome.persistency.Exercise
import hu.kts.convictmetronome.ui.theme.ConvictMetronomeTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun CmDrawer(
    exercises: List<Exercise>,
    selectedExerciseId: Int,
    onExerciseClick: (id: Int) -> Unit,
    onCreateNewClick: () -> Unit,
    drawerState: DrawerState = rememberDrawerState(initialValue = DrawerValue.Closed),
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
    content: @Composable () -> Unit
) {
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = { DrawerContent(
            exercises = exercises,
            selectedExerciseId = selectedExerciseId,
            onExerciseClick = onExerciseClick,
            onCreateNewClick = onCreateNewClick,
            drawerState = drawerState,
            coroutineScope = coroutineScope,
        )},
        content = content,
    )
}

@Composable
private fun DrawerContent(
    exercises: List<Exercise>,
    selectedExerciseId: Int,
    onExerciseClick: (id: Int) -> Unit,
    onCreateNewClick: () -> Unit,
    drawerState: DrawerState,
    coroutineScope: CoroutineScope,
) {
    ModalDrawerSheet {
        exercises.forEach { exercise ->
            NavigationDrawerItem(
                label = { Text(text = exercise.name) },
                selected = selectedExerciseId == exercise.id,
                onClick = {
                    coroutineScope.launch {
                        drawerState.close()
                        onExerciseClick(exercise.id)
                    }
                }
            )
        }
        Divider()
        NavigationDrawerItem(
            label = { Text(text = stringResource(id = R.string.drawer_create_new)) },
            selected = false,
            onClick = {
                coroutineScope.launch {
                    drawerState.close()
                    onCreateNewClick()
                }
            }
        )
    }
}

@Preview
@Composable
private fun PreviewDrawer() {
    ConvictMetronomeTheme {
        DrawerContent(
            exercises = listOf(
                Exercise.default.copy(id = 0, name = "Push up"),
                Exercise.default.copy(id = 1, name = "Pull up"),
                Exercise.default.copy(id = 2, name = "Squat"),
            ),
            selectedExerciseId = 1,
            onExerciseClick = {},
            onCreateNewClick = {},
            drawerState = DrawerState(initialValue = DrawerValue.Open),
            coroutineScope = rememberCoroutineScope()

        )
    }
}


