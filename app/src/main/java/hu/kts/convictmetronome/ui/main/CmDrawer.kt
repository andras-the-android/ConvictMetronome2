package hu.kts.convictmetronome.ui.main

import android.content.res.Configuration
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import hu.kts.convictmetronome.R
import hu.kts.convictmetronome.persistency.Exercise
import hu.kts.convictmetronome.ui.theme.CmTheme
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

private const val privacyPolicyUrl = "https://github.com/andras-the-android/ConvictMetronome2/blob/main/privacy_policy.md"

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
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 8.dp, end = 8.dp, top = 16.dp, bottom = 16.dp)
        ) {

            Icon(
                painter = painterResource(id = R.drawable.ic_metronome),
                contentDescription = "",
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = stringResource(id = R.string.app_name),
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(start = 8.dp),
                fontWeight = FontWeight.Bold
            )
        }
        Divider(modifier = Modifier.padding(top = 8.dp, bottom = 8.dp))
        exercises.forEach { exercise ->
            NavigationDrawerItem(
                label = { Text(text = exercise.name) },
                selected = selectedExerciseId == exercise.id,
                onClick = {
                    coroutineScope.launch {
                        drawerState.close()
                        onExerciseClick(exercise.id)
                    }
                },
                modifier = Modifier.padding(8.dp)
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
            },
            modifier = Modifier.padding(8.dp)
        )
        Divider()
        val uriHandler = LocalUriHandler.current
        NavigationDrawerItem(
            label = { Text(text = stringResource(id = R.string.privacy_policy)) },
            selected = false,
            onClick = { uriHandler.openUri(privacyPolicyUrl) },
            modifier = Modifier.padding(8.dp)
        )
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL)
@Composable
private fun PreviewDrawer() {
    CmTheme {
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


