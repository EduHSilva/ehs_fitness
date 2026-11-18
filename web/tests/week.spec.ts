import { describe, expect, it } from 'vitest'
import { WEEK_DAYS, dayLabel, isoDay } from '~/features/workouts/week'

describe('semana ISO', () => {
  it('usa segunda como 1 e domingo como 7', () => {
    expect(isoDay(new Date(2026, 10, 16, 12))).toBe(1)
    expect(isoDay(new Date(2026, 10, 22, 12))).toBe(7)
  })

  it('lista os sete dias na ordem da API', () => {
    expect(WEEK_DAYS.map(day => day.day)).toEqual([1, 2, 3, 4, 5, 6, 7])
  })

  it('rotula os dias conhecidos e usa fallback', () => {
    expect(dayLabel(1)).toBe('Segunda')
    expect(dayLabel(7)).toBe('Domingo')
    expect(dayLabel(9)).toBe('Dia 9')
  })
})
