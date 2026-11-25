import { errorMessage } from '~/types/api'
import { fetchTrackingConfig, fetchTrackingHistory } from './tracking.api'
import type { TrackingConfig, TrackingDay } from './types'
import {
  activeTypes,
  availableFilters,
  buildMonthGrid,
  currentMonthCursor,
  monthLabel,
  monthRange,
  monthStats,
  shiftMonth,
  type HistoryFilter,
  type MonthCursor
} from './history'

/** Registros diários do aluno autenticado ou do aluno vinculado (`student_id`). */
export function useTrackingHistory(studentId: Ref<string>) {
  const { request } = useFitnessApi()
  const loading = ref(true)
  const refreshing = ref(false)
  const monthLoading = ref(false)
  const error = ref('')
  const config = ref<TrackingConfig | null>(null)
  const days = ref<TrackingDay[]>([])
  const cursor = ref<MonthCursor>(currentMonthCursor())
  const filter = ref<HistoryFilter>('all')
  let configLoaded = false
  let requestId = 0

  const types = computed(() => activeTypes(config.value))
  const filters = computed(() => availableFilters(config.value))
  const stats = computed(() => monthStats(days.value, config.value))
  const grid = computed(() => buildMonthGrid(cursor.value))
  const label = computed(() => monthLabel(cursor.value))
  const hasRecords = computed(() => days.value.length > 0)
  const hasTracking = computed(() => types.value.length > 0)
  const daysByDate = computed(() => new Map(days.value.map(day => [day.eventDate, day])))

  async function loadConfig() {
    if (configLoaded) return
    const response = await fetchTrackingConfig(request, studentId.value || undefined)
    config.value = response.data ?? null
    configLoaded = true
    if (!filters.value.some(item => item.value === filter.value)) filter.value = 'all'
  }

  async function loadMonth() {
    const current = ++requestId
    const { from, to } = monthRange(cursor.value)
    monthLoading.value = true
    error.value = ''
    try {
      const response = await fetchTrackingHistory(request, {
        from,
        to,
        studentId: studentId.value || undefined
      })
      if (current !== requestId) return
      days.value = response.data ?? []
    } catch (cause) {
      if (current !== requestId) return
      days.value = []
      error.value = errorMessage(cause, 'Não foi possível carregar os registros.')
    } finally {
      if (current === requestId) monthLoading.value = false
    }
  }

  async function load() {
    loading.value = true
    error.value = ''
    try {
      await loadConfig()
      await loadMonth()
    } catch (cause) {
      error.value = errorMessage(cause, 'Não foi possível carregar os registros.')
    } finally {
      loading.value = false
    }
  }

  async function refresh() {
    if (refreshing.value) return
    refreshing.value = true
    try {
      await loadConfig()
      await loadMonth()
    } catch (cause) {
      error.value = errorMessage(cause, 'Não foi possível carregar os registros.')
    } finally {
      refreshing.value = false
    }
  }

  function changeMonth(delta: number) {
    cursor.value = shiftMonth(cursor.value, delta)
    void loadMonth()
  }

  function goToday() {
    cursor.value = currentMonthCursor()
    void loadMonth()
  }

  function changeFilter(next: HistoryFilter) {
    if (next !== 'all' && !types.value.includes(next)) return
    filter.value = next
  }

  function recordFor(date: string) {
    return daysByDate.value.get(date)
  }

  return {
    loading,
    refreshing,
    monthLoading,
    error,
    config,
    cursor,
    filter,
    types,
    filters,
    stats,
    grid,
    label,
    hasRecords,
    hasTracking,
    days,
    load,
    refresh,
    changeMonth,
    goToday,
    changeFilter,
    recordFor
  }
}
