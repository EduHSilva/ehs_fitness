package com.ehs.fitness.domain.workouts

import com.ehs.fitness.data.model.WorkoutExercise
import java.util.Locale

private val PT_BR = Locale.forLanguageTag("pt-BR")

object WorkoutFormat {
    /** "3 séries · 12 repetições · 10 kg", como no cartão do web. */
    fun exerciseDetail(exercise: WorkoutExercise): String {
        val parts = buildList {
            add("${exercise.series} ${if (exercise.series == 1) "série" else "séries"}")
            add("${exercise.repetitions} ${if (exercise.repetitions == 1) "repetição" else "repetições"}")
            exercise.load?.takeIf { it > 0 }?.let { add("${formatDecimal(it)} kg") }
            exercise.restSeconds?.takeIf { it > 0 }?.let { add("${it}s de descanso") }
        }
        return parts.joinToString(" · ")
    }

    fun exerciseCountLabel(count: Int): String =
        if (count == 1) "1 exercício" else "$count exercícios"

    fun formatDecimal(value: Double): String =
        if (value % 1.0 == 0.0) value.toInt().toString()
        else String.format(PT_BR, "%.1f", value)
}
