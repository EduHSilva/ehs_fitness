import type { ApiRequest } from '~/types/api'
import { fetchMeals } from '~/features/diet/diet.api'
import { fetchWorkoutWeek, fetchWorkouts } from '~/features/workouts/workout.api'
import type { HomeData } from './types'

/** O painel agrega treinos, agenda semanal e refeições do aluno. */
export async function loadHomeData(request: ApiRequest, studentId?: string): Promise<HomeData> {
  const [workouts, week, meals] = await Promise.all([
    fetchWorkouts(request, studentId),
    fetchWorkoutWeek(request, studentId),
    fetchMeals(request, studentId)
  ])
  return {
    workouts: workouts.data ?? [],
    week: week.data ?? [],
    meals: meals.data ?? []
  }
}
