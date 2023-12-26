package hu.kts.convictmetronome

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import dagger.hilt.android.AndroidEntryPoint
import hu.kts.convictmetronome.ui.theme.ConvictMetronomeTheme
import hu.kts.convictmetronome.ui.workout.WorkoutScreen
import hu.kts.convictmetronome.ui.workout.WorkoutScreenState
import hu.kts.convictmetronome.ui.workout.WorkoutViewModel

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ConvictMetronomeTheme {
                val workoutViewModel: WorkoutViewModel = viewModel()
                val workoutState: WorkoutScreenState by workoutViewModel.state.collectAsStateWithLifecycle()

                AnimatedVisibility(visible = workoutState is WorkoutScreenState.Content) {
                    WorkoutScreen(
                        state = workoutState as WorkoutScreenState.Content,
                        onClick = workoutViewModel::onCounterClick,
                        onLongClick = workoutViewModel::onCounterLongClick,
                    )
                }
            }
        }
    }
}
