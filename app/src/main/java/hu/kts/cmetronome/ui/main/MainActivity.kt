package hu.kts.cmetronome.ui.main

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowHeightSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dagger.hilt.android.AndroidEntryPoint
import hu.kts.cmetronome.ui.exercise.ExerciseSheet
import hu.kts.cmetronome.ui.exercise.ExerciseSheetState
import hu.kts.cmetronome.ui.exercise.ExerciseViewModel
import hu.kts.cmetronome.ui.theme.CmTheme
import hu.kts.cmetronome.ui.workout.WorkoutScreen
import hu.kts.cmetronome.ui.workout.WorkoutScreenState
import hu.kts.cmetronome.ui.workout.WorkoutViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val mainViewModel: MainViewModel by viewModels()
    private val workoutViewModel: WorkoutViewModel by viewModels()
    private val exerciseViewModel: ExerciseViewModel by viewModels()
    private var showSplash = true

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        installSplashScreen().apply {
            setKeepOnScreenCondition {
                showSplash
            }
        }

        enableEdgeToEdge()

        setContent {

            val windowSizeClass = calculateWindowSizeClass(this)
            val compactMode = windowSizeClass.heightSizeClass == WindowHeightSizeClass.Compact

            CmTheme {
                val workoutState: WorkoutScreenState by workoutViewModel.state.collectAsStateWithLifecycle()
                val mainState: MainScreenState by mainViewModel.state.collectAsStateWithLifecycle()
                val exerciseState: ExerciseSheetState by exerciseViewModel.state.collectAsStateWithLifecycle()

                val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                val coroutineScope = rememberCoroutineScope()

                AnimatedVisibility(
                    visible = mainState is MainScreenState.Content && workoutState is WorkoutScreenState.Content,
                    enter = fadeIn(spring(stiffness = Spring.StiffnessLow)),
                    exit = fadeOut(spring(stiffness = Spring.StiffnessLow)),
                ) {

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
                            Box(
                                Modifier
                                    .padding(it)
                                    .fillMaxSize(), contentAlignment = Alignment.Center) {
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
                    LaunchedEffect(showSplash) {
                        showSplash = false
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
