package hu.kts.convictmetronome.ui.workout

import android.util.Log
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import hu.kts.convictmetronome.ui.theme.ConvictMetronomeTheme

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WorkoutScreenPoc() {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val haptic = LocalHapticFeedback.current
    val progressAnimated by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "shimmer"
    )
    Surface (
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize()
            .drawWithCache {
                val progress = 0.5f
                val gradientThickness = 0.1f
                val offset = progress * (1 + gradientThickness)

                
                val brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White,
                        Color(0xFF42A5F5)
                    ),
                    startY = size.height * (offset - gradientThickness) ,
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
            Text("0",
                fontSize = 128.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .combinedClickable(
                        onClick = {Log.d("xxx", "short") },
                        onLongClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            Log.d("xxx", "long")
                        },
                    )
                    .padding(8.dp)
                    .fillMaxWidth()
            )
        }
    }
}

@Preview(showSystemUi = true)
@Composable
private fun PreviewWorkoutScreen() {
    ConvictMetronomeTheme {
        WorkoutScreenPoc()
    }
}