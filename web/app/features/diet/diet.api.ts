import type { ApiRequest, ApiResponse } from '~/types/api'
import type { Food, Meal, MealFood } from './types'

function studentQuery(studentId?: string) {
  return studentId ? `?student_id=${encodeURIComponent(studentId)}` : ''
}

function planQuery(id: number, studentId?: string) {
  return `?id=${encodeURIComponent(id)}${studentId ? `&student_id=${encodeURIComponent(studentId)}` : ''}`
}

export function fetchMeals(request: ApiRequest, studentId?: string) {
  return request<ApiResponse<Meal[]>>(`/fitness/diet/meals${studentQuery(studentId)}`)
}

export function searchFoods(request: ApiRequest, query: string) {
  return request<ApiResponse<Food[]>>(`/fitness/diet/meal/food?query=${encodeURIComponent(query)}`)
}

export function createMeal(request: ApiRequest, input: { name: string, hour: string, foods: MealFood[], studentId?: string }) {
  return request('/fitness/diet/meal', {
    method: 'POST',
    body: {
      name: input.name,
      hour: input.hour,
      ...(input.studentId ? { student_id: input.studentId } : {}),
      foods: foodsPayload(input.foods)
    }
  })
}

export function updateMeal(request: ApiRequest, id: number, input: { name: string, hour: string, foods: MealFood[], studentId?: string }) {
  return request(`/fitness/diet/meal${planQuery(id, input.studentId)}`, {
    method: 'PUT',
    body: {
      name: input.name,
      hour: input.hour,
      ...(input.studentId ? { student_id: input.studentId } : {}),
      foods: foodsPayload(input.foods)
    }
  })
}

export function deleteMeal(request: ApiRequest, id: number, studentId?: string) {
  return request(`/fitness/diet/meal${planQuery(id, studentId)}`, { method: 'DELETE' })
}

function foodsPayload(foods: MealFood[]) {
  return foods.map(({ food_id, quantity, observation }) => ({ food_id, quantity, observation }))
}
