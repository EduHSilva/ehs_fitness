import { errorMessage } from '~/types/api'
import { createWorkout, deleteWorkout, fetchExerciseCatalog, fetchWorkouts, updateWorkout } from './workout.api'
import type { Exercise, Workout, WorkoutExercise } from './types'

export function useWorkouts(studentId: Ref<string>, canManage: Ref<boolean>) {
  const { request } = useFitnessApi()
  const workouts = ref<Workout[]>([])
  const catalog = ref<Exercise[]>([])
  const loading = ref(true)
  const saving = ref(false)
  const error = ref('')
  const actionError = ref('')
  const formError = ref('')
  const showForm = ref(false)
  const editingId = ref<number | null>(null)
  const expandedWorkoutId = ref<number | null>(null)
  const workoutName = ref('')
  const selectedExercises = ref<WorkoutExercise[]>([])
  const exerciseSearch = ref('')
  const selectedBodyPart = ref('')
  const pendingDelete = ref<Workout | null>(null)
  const deleting = ref(false)

  const readOnly = computed(() => !canManage.value)
  const bodyParts = computed(() => [...new Set(catalog.value.map(item => item.body_part?.trim()).filter((part): part is string => Boolean(part)))].sort((a, b) => a.localeCompare(b, 'pt-BR')))
  const filteredCatalog = computed(() => {
    const search = exerciseSearch.value.trim().toLocaleLowerCase('pt-BR')
    return catalog.value.filter(exercise => (!selectedBodyPart.value || exercise.body_part === selectedBodyPart.value) && `${exercise.name} ${exercise.body_part ?? ''}`.toLocaleLowerCase('pt-BR').includes(search))
  })

  async function load() {
    loading.value = true
    error.value = ''
    try {
      workouts.value = (await fetchWorkouts(request, studentId.value || undefined)).data ?? []
    } catch (cause) {
      error.value = errorMessage(cause, 'Nao foi possivel carregar os treinos.')
    } finally {
      loading.value = false
    }
  }

  async function ensureCatalog() {
    if (catalog.value.length) return
    try {
      catalog.value = (await fetchExerciseCatalog(request)).data ?? []
    } catch {
      formError.value = 'Nao foi possivel carregar os exercicios.'
    }
  }

  async function openForm(workout?: Workout) {
    if (readOnly.value) return
    showForm.value = true
    editingId.value = workout?.id ?? null
    workoutName.value = workout?.name ?? ''
    selectedExercises.value = (workout?.exercises ?? []).map(exercise => ({
      ...exercise,
      series: exercise.series ?? 3,
      repetitions: exercise.repetitions ?? 12
    }))
    formError.value = ''
    exerciseSearch.value = ''
    selectedBodyPart.value = ''
    await ensureCatalog()
  }

  function addExercise(exercise: Exercise) {
    if (!selectedExercises.value.some(item => item.id === exercise.id)) selectedExercises.value.push({ ...exercise, series: 3, repetitions: 12 })
  }

  function removeExercise(id: number) {
    selectedExercises.value = selectedExercises.value.filter(item => item.id !== id)
  }

  function cancelForm() {
    showForm.value = false
    editingId.value = null
    workoutName.value = ''
    selectedExercises.value = []
    exerciseSearch.value = ''
    selectedBodyPart.value = ''
    formError.value = ''
  }

  async function save() {
    if (readOnly.value) return
    formError.value = ''
    if (!workoutName.value.trim()) return void (formError.value = 'Informe o nome do treino.')
    if (!selectedExercises.value.length) return void (formError.value = 'Adicione pelo menos um exercicio.')
    saving.value = true
    try {
      const input = {
        name: workoutName.value.trim(),
        exercises: selectedExercises.value,
        studentId: studentId.value || undefined
      }
      if (editingId.value) await updateWorkout(request, editingId.value, input)
      else await createWorkout(request, input)
      cancelForm()
      await load()
    } catch (cause) {
      formError.value = errorMessage(cause, 'Nao foi possivel salvar o treino.')
    } finally {
      saving.value = false
    }
  }

  function requestDelete(workout: Workout) {
    if (readOnly.value) return
    actionError.value = ''
    pendingDelete.value = workout
  }

  function cancelDelete() {
    pendingDelete.value = null
  }

  async function confirmDelete() {
    const workout = pendingDelete.value
    if (!workout || deleting.value || readOnly.value) return
    deleting.value = true
    actionError.value = ''
    try {
      await deleteWorkout(request, workout.id, studentId.value || undefined)
      pendingDelete.value = null
      await load()
    } catch (cause) {
      actionError.value = errorMessage(cause, 'Nao foi possivel excluir o treino.')
    } finally {
      deleting.value = false
    }
  }

  function exerciseDetail(exercise: Exercise) {
    return [exercise.series ? `${exercise.series} series` : '', exercise.repetitions ? `${exercise.repetitions} repeticoes` : '', exercise.load ? `${exercise.load} kg` : ''].filter(Boolean).join(' · ') || exercise.body_part || 'Sem detalhes'
  }

  return {
    workouts,
    catalog,
    loading,
    saving,
    error,
    actionError,
    formError,
    showForm,
    editingId,
    expandedWorkoutId,
    workoutName,
    selectedExercises,
    exerciseSearch,
    selectedBodyPart,
    pendingDelete,
    deleting,
    readOnly,
    bodyParts,
    filteredCatalog,
    load,
    openForm,
    addExercise,
    removeExercise,
    cancelForm,
    save,
    requestDelete,
    cancelDelete,
    confirmDelete,
    exerciseDetail
  }
}
