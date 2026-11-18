import type { ApiRequest, ApiResponse } from '~/types/api'
import type { Exercise, Workout, WorkoutExercise, WorkoutWeekDay } from './types'

function studentQuery(studentId?: string) {
  return studentId ? `?student_id=${encodeURIComponent(studentId)}` : ''
}

export function fetchWorkouts(request: ApiRequest, studentId?: string) {
  return request<ApiResponse<Workout[]>>(`/fitness/workouts${studentQuery(studentId)}`)
}

export function fetchWorkoutWeek(request: ApiRequest, studentId?: string) {
  return request<ApiResponse<WorkoutWeekDay[]>>(`/fitness/workouts/week${studentQuery(studentId)}`)
}

export function fetchExerciseCatalog(request: ApiRequest) {
  return request<ApiResponse<Exercise[]>>('/fitness/workout/exercises')
}

export function createExercise(request: ApiRequest, input: { name: string, bodyPart: string }) {
  return request<ApiResponse<Exercise>>('/fitness/workout/exercises', {
    method: 'POST',
    body: {
      name: input.name,
      body_part: input.bodyPart
    }
  })
}

export function updateExercise(request: ApiRequest, id: number, input: { name: string, bodyPart: string }) {
  return request<ApiResponse<Exercise>>(`/fitness/workout/exercises?id=${encodeURIComponent(id)}`, {
    method: 'PUT',
    body: {
      name: input.name,
      body_part: input.bodyPart
    }
  })
}

export function createWorkout(request: ApiRequest, input: { name: string, exercises: WorkoutExercise[], studentId?: string }) {
  return request('/fitness/workout', {
    method: 'POST',
    body: {
      name: input.name,
      exercises: exercisesPayload(input.exercises),
      ...(input.studentId ? { student_id: input.studentId } : {})
    }
  })
}

export function updateWorkout(request: ApiRequest, id: number, input: { name: string, exercises: WorkoutExercise[], studentId?: string }) {
  return request(`/fitness/workout?id=${encodeURIComponent(id)}${input.studentId ? `&student_id=${encodeURIComponent(input.studentId)}` : ''}`, {
    method: 'PUT',
    body: {
      name: input.name,
      exercises: exercisesPayload(input.exercises),
      ...(input.studentId ? { student_id: input.studentId } : {})
    }
  })
}

export function deleteWorkout(request: ApiRequest, id: number, studentId?: string) {
  return request(`/fitness/workout?id=${encodeURIComponent(id)}${studentId ? `&student_id=${encodeURIComponent(studentId)}` : ''}`, {
    method: 'DELETE'
  })
}

export function saveWorkoutWeek(request: ApiRequest, days: WorkoutWeekDay[], studentId?: string) {
  return request<ApiResponse<WorkoutWeekDay[]>>(`/fitness/workouts/week${studentQuery(studentId)}`, {
    method: 'PUT',
    body: {
      ...(studentId ? { student_id: studentId } : {}),
      days: [...days]
        .sort((a, b) => a.dayOfWeek - b.dayOfWeek)
        .map(day => ({ dayOfWeek: day.dayOfWeek, workoutId: day.workoutId ?? null }))
    }
  })
}

function optionalNumber(value: unknown): number | null {
  if (value === '' || value === null || value === undefined) return null
  const parsed = Number(value)
  return Number.isFinite(parsed) ? parsed : null
}

function exercisesPayload(exercises: WorkoutExercise[]) {
  return exercises.map((exercise) => {
    const { id, series, repetitions, load, rest_seconds, duration_seconds, notes, body_part } = exercise
    const cardio = body_part?.trim().toLocaleLowerCase('pt-BR') === 'cardio'
    return {
      exercise_id: id,
      series: cardio ? 1 : (optionalNumber(series) ?? 3),
      repetitions: cardio ? 0 : (optionalNumber(repetitions) ?? 12),
      load: cardio ? null : optionalNumber(load),
      rest_seconds: optionalNumber(rest_seconds),
      duration_seconds: cardio ? optionalNumber(duration_seconds) : null,
      notes: notes ?? ''
    }
  })
}
