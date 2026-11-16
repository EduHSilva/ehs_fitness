package com.ehs.fitness.domain.workouts

import com.ehs.fitness.data.model.Workout

object WeekPlan {
    /** ISO-8601: 1 = segunda ... 7 = domingo, igual ao `dayOfWeek` da API. */
    val days: List<Pair<Int, String>> = listOf(
        1 to "Segunda",
        2 to "Terça",
        3 to "Quarta",
        4 to "Quinta",
        5 to "Sexta",
        6 to "Sábado",
        7 to "Domingo",
    )

    fun dayLabel(dayOfWeek: Int): String =
        days.firstOrNull { it.first == dayOfWeek }?.second ?: "Dia $dayOfWeek"

    fun dayShort(dayOfWeek: Int): String = dayLabel(dayOfWeek).take(1)

    fun todayWorkout(workouts: List<Workout>, isoDay: Int): Workout? =
        workouts.firstOrNull { it.dayOfWeek == isoDay }

    /** Próximo treino a partir de hoje (inclusive), dando a volta na semana. */
    fun nextWorkout(workouts: List<Workout>, isoDay: Int): Pair<Int, Workout>? {
        val planned = workouts.filter { it.dayOfWeek in 1..7 }
        if (planned.isEmpty()) return null
        for (offset in 0..6) {
            val day = ((isoDay - 1 + offset) % 7) + 1
            planned.firstOrNull { it.dayOfWeek == day }?.let { return day to it }
        }
        return null
    }
}
