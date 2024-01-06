package hu.kts.convictmetronome.ui.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import dagger.hilt.android.AndroidEntryPoint
import hu.kts.convictmetronome.ui.exercise.ExerciseSheet
import hu.kts.convictmetronome.ui.exercise.ExerciseSheetState
import hu.kts.convictmetronome.ui.exercise.ExerciseViewModel
import hu.kts.convictmetronome.ui.theme.ConvictMetronomeTheme
import hu.kts.convictmetronome.ui.workout.WorkoutScreen
import hu.kts.convictmetronome.ui.workout.WorkoutScreenState
import hu.kts.convictmetronome.ui.workout.WorkoutViewModel
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ConvictMetronomeTheme {
                val workoutViewModel: WorkoutViewModel = viewModel()
                val workoutState: WorkoutScreenState by workoutViewModel.state.collectAsStateWithLifecycle()
                val mainViewModel: MainViewModel = viewModel()
                val mainState: MainScreenState by mainViewModel.state.collectAsStateWithLifecycle()
                val exerciseViewModel: ExerciseViewModel = viewModel()
                val exerciseState: ExerciseSheetState by exerciseViewModel.state.collectAsStateWithLifecycle()

                val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                val coroutineScope = rememberCoroutineScope()

                AnimatedVisibility(visible = mainState is MainScreenState.Content && workoutState is WorkoutScreenState.Content) {

                    val mainContent = mainState as MainScreenState.Content
                    CmDrawer(
                        exercises = mainContent.exercises,
                        selectedExerciseId = mainContent.selectedExerciseId,
                        onExerciseClick = mainViewModel::selectExercise,
                        onCreateNewClick = exerciseViewModel::createNewExercise,
                        drawerState = drawerState,
                        coroutineScope = coroutineScope,
                    ) {
                        MainScreen(
                            openDrawer = { coroutineScope.launch { drawerState.open() } },
                            state = mainContent,
                            appBarActionCallbacks = mainViewModel,
                            onEditExerciseClicked = exerciseViewModel::editSelectedExercise,
                        ) {
                            Box(Modifier.padding(it)) {
                                WorkoutScreen(
                                    state = workoutState as WorkoutScreenState.Content,
                                    onClick = workoutViewModel::onCounterClick,
                                    onLongClick = workoutViewModel::onCounterLongClick,
                                )

                                if (exerciseState is ExerciseSheetState.Showing) {
                                    ExerciseSheet(
                                        onDismissRequest = exerciseViewModel::dismissSheet,
                                        state = exerciseState as ExerciseSheetState.Showing,
                                        callbacks = exerciseViewModel
                                    )
                                }

                            }
                        }
                    }
                }
            }
        }
    }
}
