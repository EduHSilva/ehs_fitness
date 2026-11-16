import type { Workout, WorkoutWeekDay } from '~/features/workouts/types'

export type HomeMeal = {
  id: number
  name: string
  hour?: string
  foods?: unknown[]
}

export type HomeData = {
  workouts: Workout[]
  week: WorkoutWeekDay[]
  meals: HomeMeal[]
}

export type HomeWeekDay = {
  /** ISO-8601: 1 = segunda ... 7 = domingo, igual ao `dayOfWeek` da API. */
  day: number
  short: string
  workout: string | null
  today: boolean
}

export type HomeNextWorkout = {
  day: number
  label: string
  workout: Workout
}
