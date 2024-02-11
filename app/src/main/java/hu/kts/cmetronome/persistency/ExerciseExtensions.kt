package hu.kts.cmetronome.persistency

import hu.kts.cmetronome.timer.ExercisePhase
import hu.kts.cmetronome.ui.workout.WorkoutAnimationTargetState
import hu.kts.cmetronome.workoutlogic.Workout

fun Exercise.phaseDurationMillis(phase: ExercisePhase): Int {
    return when (phase) {
        ExercisePhase.Down -> downMillis
        ExercisePhase.LowerHold -> lowerHoldMillis
        ExercisePhase.Up -> upMillis
        ExercisePhase.UpperHold -> upperHoldMillis
    }
}

fun Exercise.initialPhase() = if (startWithUp) ExercisePhase.Up else ExercisePhase.Down

fun Exercise.getInitialAnimationTargetState(): WorkoutAnimationTargetState {
    return if (this.startWithUp)
        WorkoutAnimationTargetState.Bottom(Workout.animationResetDuration)
    else
        WorkoutAnimationTargetState.Top(Workout.animationResetDuration)
}

