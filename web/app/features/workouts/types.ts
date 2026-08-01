export type Exercise = {
  id: number
  name: string
  series?: number
  repetitions?: number
  load?: number
  rest_seconds?: number
  notes?: string
  body_part?: string
}

export type Workout = { id: number, name: string, createAt?: string, exercises?: Exercise[] }
export type WorkoutExercise = Exercise & { series: number, repetitions: number }
