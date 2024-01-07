package hu.kts.convictmetronome.ui.workout


import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
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
import hu.kts.convictmetronome.ui.theme.CmTheme
import hu.kts.convictmetronome.workoutlogic.Workout.Companion.animationResetDuration

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

    val animationColors = listOf(MaterialTheme.colorScheme.background, MaterialTheme.colorScheme.secondary)
    Surface (
        modifier = Modifier
            .padding(16.dp)
            .widthIn(max = 400.dp)
            .heightIn(max = 900.dp)
            .fillMaxSize()
            .drawWithCache {
                val offset = animationProgress * (1 + animationGradientThickness)

                val brush = Brush.verticalGradient(
                    colors = animationColors,
                    startY = size.height * (offset - animationGradientThickness),
                    endY = size.height * offset
                )
                onDrawBehind {
                    drawRoundRect(
                        brush,
                        cornerRadius = CornerRadius(10.dp.toPx())
                    )
                }
            },
        color = Color.Transparent,
        border = BorderStroke(2.dp, MaterialTheme.colorScheme.secondary),
        shape = MaterialTheme.shapes.small
    ) {
        Column(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(id = state.helpTextResourceId),
                textAlign = TextAlign.Center,
            )
            RepCounterAnimation(
                counterText = state.repCounter.toString(),
                countdownInProgress = state.countdownInProgress,
            ) { counterText ->
                Text(
                    text = counterText,
                    style = MaterialTheme.typography.displayLarge,
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
            }
            Text(
                text = state.interSetClock,
                style = MaterialTheme.typography.displaySmall,
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

@Composable
private fun RepCounterAnimation(
    counterText: String,
    countdownInProgress: Boolean,
    content: @Composable (String) -> Unit,
) {
    AnimatedContent(
        targetState = counterText,
        label = "content",
        transitionSpec = if (countdownInProgress) countdownTransitionSpec else repCounterTransitionSpec,
    ) {
        content(it)
    }
}

private val repCounterTransitionSpec: AnimatedContentTransitionScope<String>.() -> ContentTransform =
    {
        if (targetState < initialState) {
            (slideInVertically { height -> height } + fadeIn()) togetherWith
                    slideOutVertically { height -> -height } + fadeOut()
        } else {
            (slideInVertically { height -> -height } + fadeIn()) togetherWith
                    slideOutVertically { height -> height } + fadeOut()
        }.using(
            SizeTransform(clip = false)
        )
    }

private val countdownTransitionSpec: AnimatedContentTransitionScope<String>.() -> ContentTransform =
    {
        ((scaleIn() + fadeIn()) togetherWith
                scaleOut(targetScale = 10f) + fadeOut())
            .using(
                // Disable clipping since the faded slide-in/out should
                // be displayed out of bounds.
                SizeTransform(clip = false)
            )
    }



@Preview(showSystemUi = true)
@Composable
private fun PreviewWorkoutScreen() {
    CmTheme {
        WorkoutScreen(
            state = WorkoutScreenState.Content(
                repCounter = 5,
                interSetClock = "00:00",
                completedSets = 2,
                animationTargetState = WorkoutAnimationTargetState.Bottom(0),
                helpTextResourceId = R.string.help_initial
            ),
            onClick = {},
            onLongClick = { false }
        )
    }
}
