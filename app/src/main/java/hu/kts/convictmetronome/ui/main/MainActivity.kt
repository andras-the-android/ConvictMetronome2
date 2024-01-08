package hu.kts.convictmetronome.ui.main

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowHeightSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import dagger.hilt.android.AndroidEntryPoint
import hu.kts.convictmetronome.ui.exercise.ExerciseSheet
import hu.kts.convictmetronome.ui.exercise.ExerciseSheetState
import hu.kts.convictmetronome.ui.exercise.ExerciseViewModel
import hu.kts.convictmetronome.ui.theme.CmTheme
import hu.kts.convictmetronome.ui.workout.WorkoutScreen
import hu.kts.convictmetronome.ui.workout.WorkoutScreenState
import hu.kts.convictmetronome.ui.workout.WorkoutViewModel
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {

            val windowSizeClass = calculateWindowSizeClass(this)
            val compactMode = windowSizeClass.heightSizeClass == WindowHeightSizeClass.Compact


            CmTheme {
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
                    val workoutContent = workoutState as WorkoutScreenState.Content
                    keepScreenAwake(workoutContent.keepScreenAlive)
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
                            Box(Modifier.padding(it).fillMaxSize(), contentAlignment = Alignment.Center) {
                                WorkoutScreen(
                                    state = workoutContent,
                                    compactMode = compactMode,
                                    callbacks = workoutViewModel,
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

    private fun keepScreenAwake(keep: Boolean) {
        if (keep) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }
}
