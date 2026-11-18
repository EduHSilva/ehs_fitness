package com.ehs.fitness.domain.workouts

import com.ehs.fitness.data.model.Workout
import kotlinx.serialization.Serializable

/** Cópia do exercício do plano feita no início do treino, para sobreviver sem rede. */
@Serializable
data class TrainingExercise(
    val exerciseId: Int,
    val name: String,
    val bodyPart: String = "",
    val series: Int = 3,
    val repetitions: Int = 12,
    val load: Double? = null,
    val restSeconds: Int? = null,
    val durationSeconds: Int? = null,
    val notes: String = "",
) {
    val rest: Int get() = restSeconds?.coerceAtLeast(0) ?: 0

    /** Cardio é uma etapa única por tempo, nunca séries de repetições. */
    val isCardio: Boolean get() = WorkoutFormat.isCardio(bodyPart)

    val totalSeries: Int get() = if (isCardio) 1 else series.coerceAtLeast(1)
}

/**
 * Progresso local de uma execução. O descanso usa o relógio de parede
 * (`restEndsAtMillis`) para continuar contando mesmo se o app for encerrado.
 */
@Serializable
data class TrainingSession(
    val workoutId: Int,
    val workoutName: String,
    val exercises: List<TrainingExercise>,
    val exerciseIndex: Int = 0,
    val completedSeries: Int = 0,
    val restEndsAtMillis: Long? = null,
    val startedAtMillis: Long = 0,
    val updatedAtMillis: Long = 0,
) {
    val currentExercise: TrainingExercise? get() = exercises.getOrNull(exerciseIndex)

    val isFinished: Boolean get() = exerciseIndex >= exercises.size

    val currentSeries: Int get() = completedSeries + 1

    val totalSeries: Int get() = exercises.sumOf { it.totalSeries }

    val completedSeriesOverall: Int
        get() = exercises.take(exerciseIndex).sumOf { it.totalSeries } + completedSeries

    fun restRemainingMillis(nowMillis: Long): Long =
        (restEndsAtMillis?.minus(nowMillis) ?: 0L).coerceAtLeast(0L)

    fun restRemainingSeconds(nowMillis: Long): Int =
        ((restRemainingMillis(nowMillis) + 999L) / 1000L).toInt()

    fun isResting(nowMillis: Long): Boolean = restRemainingMillis(nowMillis) > 0L

    /** Avança uma série; inicia o descanso do exercício concluído quando há próximo passo. */
    fun completeSeries(nowMillis: Long): TrainingSession {
        val exercise = currentExercise ?: return this
        val lastSeries = completedSeries + 1 >= exercise.totalSeries
        val nextIndex = if (lastSeries) exerciseIndex + 1 else exerciseIndex
        val finished = nextIndex >= exercises.size
        val rest = if (finished) 0 else exercise.rest
        return copy(
            exerciseIndex = nextIndex,
            completedSeries = if (lastSeries) 0 else completedSeries + 1,
            restEndsAtMillis = if (rest > 0) nowMillis + rest * 1000L else null,
            updatedAtMillis = nowMillis,
        )
    }

    fun skipRest(nowMillis: Long): TrainingSession =
        copy(restEndsAtMillis = null, updatedAtMillis = nowMillis)

    /** Limpa um descanso que já terminou enquanto o app estava fechado. */
    fun settleRest(nowMillis: Long): TrainingSession =
        if (restEndsAtMillis != null && !isResting(nowMillis)) skipRest(nowMillis) else this

    companion object {
        fun start(workout: Workout, nowMillis: Long): TrainingSession? {
            if (workout.exercises.isEmpty()) return null
            return TrainingSession(
                workoutId = workout.id,
                workoutName = workout.name,
                exercises = workout.exercises.map {
                    TrainingExercise(
                        exerciseId = it.exerciseId,
                        name = it.name,
                        bodyPart = it.bodyPart,
                        series = it.series,
                        repetitions = it.repetitions,
                        load = it.load,
                        restSeconds = it.restSeconds,
                        durationSeconds = it.durationSeconds,
                        notes = it.notes,
                    )
                },
                startedAtMillis = nowMillis,
                updatedAtMillis = nowMillis,
            )
        }
    }
}
