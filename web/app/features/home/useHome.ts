import { errorMessage } from '~/types/api'
import { loadHomeData } from './home.api'
import type { HomeMeal, HomeNextWorkout, HomeWeekDay } from './types'
import type { Workout, WorkoutWeekDay } from '~/features/workouts/types'
import { WEEK_DAYS, dayLabel, isoDay } from '~/features/workouts/week'

export function useHome(studentId?: Ref<string>) {
  const { request } = useFitnessApi()
  const loading = ref(true)
  const error = ref('')
  const workouts = ref<Workout[]>([])
  const week = ref<WorkoutWeekDay[]>([])
  const meals = ref<HomeMeal[]>([])
  const today = isoDay()

  const planned = computed(() => week.value.filter(day => day.workoutId !== null))
  const totalWorkouts = computed(() => workouts.value.length)
  const totalMeals = computed(() => meals.value.length)
  const activeDays = computed(() => planned.value.length)

  function workoutById(id: number | null): Workout | null {
    if (id === null) return null
    return workouts.value.find(workout => workout.id === id) ?? null
  }

  function plannedWorkout(day: number): Workout | null {
    return workoutById(week.value.find(item => item.dayOfWeek === day)?.workoutId ?? null)
  }

  const todayWorkout = computed(() => plannedWorkout(today))

  /** Próximo treino a partir de hoje (inclusive), dando a volta na semana. */
  const nextWorkout = computed<HomeNextWorkout | null>(() => {
    for (let offset = 0; offset < 7; offset += 1) {
      const day = ((today - 1 + offset) % 7) + 1
      const workout = plannedWorkout(day)
      if (workout) return { day, label: dayLabel(day), workout }
    }
    return null
  })

  const weekDays = computed<HomeWeekDay[]>(() => WEEK_DAYS.map(item => ({
    day: item.day,
    short: item.short,
    workout: week.value.find(day => day.dayOfWeek === item.day)?.name
      || plannedWorkout(item.day)?.name
      || null,
    today: item.day === today
  })))

  const consistencyMessage = computed(() => {
    if (!activeDays.value) return 'Defina seus treinos na agenda para acompanhar a semana.'
    if (activeDays.value >= 5) return 'Semana cheia! Mantenha o ritmo e celebre cada treino.'
    return 'Você está indo muito bem. Continue assim!'
  })

  function exerciseCount(workout: Workout): number {
    return workout.exercises?.length ?? 0
  }

  function exerciseLabel(workout: Workout): string {
    const count = exerciseCount(workout)
    return count === 1 ? '1 exercício' : `${count} exercícios`
  }

  async function load() {
    loading.value = true
    error.value = ''
    try {
      const data = await loadHomeData(request, studentId?.value || undefined)
      workouts.value = data.workouts
      week.value = data.week
      meals.value = data.meals
    } catch (cause) {
      error.value = errorMessage(cause, 'Não foi possível carregar seu painel.')
    } finally {
      loading.value = false
    }
  }

  return {
    loading,
    error,
    today,
    totalWorkouts,
    totalMeals,
    activeDays,
    todayWorkout,
    nextWorkout,
    weekDays,
    consistencyMessage,
    exerciseCount,
    exerciseLabel,
    load
  }
}
