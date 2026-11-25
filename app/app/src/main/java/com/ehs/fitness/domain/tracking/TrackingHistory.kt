package com.ehs.fitness.domain.tracking

import com.ehs.fitness.data.model.TrackingConfig
import com.ehs.fitness.data.model.TrackingDay
import java.time.LocalDate
import java.time.YearMonth
import kotlin.math.roundToInt

enum class TrackingType(val label: String) {
    CREATINE("Creatina"),
    WORKOUT("Treino"),
    CARDIO("Cardio"),
}

data class TrackingStats(
    val registeredDays: Int = 0,
    val creatineDays: Int = 0,
    val workoutDays: Int = 0,
    val cardioDays: Int = 0,
    val cardioMinutes: Double = 0.0,
)

/** Regras puras do calendário mensal de acompanhamento. */
object TrackingHistory {
    private val monthNames = listOf(
        "Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho",
        "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro",
    )

    fun monthLabel(month: YearMonth): String = "${monthNames[month.monthValue - 1]} de ${month.year}"

    fun activeTypes(config: TrackingConfig?): List<TrackingType> = buildList {
        if (config?.showCreatine == true) add(TrackingType.CREATINE)
        if (config?.showWorkout == true) add(TrackingType.WORKOUT)
        if (config?.cardioEnabled == true) add(TrackingType.CARDIO)
    }

    /** Tipos registrados no dia, limitados ao que o acompanhamento pede. */
    fun typesFor(record: TrackingDay?, config: TrackingConfig?): Set<TrackingType> {
        if (record == null) return emptySet()
        val types = mutableSetOf<TrackingType>()
        if (config?.showCreatine == true && record.creatine) types += TrackingType.CREATINE
        if (config?.showWorkout == true && record.trained) types += TrackingType.WORKOUT
        if (config?.cardioEnabled == true && record.cardio > 0.0) types += TrackingType.CARDIO
        return types
    }

    fun stats(days: List<TrackingDay>, config: TrackingConfig?, month: YearMonth): TrackingStats {
        val active = activeTypes(config).toSet()
        var registered = 0
        var creatine = 0
        var workout = 0
        var cardioDays = 0
        var cardioMinutes = 0.0
        for (day in days) {
            if (YearMonth.from(day.date) != month) continue
            val hasCreatine = TrackingType.CREATINE in active && day.creatine
            val hasWorkout = TrackingType.WORKOUT in active && day.trained
            val hasCardio = TrackingType.CARDIO in active && day.cardio > 0.0
            if (hasCreatine) creatine++
            if (hasWorkout) workout++
            if (hasCardio) {
                cardioDays++
                cardioMinutes += day.cardio
            }
            if (hasCreatine || hasWorkout || hasCardio) registered++
        }
        return TrackingStats(
            registeredDays = registered,
            creatineDays = creatine,
            workoutDays = workout,
            cardioDays = cardioDays,
            cardioMinutes = cardioMinutes,
        )
    }

    /** Dias do mês cercados por `null` para completar as semanas desde o domingo. */
    fun monthDays(month: YearMonth): List<LocalDate?> {
        val leading = month.atDay(1).dayOfWeek.value % 7
        val cells = MutableList<LocalDate?>(leading + month.lengthOfMonth()) { null }
        for (day in 1..month.lengthOfMonth()) {
            cells[leading + day - 1] = month.atDay(day)
        }
        while (cells.size % 7 != 0) cells.add(null)
        return cells
    }

    fun range(month: YearMonth): Pair<LocalDate, LocalDate> = month.atDay(1) to month.atEndOfMonth()

    fun shiftMonth(month: YearMonth, delta: Long): YearMonth = month.plusMonths(delta)

    fun formatMinutes(value: Double): String {
        val total = value.roundToInt()
        if (total < 60) return "$total min"
        val hours = total / 60
        val minutes = total % 60
        return if (minutes == 0) "${hours}h" else "${hours}h${minutes.toString().padStart(2, '0')}"
    }
}
