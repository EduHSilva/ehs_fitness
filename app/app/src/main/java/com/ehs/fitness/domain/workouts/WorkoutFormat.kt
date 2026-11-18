package com.ehs.fitness.domain.workouts

import com.ehs.fitness.data.model.WorkoutExercise
import java.util.Locale

private val PT_BR = Locale.forLanguageTag("pt-BR")

object WorkoutFormat {
    /** "3 séries · 12 repetições · 10 kg", como no cartão do web. Cardio mostra o tempo. */
    fun exerciseDetail(exercise: WorkoutExercise): String =
        detail(exercise.bodyPart, exercise.series, exercise.repetitions, exercise.durationSeconds, exercise.load, exercise.restSeconds)

    fun exerciseDetail(exercise: TrainingExercise): String =
        detail(exercise.bodyPart, exercise.series, exercise.repetitions, exercise.durationSeconds, exercise.load, exercise.restSeconds)

    fun isCardio(bodyPart: String?): Boolean =
        bodyPart?.trim()?.equals("cardio", ignoreCase = true) == true

    /** "20 min": duração prescrita para exercícios de cardio. */
    fun durationLabel(durationSeconds: Int?): String {
        val seconds = durationSeconds ?: 0
        if (seconds <= 0) return "por tempo"
        val minutes = (seconds / 60).coerceAtLeast(1)
        return "$minutes min"
    }

    private fun detail(
        bodyPart: String,
        series: Int,
        repetitions: Int,
        durationSeconds: Int?,
        load: Double?,
        restSeconds: Int?,
    ): String {
        val parts = buildList {
            if (isCardio(bodyPart)) {
                add(durationLabel(durationSeconds))
            } else {
                add("$series ${if (series == 1) "série" else "séries"}")
                add("$repetitions ${if (repetitions == 1) "repetição" else "repetições"}")
                load?.takeIf { it > 0 }?.let { add("${formatDecimal(it)} kg") }
            }
            restSeconds?.takeIf { it > 0 }?.let { add("${it}s de descanso") }
        }
        return parts.joinToString(" · ")
    }

    fun exerciseCountLabel(count: Int): String =
        if (count == 1) "1 exercício" else "$count exercícios"

    /** Mesmos rótulos do web (`web/app/features/workouts/bodyParts.ts`). */
    private val BODY_PART_LABELS = mapOf(
        "upper arms" to "Braços",
        "upper legs" to "Pernas",
        "lower arms" to "Antebraços",
        "lower legs" to "Panturrilhas",
        "back" to "Costas",
        "waist" to "Abdômen",
        "chest" to "Peito",
        "shoulders" to "Ombros",
        "cardio" to "Cardio",
        "neck" to "Pescoço",
    )

    fun bodyPartLabel(bodyPart: String?): String {
        val value = bodyPart?.trim().orEmpty()
        if (value.isEmpty()) return ""
        return BODY_PART_LABELS[value.lowercase(PT_BR)] ?: value
    }

    /** "0:45", "1:05": usado pelo cronômetro de descanso. */
    fun countdown(seconds: Int): String {
        val safe = seconds.coerceAtLeast(0)
        return String.format(PT_BR, "%d:%02d", safe / 60, safe % 60)
    }

    fun formatDecimal(value: Double): String =
        if (value % 1.0 == 0.0) value.toInt().toString()
        else String.format(PT_BR, "%.1f", value)
}
