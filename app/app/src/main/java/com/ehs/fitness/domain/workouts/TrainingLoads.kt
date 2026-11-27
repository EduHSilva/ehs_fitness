package com.ehs.fitness.domain.workouts

import com.ehs.fitness.data.model.Workout

/**
 * Cargas ajustadas pelo aluno no aparelho, por exercício. O valor `0.0` é uma
 * sobreposição válida ("sem carga") e remove a carga prescrita no plano.
 */
object TrainingLoads {
    fun apply(workout: Workout, overrides: Map<Int, Double>): Workout {
        if (overrides.isEmpty()) return workout
        return workout.copy(
            exercises = workout.exercises.map { exercise ->
                val override = overrides[exercise.exerciseId] ?: return@map exercise
                exercise.copy(load = override.takeIf { it > 0.0 })
            },
        )
    }

    fun apply(workouts: List<Workout>, overrides: Map<Int, Double>): List<Workout> =
        if (overrides.isEmpty()) workouts else workouts.map { apply(it, overrides) }
}