import { errorMessage } from '~/types/api'
import { fetchWorkoutWeek, fetchWorkouts, saveWorkoutWeek } from './workout.api'
import type { Workout, WorkoutWeekDay } from './types'

export function useWeekPlan(studentId: Ref<string>, canManage: Ref<boolean>) {
  const { request } = useFitnessApi()
  const days = ref<WorkoutWeekDay[]>([])
  const workouts = ref<Workout[]>([])
  const loading = ref(true)
  const saving = ref(false)
  const error = ref('')
  const actionError = ref('')
  const feedback = ref('')

  const readOnly = computed(() => !canManage.value)
  const plan = computed(() => new Map(days.value.map(day => [day.dayOfWeek, day.workoutId])))
  const plannedCount = computed(() => days.value.filter(day => day.workoutId !== null).length)

  async function load() {
    loading.value = true
    error.value = ''
    try {
      const [week, list] = await Promise.all([
        fetchWorkoutWeek(request, studentId.value || undefined),
        fetchWorkouts(request, studentId.value || undefined)
      ])
      workouts.value = list.data ?? []
      days.value = normalizeWeek(week.data ?? [], workouts.value)
    } catch (cause) {
      error.value = errorMessage(cause, 'Nao foi possivel carregar a agenda.')
    } finally {
      loading.value = false
    }
  }

  function assign(day: number, workoutId: number | null) {
    if (readOnly.value) return
    actionError.value = ''
    feedback.value = ''
    days.value = days.value.map((item) => {
      if (item.dayOfWeek === day) return { ...item, workoutId, name: workoutName(workoutId) }
      if (workoutId !== null && item.workoutId === workoutId) return { ...item, workoutId: null, name: '' }
      return item
    })
  }

  async function save() {
    if (readOnly.value || saving.value) return
    saving.value = true
    actionError.value = ''
    feedback.value = ''
    try {
      const response = await saveWorkoutWeek(request, days.value, studentId.value || undefined)
      days.value = normalizeWeek(response.data ?? days.value, workouts.value)
      feedback.value = 'Agenda salva.'
    } catch (cause) {
      actionError.value = errorMessage(cause, 'Nao foi possivel salvar a agenda.')
    } finally {
      saving.value = false
    }
  }

  function workoutName(workoutId: number | null): string {
    if (workoutId === null) return ''
    return workouts.value.find(workout => workout.id === workoutId)?.name ?? ''
  }

  function normalizeWeek(week: WorkoutWeekDay[], list: Workout[]): WorkoutWeekDay[] {
    const byDay = new Map(week.map(day => [day.dayOfWeek, day]))
    return [1, 2, 3, 4, 5, 6, 7].map((day) => {
      const entry = byDay.get(day)
      const workoutId = entry?.workoutId ?? null
      return {
        dayOfWeek: day,
        workoutId,
        name: entry?.name || list.find(workout => workout.id === workoutId)?.name || ''
      }
    })
  }

  return {
    days,
    workouts,
    loading,
    saving,
    error,
    actionError,
    feedback,
    readOnly,
    plan,
    plannedCount,
    load,
    assign,
    save,
    workoutName
  }
}
