import { errorMessage } from '~/types/api'
import { fetchExerciseCatalog, fetchWorkouts, createWorkout } from './workout.api'
import type { Exercise, Workout, WorkoutExercise } from './types'

export function useWorkouts(studentId: Ref<string>) {
  const { request } = useFitnessApi()
  const workouts = ref<Workout[]>([])
  const catalog = ref<Exercise[]>([])
  const loading = ref(true)
  const saving = ref(false)
  const error = ref('')
  const formError = ref('')
  const showForm = ref(false)
  const expandedWorkoutId = ref<number | null>(null)
  const workoutName = ref('')
  const selectedExercises = ref<WorkoutExercise[]>([])
  const exerciseSearch = ref('')
  const selectedBodyPart = ref('')

  const bodyParts = computed(() => [...new Set(catalog.value.map(item => item.body_part?.trim()).filter((part): part is string => Boolean(part)))].sort((a, b) => a.localeCompare(b, 'pt-BR')))
  const filteredCatalog = computed(() => {
    const search = exerciseSearch.value.trim().toLocaleLowerCase('pt-BR')
    return catalog.value.filter(exercise => (!selectedBodyPart.value || exercise.body_part === selectedBodyPart.value) && `${exercise.name} ${exercise.body_part ?? ''}`.toLocaleLowerCase('pt-BR').includes(search))
  })

  async function load() {
    loading.value = true
    error.value = ''
    try {
      workouts.value = (await fetchWorkouts(request)).data ?? []
    } catch (cause) {
      error.value = errorMessage(cause, 'Nao foi possivel carregar seus treinos.')
    } finally {
      loading.value = false
    }
  }
  async function openForm() {
    showForm.value = true
    formError.value = ''
    exerciseSearch.value = ''
    selectedBodyPart.value = ''
    if (catalog.value.length) return
    try {
      catalog.value = (await fetchExerciseCatalog(request)).data ?? []
    } catch {
      formError.value = 'Nao foi possivel carregar os exercicios.'
    }
  }
  function addExercise(exercise: Exercise) {
    if (!selectedExercises.value.some(item => item.id === exercise.id)) selectedExercises.value.push({ ...exercise, series: 3, repetitions: 12 })
  }
  function removeExercise(id: number) {
    selectedExercises.value = selectedExercises.value.filter(item => item.id !== id)
  }
  function cancelForm() {
    showForm.value = false
    workoutName.value = ''
    selectedExercises.value = []
    exerciseSearch.value = ''
    selectedBodyPart.value = ''
    formError.value = ''
  }
  async function save() {
    formError.value = ''
    if (!workoutName.value.trim()) return void (formError.value = 'Informe o nome do treino.')
    if (!selectedExercises.value.length) return void (formError.value = 'Adicione pelo menos um exercicio.')
    saving.value = true
    try {
      await createWorkout(request, { name: workoutName.value.trim(), exercises: selectedExercises.value, studentId: studentId.value || undefined })
      cancelForm()
      await load()
    } catch (cause) {
      formError.value = errorMessage(cause, 'Nao foi possivel salvar o treino.')
    } finally {
      saving.value = false
    }
  }
  function exerciseDetail(exercise: Exercise) {
    return [exercise.series ? `${exercise.series} series` : '', exercise.repetitions ? `${exercise.repetitions} repeticoes` : '', exercise.load ? `${exercise.load} kg` : ''].filter(Boolean).join(' · ') || exercise.body_part || 'Sem detalhes'
  }

  return { workouts, catalog, loading, saving, error, formError, showForm, expandedWorkoutId, workoutName, selectedExercises, exerciseSearch, selectedBodyPart, bodyParts, filteredCatalog, load, openForm, addExercise, removeExercise, cancelForm, save, exerciseDetail }
}
