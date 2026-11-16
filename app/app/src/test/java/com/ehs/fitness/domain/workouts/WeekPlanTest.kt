package com.ehs.fitness.domain.workouts

import com.ehs.fitness.data.model.Workout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class WeekPlanTest {
    private val peito = Workout(id = 1, name = "Peito", dayOfWeek = 1)
    private val pernas = Workout(id = 2, name = "Pernas", dayOfWeek = 3)

    @Test
    fun `rotulo do dia segue a ISO-8601`() {
        assertEquals("Segunda", WeekPlan.dayLabel(1))
        assertEquals("Domingo", WeekPlan.dayLabel(7))
        assertEquals("Dia 9", WeekPlan.dayLabel(9))
    }

    @Test
    fun `treino de hoje e o proximo considera a volta na semana`() {
        assertEquals(peito, WeekPlan.todayWorkout(listOf(peito, pernas), 1))
        assertNull(WeekPlan.todayWorkout(listOf(peito), 2))

        val next = WeekPlan.nextWorkout(listOf(peito, pernas), 5)
        assertEquals(1, next?.first)
        assertEquals(peito, next?.second)
    }

    @Test
    fun `sem treinos planejados nao existe proximo treino`() {
        assertNull(WeekPlan.nextWorkout(listOf(Workout(id = 3, name = "Sem dia")), 2))
    }
}
