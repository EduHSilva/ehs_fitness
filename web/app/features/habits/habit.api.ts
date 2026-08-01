import type { ApiRequest, ApiResponse } from '~/types/api'
import type { Habit } from './types'

// TODO: implementar estes endpoints no backend e alinhar os nomes finais dos recursos.
export function fetchHabits(request: ApiRequest, userId?: string) {
  const query = userId ? `?user_id=${encodeURIComponent(userId)}` : ''
  return request<ApiResponse<Habit[]>>(`/fitness/habits${query}`)
}

export function createHabit(request: ApiRequest, habit: Omit<Habit, 'id'>, userId?: string) {
  return request<ApiResponse<Habit>>('/fitness/habits', {
    method: 'POST',
    body: { ...habit, ...(userId ? { user_id: userId } : {}) }
  })
}

export function deleteHabit(request: ApiRequest, id: string) {
  return request(`/fitness/habits/${id}`, { method: 'DELETE' })
}
