export const WEEK_DAYS = [
  { day: 1, short: 'S', label: 'Segunda' },
  { day: 2, short: 'T', label: 'Terça' },
  { day: 3, short: 'Q', label: 'Quarta' },
  { day: 4, short: 'Q', label: 'Quinta' },
  { day: 5, short: 'S', label: 'Sexta' },
  { day: 6, short: 'S', label: 'Sábado' },
  { day: 7, short: 'D', label: 'Domingo' }
] as const

/** `Date.getDay()` devolve 0 para domingo; a API usa 1 = segunda ... 7 = domingo. */
export function isoDay(date = new Date()): number {
  const weekday = date.getDay()
  return weekday === 0 ? 7 : weekday
}

export function dayLabel(day: number): string {
  return WEEK_DAYS.find(item => item.day === day)?.label ?? `Dia ${day}`
}
