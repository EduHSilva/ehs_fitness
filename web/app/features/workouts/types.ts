export type Exercise = {
  id: number
  name: string
  series?: number
  repetitions?: number
  load?: number
  rest_seconds?: number
  duration_seconds?: number
  notes?: string
  body_part?: string
  custom?: boolean
}

export type Workout = {
  id: number
  name: string
  dayOfWeek?: number
  createAt?: string
  exercises?: Exercise[]
}

export type WorkoutExercise = Exercise & { series: number, repetitions: number, rest_seconds: number }

/** Exercício no formulário: cardio usa `duration_minutes` em vez de séries/repetições. */
export type WorkoutFormExercise = WorkoutExercise & { duration_minutes?: number }

export type WorkoutWeekDay = {
  dayOfWeek: number
  workoutId: number | null
  name?: string
}
