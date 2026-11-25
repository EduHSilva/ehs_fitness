package com.ehs.fitness.navigation

object Routes {
    const val HOME = "home"
    const val WORKOUTS = "workouts"
    const val WEEK = "week"
    const val DIET = "diet"
    const val HISTORY = "history"
    const val PROFILE = "profile"
    const val TRAINING = "training/{workoutId}"
    const val TRAINING_WORKOUT_ARG = "workoutId"

    fun training(workoutId: Int): String = "training/$workoutId"
}
