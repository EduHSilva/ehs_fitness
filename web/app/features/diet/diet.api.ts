import type { ApiRequest, ApiResponse } from '~/types/api'
import type { Food, Meal, MealFood } from './types'

export function fetchMeals(request: ApiRequest) {
  return request<ApiResponse<Meal[]>>('/fitness/diet/meals')
}

export function searchFoods(request: ApiRequest, query: string) {
  return request<ApiResponse<Food[]>>(`/fitness/diet/meal/food?query=${encodeURIComponent(query)}`)
}

export function createMeal(request: ApiRequest, input: { name: string, hour: string, foods: MealFood[], userId?: string }) {
  return request('/fitness/diet/meal', {
    method: 'POST',
    body: {
      name: input.name,
      hour: input.hour,
      ...(input.userId ? { user_id: input.userId } : {}),
      foods: input.foods.map(({ food_id, quantity, obs }) => ({ food_id, quantity, obs }))
    }
  })
}
