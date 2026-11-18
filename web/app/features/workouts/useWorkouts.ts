import { errorMessage } from '~/types/api'
import { bodyPartLabel } from './bodyParts'
import { createExercise, createWorkout, deleteWorkout, fetchExerciseCatalog, fetchWorkouts, updateExercise, updateWorkout } from './workout.api'
import type { Exercise, Workout, WorkoutExercise, WorkoutFormExercise } from './types'

function isCardioExercise(exercise: Pick<Exercise, 'body_part'>) {
  return exercise.body_part?.trim().toLocaleLowerCase('pt-BR') === 'cardio'
}

function toFormExercise(exercise: Exercise): WorkoutFormExercise {
  if (isCardioExercise(exercise)) {
    return {
      ...exercise,
      series: exercise.series ?? 1,
      repetitions: exercise.repetitions ?? 0,
      rest_seconds: exercise.rest_seconds ?? 0,
      duration_minutes: Math.max(1, Math.round((exercise.duration_seconds ?? 600) / 60))
    }
  }
  return {
    ...exercise,
    series: exercise.series ?? 3,
    repetitions: exercise.repetitions ?? 12,
    rest_seconds: exercise.rest_seconds ?? 60
  }
}

function toPayloadExercise(exercise: WorkoutFormExercise): WorkoutExercise {
  const { duration_minutes, ...base } = exercise
  if (!isCardioExercise(base)) return base
  return {
    ...base,
    series: 1,
    repetitions: 0,
    load: undefined,
    duration_seconds: Math.max(1, Math.round(Number(duration_minutes) || 0)) * 60
  }
}

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
  const selectedExercises = ref<WorkoutFormExercise[]>([])
  const exerciseSearch = ref('')
  const selectedBodyPart = ref('')
  const pendingDelete = ref<Workout | null>(null)
  const deleting = ref(false)
  const showExerciseForm = ref(false)
  const editingExerciseId = ref<number | null>(null)
  const exerciseName = ref('')
  const exerciseBodyPart = ref('')
  const savingExercise = ref(false)
  const exerciseFormError = ref('')

  const readOnly = computed(() => !canManage.value)
  const bodyParts = computed(() => [...new Set(catalog.value.map(item => item.body_part?.trim()).filter((part): part is string => Boolean(part)))].sort((a, b) => bodyPartLabel(a).localeCompare(bodyPartLabel(b), 'pt-BR')))
  const filteredCatalog = computed(() => {
    const search = exerciseSearch.value.trim().toLocaleLowerCase('pt-BR')
    return catalog.value.filter(exercise => (!selectedBodyPart.value || exercise.body_part === selectedBodyPart.value) && `${exercise.name} ${bodyPartLabel(exercise.body_part)}`.toLocaleLowerCase('pt-BR').includes(search))
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
    selectedExercises.value = (workout?.exercises ?? []).map(toFormExercise)
    formError.value = ''
    exerciseSearch.value = ''
    selectedBodyPart.value = ''
    cancelExerciseForm()
    await ensureCatalog()
  }

  function openExerciseForm(exercise?: Exercise) {
    if (readOnly.value) return
    if (exercise && !exercise.custom) return
    showExerciseForm.value = true
    editingExerciseId.value = exercise?.id ?? null
    exerciseName.value = exercise?.name ?? ''
    exerciseBodyPart.value = exercise?.body_part ?? ''
    exerciseFormError.value = ''
  }

  function cancelExerciseForm() {
    showExerciseForm.value = false
    editingExerciseId.value = null
    exerciseName.value = ''
    exerciseBodyPart.value = ''
    exerciseFormError.value = ''
  }

  async function saveCustomExercise() {
    if (readOnly.value) return
    exerciseFormError.value = ''
    const name = exerciseName.value.trim()
    if (!name) return void (exerciseFormError.value = 'Informe o nome do exercício.')
    if (!exerciseBodyPart.value) return void (exerciseFormError.value = 'Selecione a parte do corpo.')
    savingExercise.value = true
    try {
      const input = { name, bodyPart: exerciseBodyPart.value }
      const saved = editingExerciseId.value
        ? (await updateExercise(request, editingExerciseId.value, input)).data
        : (await createExercise(request, input)).data
      if (!saved) throw new Error('Exercício não retornado pela API.')
      if (editingExerciseId.value) applyExerciseUpdate(saved)
      else {
        catalog.value = [...catalog.value, saved].sort((a, b) => a.name.localeCompare(b.name, 'pt-BR'))
        addExercise(saved)
      }
      selectedBodyPart.value = ''
      exerciseSearch.value = ''
      cancelExerciseForm()
    } catch (cause) {
      exerciseFormError.value = errorMessage(cause, editingExerciseId.value
        ? 'Não foi possível salvar o exercício.'
        : 'Não foi possível criar o exercício.')
    } finally {
      savingExercise.value = false
    }
  }

  function applyExerciseUpdate(exercise: Exercise) {
    catalog.value = catalog.value
      .map(item => item.id === exercise.id ? { ...item, ...exercise } : item)
      .sort((a, b) => a.name.localeCompare(b.name, 'pt-BR'))
    selectedExercises.value = selectedExercises.value.map((item) => {
      if (item.id !== exercise.id) return item
      const base = { ...item, name: exercise.name, body_part: exercise.body_part }
      if (isCardioExercise(exercise)) {
        return {
          ...base,
          series: 1,
          repetitions: 0,
          load: undefined,
          duration_minutes: item.duration_minutes ?? 10
        }
      }
      if (isCardioExercise(item)) {
        return {
          ...base,
          series: 3,
          repetitions: 12,
          load: undefined,
          duration_minutes: undefined
        }
      }
      return base
    })
  }

  function addExercise(exercise: Exercise) {
    if (!selectedExercises.value.some(item => item.id === exercise.id)) selectedExercises.value.push(toFormExercise(exercise))
  }

  function removeExercise(id: number) {
    selectedExercises.value = selectedExercises.value.filter(item => item.id !== id)
  }

  function moveExercise(index: number, offset: number) {
    const target = index + offset
    if (index < 0 || target < 0 || target >= selectedExercises.value.length) return
    const next = [...selectedExercises.value]
    const moved = next[index]
    if (!moved) return
    next.splice(index, 1)
    next.splice(target, 0, moved)
    selectedExercises.value = next
  }

  function cancelForm() {
    showForm.value = false
    editingId.value = null
    workoutName.value = ''
    selectedExercises.value = []
    exerciseSearch.value = ''
    selectedBodyPart.value = ''
    formError.value = ''
    cancelExerciseForm()
  }

  async function save() {
    if (readOnly.value) return
    formError.value = ''
    if (!workoutName.value.trim()) return void (formError.value = 'Informe o nome do treino.')
    if (!selectedExercises.value.length) return void (formError.value = 'Adicione pelo menos um exercicio.')
    if (selectedExercises.value.some(item => isCardioExercise(item) && !(Number(item.duration_minutes) > 0))) {
      return void (formError.value = 'Informe a duracao dos exercicios de cardio.')
    }
    saving.value = true
    try {
      const input = {
        name: workoutName.value.trim(),
        exercises: selectedExercises.value.map(toPayloadExercise),
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
    if (isCardioExercise(exercise) && exercise.duration_seconds) {
      return [`${Math.round(exercise.duration_seconds / 60)} min`, exercise.rest_seconds ? `${exercise.rest_seconds}s descanso` : ''].filter(Boolean).join(' · ')
    }
    return [exercise.series ? `${exercise.series} series` : '', exercise.repetitions ? `${exercise.repetitions} repeticoes` : '', exercise.load ? `${exercise.load} kg` : '', exercise.rest_seconds ? `${exercise.rest_seconds}s descanso` : ''].filter(Boolean).join(' · ') || bodyPartLabel(exercise.body_part) || 'Sem detalhes'
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
    showExerciseForm,
    editingExerciseId,
    exerciseName,
    exerciseBodyPart,
    savingExercise,
    exerciseFormError,
    readOnly,
    bodyParts,
    filteredCatalog,
    load,
    openForm,
    openExerciseForm,
    cancelExerciseForm,
    saveCustomExercise,
    addExercise,
    removeExercise,
    moveExercise,
    cancelForm,
    save,
    requestDelete,
    cancelDelete,
    confirmDelete,
    exerciseDetail,
    isCardioExercise
  }
}
