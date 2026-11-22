package com.ehs.fitness.domain.workouts

import com.ehs.fitness.data.model.Workout
import com.ehs.fitness.data.model.WorkoutExercise
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TrainingSessionTest {
    private val supino = WorkoutExercise(
        exerciseId = 1,
        name = "Supino reto",
        series = 2,
        repetitions = 10,
        load = 20.0,
        restSeconds = 60,
    )
    private val rosca = WorkoutExercise(
        exerciseId = 2,
        name = "Rosca direta",
        series = 1,
        repetitions = 12,
        restSeconds = 0,
    )
    private val workout = Workout(
        id = 7,
        name = "Peito e braços",
        dayOfWeek = 1,
        exercises = listOf(supino, rosca),
    )

    @Test
    fun `inicia no primeiro exercicio e sem descanso`() {
        val session = TrainingSession.start(workout, nowMillis = 1_000L)

        assertNotNull(session)
        assertEquals(7, session!!.workoutId)
        assertEquals("Supino reto", session.currentExercise?.name)
        assertEquals(1, session.currentSeries)
        assertEquals(3, session.totalSeries)
        assertFalse(session.isResting(1_000L))
    }

    @Test
    fun `treino sem exercicios nao inicia`() {
        assertNull(TrainingSession.start(Workout(id = 8, name = "Vazio"), nowMillis = 0L))
    }

    @Test
    fun `concluir serie inicia descanso do exercicio`() {
        val session = TrainingSession.start(workout, nowMillis = 1_000L)!!
        val next = session.completeSeries(nowMillis = 2_000L)

        assertEquals(2, next.currentSeries)
        assertEquals(1, next.completedSeriesOverall)
        assertEquals(62_000L, next.restEndsAtMillis)
        assertEquals(60, next.restRemainingSeconds(2_000L))
        assertEquals(30, next.restRemainingSeconds(32_000L))
    }

    @Test
    fun `ultima serie avanca para o proximo exercicio`() {
        val session = TrainingSession.start(workout, nowMillis = 0L)!!
            .completeSeries(nowMillis = 0L)

        val next = session.completeSeries(nowMillis = 1_000L)

        assertEquals(1, next.exerciseIndex)
        assertEquals(1, next.currentSeries)
        assertEquals("Rosca direta", next.currentExercise?.name)
        assertEquals(61_000L, next.restEndsAtMillis)
    }

    @Test
    fun `trocar o exercicio ativo preserva o progresso de cada um`() {
        val session = TrainingSession.start(workout, nowMillis = 0L)!!
            .completeSeries(nowMillis = 0L)

        val switched = session.selectExercise(1, nowMillis = 5_000L)

        assertEquals(1, switched.exerciseIndex)
        assertEquals("Rosca direta", switched.currentExercise?.name)
        assertEquals(1, switched.currentSeries)
        assertEquals(1, switched.completedSeriesOverall)
        assertEquals(1, switched.completedSeriesFor(0))
        assertEquals(5_000L, switched.updatedAtMillis)
        assertEquals("Supino reto", switched.nextPendingExercise?.name)
    }

    @Test
    fun `voltar ao exercicio anterior mantem a serie em andamento`() {
        val session = TrainingSession.start(workout, nowMillis = 0L)!!
            .completeSeries(nowMillis = 0L)
            .selectExercise(1, nowMillis = 0L)
            .selectExercise(0, nowMillis = 0L)

        assertEquals(0, session.exerciseIndex)
        assertEquals(2, session.currentSeries)
        assertEquals(1, session.completedSeriesOverall)
    }

    @Test
    fun `exercicio concluido ou indice invalido nao muda o ativo`() {
        val session = TrainingSession.start(workout, nowMillis = 0L)!!
            .selectExercise(1, nowMillis = 0L)
            .completeSeries(nowMillis = 0L)

        assertEquals(0, session.exerciseIndex)
        assertTrue(session.isExerciseDone(1))
        assertEquals(session, session.selectExercise(1, nowMillis = 1_000L))
        assertEquals(session, session.selectExercise(9, nowMillis = 1_000L))
        assertEquals(session, session.selectExercise(-1, nowMillis = 1_000L))
    }

    @Test
    fun `treino so encerra quando todos os exercicios terminam`() {
        val session = TrainingSession.start(workout, nowMillis = 0L)!!
            .selectExercise(1, nowMillis = 0L)
            .completeSeries(nowMillis = 0L)

        assertFalse(session.isFinished)
        assertEquals(0, session.exerciseIndex)
        assertNull(session.restEndsAtMillis)

        val midSupino = session.completeSeries(nowMillis = 0L)
        assertFalse(midSupino.isFinished)
        assertEquals(60_000L, midSupino.restEndsAtMillis)

        val finished = midSupino.completeSeries(nowMillis = 0L)
        assertTrue(finished.isFinished)
        assertEquals(3, finished.completedSeriesOverall)
        assertNull(finished.restEndsAtMillis)
    }

    @Test
    fun `ultima serie do ultimo exercicio encerra o treino sem descanso`() {
        val session = TrainingSession.start(workout, nowMillis = 0L)!!
            .completeSeries(nowMillis = 0L)
            .completeSeries(nowMillis = 0L)

        val finished = session.completeSeries(nowMillis = 0L)

        assertTrue(finished.isFinished)
        assertNull(finished.restEndsAtMillis)
        assertEquals(3, finished.completedSeriesOverall)
    }

    @Test
    fun `descanso vencido e limpo ao retomar`() {
        val session = TrainingSession.start(workout, nowMillis = 0L)!!
            .completeSeries(nowMillis = 1_000L)

        val stillResting = session.settleRest(nowMillis = 30_000L)
        assertEquals(61_000L, stillResting.restEndsAtMillis)

        val settled = session.settleRest(nowMillis = 90_000L)
        assertNull(settled.restEndsAtMillis)
        assertFalse(settled.isResting(90_000L))
    }

    @Test
    fun `pular descanso mantem a serie atual`() {
        val session = TrainingSession.start(workout, nowMillis = 0L)!!
            .completeSeries(nowMillis = 0L)
            .skipRest(nowMillis = 2_000L)

        assertNull(session.restEndsAtMillis)
        assertEquals(2, session.currentSeries)
    }

    @Test
    fun `sessao sobrevive a serializacao local`() {
        val session = TrainingSession.start(workout, nowMillis = 1_000L)!!
            .completeSeries(nowMillis = 2_000L)
        val json = Json.encodeToString(TrainingSession.serializer(), session)
        val restored = Json.decodeFromString(TrainingSession.serializer(), json)

        assertEquals(session, restored)
        assertEquals(60, restored.restRemainingSeconds(2_000L))
    }

    @Test
    fun `ajuste de carga vale so para o exercicio atual`() {
        val session = TrainingSession.start(workout, nowMillis = 0L)!!

        val updated = session.updateLoad(42.5, nowMillis = 500L)

        assertEquals(42.5, updated.currentExercise!!.load!!, 0.0)
        assertEquals(42.5, updated.exercises.first().load!!, 0.0)
        assertNull(updated.exercises[1].load)
        assertEquals(500L, updated.updatedAtMillis)

        val cleared = updated.updateLoad(0.0, nowMillis = 600L)
        assertNull(cleared.currentExercise!!.load)
    }

    @Test
    fun `cronometro e rotulo da parte do corpo`() {
        assertEquals("0:45", WorkoutFormat.countdown(45))
        assertEquals("1:05", WorkoutFormat.countdown(65))
        assertEquals("Braços", WorkoutFormat.bodyPartLabel("upper arms"))
        assertEquals("Costas", WorkoutFormat.bodyPartLabel(" Back "))
        assertEquals("", WorkoutFormat.bodyPartLabel(null))
    }

    @Test
    fun `cardio ocupa uma etapa unica com o tempo prescrito`() {
        val cardio = WorkoutExercise(
            exerciseId = 3,
            name = "Esteira",
            bodyPart = "cardio",
            series = 3,
            repetitions = 12,
            durationSeconds = 1_200,
            restSeconds = 60,
        )
        val session = TrainingSession.start(
            Workout(id = 9, name = "Cardio", exercises = listOf(cardio)),
            nowMillis = 0L,
        )!!

        assertEquals(1, session.totalSeries)
        assertEquals("20 min · 60s de descanso", WorkoutFormat.exerciseDetail(session.currentExercise!!))

        val finished = session.completeSeries(nowMillis = 1_000L)
        assertTrue(finished.isFinished)
    }

    @Test
    fun `cardio sem tempo prescrito continua por tempo`() {
        assertEquals("por tempo", WorkoutFormat.durationLabel(null))
        assertEquals("por tempo", WorkoutFormat.durationLabel(0))
        assertEquals("20 min", WorkoutFormat.durationLabel(1_200))
    }
}
