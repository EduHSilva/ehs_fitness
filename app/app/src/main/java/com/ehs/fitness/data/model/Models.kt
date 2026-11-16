package com.ehs.fitness.data.model

/** Exercício já vinculado a um treino, com as séries prescritas. */
data class WorkoutExercise(
    val exerciseId: Int,
    val name: String,
    val bodyPart: String = "",
    val series: Int = 3,
    val repetitions: Int = 12,
    val load: Double? = null,
    val restSeconds: Int? = null,
    val notes: String = "",
)

data class Workout(
    val id: Int,
    val name: String,
    val dayOfWeek: Int = 0,
    val exercises: List<WorkoutExercise> = emptyList(),
)

/** Dia da agenda semanal: `workoutId` nulo significa descanso. */
data class WeekDay(
    val dayOfWeek: Int,
    val workoutId: Int? = null,
    val name: String = "",
)

data class MealFood(
    val foodId: Int,
    val name: String,
    val quantity: Int = 100,
    val observation: String = "",
)

data class Meal(
    val id: Int,
    val name: String,
    val hour: String,
    val foods: List<MealFood> = emptyList(),
)
