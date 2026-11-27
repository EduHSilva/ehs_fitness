package com.ehs.fitness.domain.workouts

import com.ehs.fitness.data.model.Workout
import com.ehs.fitness.data.model.WorkoutExercise
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TrainingLoadsTest {
    private val workout = Workout(
        id = 1,
        name = "Peito",
        exercises = listOf(
            WorkoutExercise(exerciseId = 1, name = "Supino", load = 20.0),
            WorkoutExercise(exerciseId = 2, name = "Crucifixo", load = 10.0),
        ),
    )

    @Test
    fun `sobrepoe apenas as cargas ajustadas`() {
        val applied = TrainingLoads.apply(workout, mapOf(1 to 30.0))

        assertEquals(30.0, applied.exercises[0].load!!, 0.0)
        assertEquals(10.0, applied.exercises[1].load!!, 0.0)
    }

    @Test
    fun `zero remove a carga prescrita`() {
        val applied = TrainingLoads.apply(workout, mapOf(1 to 0.0))

        assertNull(applied.exercises[0].load)
    }

    @Test
    fun `sem ajustes devolve o treino original`() {
        assertEquals(workout, TrainingLoads.apply(workout, emptyMap()))
    }
}