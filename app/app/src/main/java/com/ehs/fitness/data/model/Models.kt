package com.ehs.fitness.data.model

import java.time.LocalDate

/** Exercício já vinculado a um treino, com as séries prescritas. */
data class WorkoutExercise(
    val exerciseId: Int,
    val name: String,
    val bodyPart: String = "",
    val series: Int = 3,
    val repetitions: Int = 12,
    val load: Double? = null,
    val restSeconds: Int? = null,
    val durationSeconds: Int? = null,
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
    val calories: Double = 0.0,
    val protein: Double = 0.0,
    val carbs: Double = 0.0,
    val fat: Double = 0.0,
    val fiber: Double = 0.0,
)

data class Meal(
    val id: Int,
    val name: String,
    val hour: String,
    val foods: List<MealFood> = emptyList(),
)

/**
 * Acompanhamento solicitado pelo profissional no web: creatina, treino e meta
 * diária de cardio (em minutos; 0 desativa o registro).
 */
data class TrackingConfig(
    val showCreatine: Boolean = true,
    val showWorkout: Boolean = false,
    val dailyCardio: Double = 0.0,
) {
    val cardioEnabled: Boolean get() = dailyCardio > 0.0
    val enabled: Boolean get() = showCreatine || showWorkout || cardioEnabled
}

/** Registro do próprio dia. */
data class DailyTracking(
    val creatine: Boolean = false,
    val trained: Boolean = false,
    val cardio: Double = 0.0,
)

/** Registro diário com a data, usado no calendário de acompanhamento. */
data class TrackingDay(
    val date: LocalDate,
    val creatine: Boolean = false,
    val trained: Boolean = false,
    val cardio: Double = 0.0,
)

/**
 * Vínculo ativo com o profissional que gerencia o plano do aluno.
 * `domain` é `PERSONAL` (treinos/agenda) ou `NUTRITIONIST` (dieta).
 */
data class ManagedLink(
    val domain: String,
    val professionalName: String,
)
