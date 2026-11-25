package com.ehs.fitness.domain.tracking

import com.ehs.fitness.data.model.TrackingConfig
import com.ehs.fitness.data.model.TrackingDay
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.YearMonth

class TrackingHistoryTest {
    private val config = TrackingConfig(showCreatine = true, showWorkout = true, dailyCardio = 30.0)

    private fun day(
        date: String,
        creatine: Boolean = false,
        trained: Boolean = false,
        cardio: Double = 0.0,
    ) = TrackingDay(LocalDate.parse(date), creatine = creatine, trained = trained, cardio = cardio)

    @Test
    fun `tipos ativos seguem a configuracao`() {
        assertEquals(
            listOf(TrackingType.CREATINE, TrackingType.WORKOUT, TrackingType.CARDIO),
            TrackingHistory.activeTypes(config),
        )
        assertEquals(
            listOf(TrackingType.WORKOUT),
            TrackingHistory.activeTypes(TrackingConfig(showCreatine = false, showWorkout = true, dailyCardio = 0.0)),
        )
        assertTrue(TrackingHistory.activeTypes(null).isEmpty())
    }

    @Test
    fun `tipos do dia ignoram o que nao esta ativo`() {
        val record = day("2026-11-02", creatine = true, trained = true, cardio = 20.0)
        assertEquals(
            setOf(TrackingType.CREATINE, TrackingType.WORKOUT, TrackingType.CARDIO),
            TrackingHistory.typesFor(record, config),
        )
        assertEquals(
            setOf(TrackingType.WORKOUT),
            TrackingHistory.typesFor(record, TrackingConfig(showCreatine = false, showWorkout = true, dailyCardio = 0.0)),
        )
        assertTrue(TrackingHistory.typesFor(null, config).isEmpty())
    }

    @Test
    fun `estatisticas somam apenas o mes e os tipos ativos`() {
        val days = listOf(
            day("2026-11-02", creatine = true, trained = true, cardio = 20.0),
            day("2026-11-03", creatine = true),
            day("2026-11-04", trained = true, cardio = 45.5),
            day("2026-11-05"),
            day("2026-10-31", creatine = true, cardio = 90.0),
        )
        assertEquals(
            TrackingStats(
                registeredDays = 3,
                creatineDays = 2,
                workoutDays = 2,
                cardioDays = 2,
                cardioMinutes = 65.5,
            ),
            TrackingHistory.stats(days, config, YearMonth.of(2026, 11)),
        )
        assertEquals(
            TrackingStats(),
            TrackingHistory.stats(days, TrackingConfig(showCreatine = false, showWorkout = false, dailyCardio = 0.0), YearMonth.of(2026, 11)),
        )
    }

    @Test
    fun `calendario comeca no domingo e completa as semanas`() {
        val april = TrackingHistory.monthDays(YearMonth.of(2026, 4))
        assertEquals(35, april.size)
        assertEquals(listOf(null, null, null), april.take(3))
        assertEquals(LocalDate.of(2026, 4, 1), april[3])
        assertEquals(LocalDate.of(2026, 4, 30), april[32])
        assertEquals(listOf(null, null), april.takeLast(2))

        assertEquals(28, TrackingHistory.monthDays(YearMonth.of(2026, 2)).size)
    }

    @Test
    fun `troca de mes cruza o ano`() {
        assertEquals(YearMonth.of(2027, 1), TrackingHistory.shiftMonth(YearMonth.of(2026, 12), 1))
        assertEquals(YearMonth.of(2025, 12), TrackingHistory.shiftMonth(YearMonth.of(2026, 1), -1))
    }

    @Test
    fun `formata minutos de cardio`() {
        assertEquals("45 min", TrackingHistory.formatMinutes(45.0))
        assertEquals("1h", TrackingHistory.formatMinutes(60.0))
        assertEquals("1h15", TrackingHistory.formatMinutes(75.0))
        assertEquals("2h", TrackingHistory.formatMinutes(120.0))
    }

    @Test
    fun `rotula o mes em portugues`() {
        assertEquals("Novembro de 2026", TrackingHistory.monthLabel(YearMonth.of(2026, 11)))
        assertEquals("Março de 2026", TrackingHistory.monthLabel(YearMonth.of(2026, 3)))
    }
}
