package hu.kts.convictmetronome.ui.workout


import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import hu.kts.convictmetronome.R
import hu.kts.convictmetronome.ui.theme.ConvictMetronomeTheme
import hu.kts.convictmetronome.ui.workout.WorkoutViewModel.Companion.animationResetDuration

private const val animationGradientThickness = 0.1f

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WorkoutScreen(
    state: WorkoutScreenState.Content,
    onClick: () -> Unit,
    onLongClick: () -> Boolean
) {
    val haptic = LocalHapticFeedback.current

    val animationDuration = state.animationTargetState.durationMillis
    val animationProgress by animateFloatAsState(
        targetValue = when (state.animationTargetState) {
            // we need to slightly alter the target value during reset to force recalculate the animation speed
            is WorkoutAnimationTargetState.Bottom -> if (animationDuration == animationResetDuration) 1.0001f else 1f
            is WorkoutAnimationTargetState.Top -> if (animationDuration == animationResetDuration) 0.0001f else 0f
        },
        animationSpec = tween(state.animationTargetState.durationMillis, easing = LinearEasing),
        label = "shimmer"
    )
    Surface (
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize()
            .drawWithCache {
                val offset = animationProgress * (1 + animationGradientThickness)

                val brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White,
                        Color(0xFF42A5F5)
                    ),
                    startY = size.height * (offset - animationGradientThickness) ,
                    endY = size.height * offset
                )
                onDrawBehind {
                    drawRoundRect(
                        brush,
                        cornerRadius = CornerRadius(10.dp.toPx())
                    )
                }
            },
        color = Color.Transparent
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = state.repCounter.toString(),
                fontSize = 128.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .combinedClickable(
                        onClick = onClick,
                        onLongClick = {
                            if (onLongClick()) {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            }
                        },
                    )
                    .padding(8.dp)
                    .fillMaxWidth()
            )
            Text(
                text = state.interSetClock,
                fontSize = 40.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
            )
            Row {
                Text(text = stringResource(id = R.string.set_counter_label))
                Text(text = state.completedSets.toString())

            }

        }
    }
}

@Preview(showSystemUi = true)
@Composable
private fun PreviewWorkoutScreen() {
    ConvictMetronomeTheme {
        WorkoutScreen(
            state = WorkoutScreenState.Content(
                repCounter = 5,
                interSetClock = "00:00",
                completedSets = 2,
                animationTargetState = WorkoutAnimationTargetState.Top(0)
            ),
            onClick = {},
            onLongClick = { false }
        )
    }
}
