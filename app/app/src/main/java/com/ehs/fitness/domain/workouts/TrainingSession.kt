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
    /** Séries concluídas por posição no plano; a ordem dos exercícios nunca muda. */
    val completedByExercise: Map<Int, Int> = emptyMap(),
    val restEndsAtMillis: Long? = null,
    val startedAtMillis: Long = 0,
    val updatedAtMillis: Long = 0,
) {
    val currentExercise: TrainingExercise? get() = exercises.getOrNull(exerciseIndex)

    val isFinished: Boolean
        get() = currentExercise == null ||
            exercises.indices.all { completedSeriesFor(it) >= exercises[it].totalSeries }

    val currentSeries: Int get() = completedSeriesFor(exerciseIndex) + 1

    val totalSeries: Int get() = exercises.sumOf { it.totalSeries }

    val completedSeriesOverall: Int
        get() = exercises.indices.sumOf { index ->
            completedSeriesFor(index).coerceAtMost(exercises[index].totalSeries)
        }

    /** Minutos de cardio já concluídos; usado no acompanhamento diário ao encerrar o treino. */
    val completedCardioMinutes: Double
        get() = exercises.indices.sumOf { index ->
            val exercise = exercises[index]
            if (exercise.isCardio && isExerciseDone(index)) {
                (exercise.durationSeconds ?: 0) / 60.0
            } else {
                0.0
            }
        }

    /** Próximo pendente depois do ativo, na ordem do plano; volta ao começo ao chegar no fim. */
    val nextPendingExercise: TrainingExercise?
        get() = nextPendingIndex(exerciseIndex)?.let { exercises[it] }

    fun completedSeriesFor(index: Int): Int = completedByExercise[index] ?: 0

    fun isExerciseDone(index: Int): Boolean =
        exercises.getOrNull(index)?.let { completedSeriesFor(index) >= it.totalSeries } ?: false

    fun restRemainingMillis(nowMillis: Long): Long =
        (restEndsAtMillis?.minus(nowMillis) ?: 0L).coerceAtLeast(0L)

    fun restRemainingSeconds(nowMillis: Long): Int =
        ((restRemainingMillis(nowMillis) + 999L) / 1000L).toInt()

    fun isResting(nowMillis: Long): Boolean = restRemainingMillis(nowMillis) > 0L

    /** Troca o exercício ativo sem mover nada; o progresso de cada um é preservado. */
    fun selectExercise(index: Int, nowMillis: Long): TrainingSession {
        if (index !in exercises.indices) return this
        if (index == exerciseIndex || isExerciseDone(index)) return this
        return copy(exerciseIndex = index, updatedAtMillis = nowMillis)
    }

    /** Avança uma série; inicia o descanso do exercício concluído quando há próximo passo. */
    fun completeSeries(nowMillis: Long): TrainingSession {
        val exercise = currentExercise ?: return this
        val done = completedSeriesFor(exerciseIndex)
        if (done >= exercise.totalSeries) return this
        val completed = completedByExercise + (exerciseIndex to done + 1)
        val exerciseFinished = done + 1 >= exercise.totalSeries
        val nextIndex = if (exerciseFinished) nextPendingIndex(exerciseIndex) else exerciseIndex
        val rest = if (nextIndex == null) 0 else exercise.rest
        return copy(
            exerciseIndex = nextIndex ?: exerciseIndex,
            completedByExercise = completed,
            restEndsAtMillis = if (rest > 0) nowMillis + rest * 1000L else null,
            updatedAtMillis = nowMillis,
        )
    }

    fun skipRest(nowMillis: Long): TrainingSession =
        copy(restEndsAtMillis = null, updatedAtMillis = nowMillis)

    /** Ajusta a carga do exercício atual; 0 remove a carga prescrita. */
    fun updateLoad(load: Double, nowMillis: Long): TrainingSession {
        val exercise = currentExercise ?: return this
        val safe = load.takeIf { it > 0.0 }
        return copy(
            exercises = exercises.map {
                if (it.exerciseId == exercise.exerciseId) it.copy(load = safe) else it
            },
            updatedAtMillis = nowMillis,
        )
    }

    /** Limpa um descanso que já terminou enquanto o app estava fechado. */
    fun settleRest(nowMillis: Long): TrainingSession =
        if (restEndsAtMillis != null && !isResting(nowMillis)) skipRest(nowMillis) else this

    private fun nextPendingIndex(after: Int): Int? {
        val forward = exercises.indices.filter { it > after }
        val fromStart = exercises.indices.filter { it < after }
        return (forward + fromStart).firstOrNull { !isExerciseDone(it) }
    }

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
