import type { ApiRequest, ApiResponse } from '~/types/api'
import type { Exercise, Workout, WorkoutExercise } from './types'

export function fetchWorkouts(request: ApiRequest) {
  return request<ApiResponse<Workout[]>>('/fitness/workouts')
}

export function fetchExerciseCatalog(request: ApiRequest) {
  return request<ApiResponse<Exercise[]>>('/fitness/workout/exercises')
}

export function createWorkout(request: ApiRequest, input: { name: string, exercises: WorkoutExercise[], studentId?: string }) {
  return request('/fitness/workout', {
    method: 'POST',
    body: {
      name: input.name,
      exercises: input.exercises.map(({ id, series, repetitions, load, rest_seconds, notes }) => ({
        exercise_id: id, series, repetitions, load: load || null, rest_seconds: rest_seconds || null, notes: notes || ''
      })),
      ...(input.studentId ? { user_id: input.studentId } : {})
    }
  })
}
