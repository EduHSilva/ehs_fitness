import type { TrackingConfig, TrackingDay } from './types'

export type TrackingType = 'creatine' | 'workout' | 'cardio'
export type HistoryFilter = 'all' | TrackingType

export type MonthCursor = {
  year: number
  /** 0 = janeiro ... 11 = dezembro (como `Date.getMonth`). */
  month: number
}

export type CalendarCell = {
  date: string
  day: number
  today: boolean
} | null

export type TrackingStats = {
  registeredDays: number
  creatineDays: number
  workoutDays: number
  cardioDays: number
  cardioMinutes: number
}

export const TYPE_ICONS: Record<TrackingType, string> = {
  creatine: 'i-lucide-pill',
  workout: 'i-lucide-dumbbell',
  cardio: 'i-lucide-heart-pulse'
}

export const TYPE_LABELS: Record<TrackingType, string> = {
  creatine: 'Creatina',
  workout: 'Treino',
  cardio: 'Cardio'
}

export const HISTORY_FILTERS: { value: HistoryFilter, label: string, icon: string }[] = [
  { value: 'all', label: 'Todos', icon: 'i-lucide-layout-grid' },
  { value: 'creatine', label: 'Creatina', icon: TYPE_ICONS.creatine },
  { value: 'workout', label: 'Treino', icon: TYPE_ICONS.workout },
  { value: 'cardio', label: 'Cardio', icon: TYPE_ICONS.cardio }
]

/** Cabeçalho do calendário começando no domingo, como nos calendários pt-BR. */
export const WEEKDAY_LABELS = ['D', 'S', 'T', 'Q', 'Q', 'S', 'S'] as const

const MONTH_NAMES = [
  'Janeiro',
  'Fevereiro',
  'Março',
  'Abril',
  'Maio',
  'Junho',
  'Julho',
  'Agosto',
  'Setembro',
  'Outubro',
  'Novembro',
  'Dezembro'
]

export function currentMonthCursor(date = new Date()): MonthCursor {
  return { year: date.getFullYear(), month: date.getMonth() }
}

export function shiftMonth(cursor: MonthCursor, delta: number): MonthCursor {
  return currentMonthCursor(new Date(cursor.year, cursor.month + delta, 1))
}

export function monthLabel(cursor: MonthCursor): string {
  return `${MONTH_NAMES[cursor.month]} de ${cursor.year}`
}

export function isoDate(date: Date): string {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

/** Intervalo inclusivo do mês, no formato aceito por `?from=&to=`. */
export function monthRange(cursor: MonthCursor): { from: string, to: string } {
  return {
    from: isoDate(new Date(cursor.year, cursor.month, 1)),
    to: isoDate(new Date(cursor.year, cursor.month + 1, 0))
  }
}

/** Dias do mês cercados por células vazias para completar as semanas. */
export function buildMonthGrid(cursor: MonthCursor, today = new Date()): CalendarCell[] {
  const first = new Date(cursor.year, cursor.month, 1)
  const daysInMonth = new Date(cursor.year, cursor.month + 1, 0).getDate()
  const todayIso = isoDate(today)
  const cells: CalendarCell[] = Array.from({ length: first.getDay() }, () => null)
  for (let day = 1; day <= daysInMonth; day++) {
    const date = isoDate(new Date(cursor.year, cursor.month, day))
    cells.push({ date, day, today: date === todayIso })
  }
  while (cells.length % 7 !== 0) cells.push(null)
  return cells
}

export function activeTypes(config: TrackingConfig | null): TrackingType[] {
  if (!config) return []
  const types: TrackingType[] = []
  if (config.showCreatine) types.push('creatine')
  if (config.showWorkout) types.push('workout')
  if (config.dailyCardio > 0) types.push('cardio')
  return types
}

export function availableFilters(config: TrackingConfig | null): typeof HISTORY_FILTERS {
  const types = activeTypes(config)
  return HISTORY_FILTERS.filter(item => item.value === 'all' || types.includes(item.value))
}

/** Tipos efetivamente registrados no dia, limitados ao que o acompanhamento pede. */
export function dayTypes(day: TrackingDay | undefined, config: TrackingConfig | null): TrackingType[] {
  if (!day || !config) return []
  const types: TrackingType[] = []
  if (config.showCreatine && day.creatine) types.push('creatine')
  if (config.showWorkout && day.trained) types.push('workout')
  if (config.dailyCardio > 0 && day.cardio > 0) types.push('cardio')
  return types
}

export function monthStats(days: TrackingDay[], config: TrackingConfig | null): TrackingStats {
  const active = new Set(activeTypes(config))
  const stats: TrackingStats = {
    registeredDays: 0,
    creatineDays: 0,
    workoutDays: 0,
    cardioDays: 0,
    cardioMinutes: 0
  }
  for (const day of days) {
    const creatine = active.has('creatine') && day.creatine
    const workout = active.has('workout') && day.trained
    const cardio = active.has('cardio') && day.cardio > 0
    if (creatine) stats.creatineDays++
    if (workout) stats.workoutDays++
    if (cardio) {
      stats.cardioDays++
      stats.cardioMinutes += day.cardio
    }
    if (creatine || workout || cardio) stats.registeredDays++
  }
  return stats
}

export function formatMinutes(value: number): string {
  const total = Math.round(value)
  if (total < 60) return `${total} min`
  const hours = Math.floor(total / 60)
  const minutes = total % 60
  return minutes ? `${hours}h${String(minutes).padStart(2, '0')}` : `${hours}h`
}
