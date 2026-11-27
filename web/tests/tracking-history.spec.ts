import { describe, expect, it } from 'vitest'
import {
  activeTypes,
  availableFilters,
  buildMonthGrid,
  dayTypes,
  formatMinutes,
  historyList,
  monthLabel,
  monthRange,
  monthStats,
  shiftMonth
} from '~/features/tracking/history'
import type { TrackingConfig, TrackingDay } from '~/features/tracking/types'

const config: TrackingConfig = {
  showCreatine: true,
  showWorkout: true,
  dailyCardio: 30
}

function day(eventDate: string, values: Partial<TrackingDay> = {}): TrackingDay {
  return {
    eventDate,
    creatine: false,
    trained: false,
    cardio: 0,
    ...values
  }
}

describe('calendário mensal', () => {
  it('cobre o mês inteiro no intervalo inclusivo', () => {
    expect(monthRange({ year: 2026, month: 10 })).toEqual({
      from: '2026-11-01',
      to: '2026-11-30'
    })
    expect(monthRange({ year: 2024, month: 1 })).toEqual({
      from: '2024-02-01',
      to: '2024-02-29'
    })
  })

  it('avança e volta entre anos', () => {
    expect(shiftMonth({ year: 2026, month: 11 }, 1)).toEqual({ year: 2027, month: 0 })
    expect(shiftMonth({ year: 2026, month: 0 }, -1)).toEqual({ year: 2025, month: 11 })
  })

  it('rotula o mês em português', () => {
    expect(monthLabel({ year: 2026, month: 10 })).toBe('Novembro de 2026')
    expect(monthLabel({ year: 2026, month: 2 })).toBe('Março de 2026')
  })

  it('alinha os dias no domingo e completa as semanas', () => {
    const cells = buildMonthGrid({ year: 2026, month: 3 }, new Date(2026, 3, 10))
    expect(cells).toHaveLength(35)
    expect(cells.slice(0, 3)).toEqual([null, null, null])
    expect(cells[3]).toEqual({ date: '2026-04-01', day: 1, today: false })
    expect(cells[12]).toEqual({ date: '2026-04-10', day: 10, today: true })
    expect(cells[32]).toEqual({ date: '2026-04-30', day: 30, today: false })
    expect(cells.slice(33)).toEqual([null, null])
  })
})

describe('tipos e estatísticas', () => {
  const days = [
    day('2026-11-02', { creatine: true, trained: true, cardio: 20 }),
    day('2026-11-03', { creatine: true }),
    day('2026-11-04', { trained: true, cardio: 45.5 }),
    day('2026-11-05', {})
  ]

  it('respeita o que está ativo no acompanhamento', () => {
    expect(activeTypes(config)).toEqual(['creatine', 'workout', 'cardio'])
    expect(activeTypes({ showCreatine: false, showWorkout: true, dailyCardio: 0 }))
      .toEqual(['workout'])
    expect(availableFilters({ showCreatine: false, showWorkout: true, dailyCardio: 0 })
      .map(item => item.value)).toEqual(['all', 'workout'])
  })

  it('lista só os tipos registrados e ativos', () => {
    expect(dayTypes(days[0], config)).toEqual(['creatine', 'workout', 'cardio'])
    expect(dayTypes(days[1], config)).toEqual(['creatine'])
    expect(dayTypes(days[1], { ...config, showCreatine: false })).toEqual([])
    expect(dayTypes(undefined, config)).toEqual([])
  })

  it('soma dias e minutos do mês', () => {
    expect(monthStats(days, config)).toEqual({
      registeredDays: 3,
      creatineDays: 2,
      workoutDays: 2,
      cardioDays: 2,
      cardioMinutes: 65.5,
      cardioAverage: 32.75
    })
    expect(monthStats(days, { showCreatine: false, showWorkout: false, dailyCardio: 0 }))
      .toEqual({
        registeredDays: 0,
        creatineDays: 0,
        workoutDays: 0,
        cardioDays: 0,
        cardioMinutes: 0,
        cardioAverage: 0
      })
  })

  it('formata minutos de cardio', () => {
    expect(formatMinutes(45)).toBe('45 min')
    expect(formatMinutes(60)).toBe('1h')
    expect(formatMinutes(75)).toBe('1h15')
    expect(formatMinutes(120)).toBe('2h')
  })

  it('normaliza lista, registro único e resposta vazia', () => {
    const single = day('2026-11-25', { creatine: true })
    expect(historyList([days[0], days[1]])).toEqual([days[0], days[1]])
    expect(historyList(single)).toEqual([single])
    expect(historyList(null)).toEqual([])
    expect(historyList(undefined)).toEqual([])
    expect(historyList([{ ...single, eventDate: '' }])).toEqual([])
  })
})
