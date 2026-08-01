import { errorMessage } from '~/types/api'
import { createHabit, deleteHabit, fetchHabits } from './habit.api'
import type { Habit } from './types'

export const habitCategories = ['Saude', 'Alimentacao', 'Atividade fisica', 'Sono', 'Bem-estar', 'Outro']

export function useHabits(studentId: Ref<string>) {
  const { request } = useFitnessApi()
  const habits = ref<Habit[]>([])
  const name = ref('')
  const category = ref('')
  const error = ref('')
  async function load() {
    error.value = ''
    try {
      habits.value = (await fetchHabits(request, studentId.value || undefined)).data ?? []
    } catch (cause) {
      error.value = errorMessage(cause, 'Nao foi possivel carregar os habitos.')
    }
  }
  async function add() {
    error.value = ''
    if (!name.value.trim() || !category.value) return void (error.value = 'Informe o nome e a categoria do habito.')
    try {
      const habit = (await createHabit(request, { name: name.value.trim(), category: category.value }, studentId.value || undefined)).data
      if (habit) habits.value.push(habit)
      name.value = ''
      category.value = ''
    } catch (cause) {
      error.value = errorMessage(cause, 'Nao foi possivel adicionar o habito.')
    }
  }
  async function remove(id: string) {
    error.value = ''
    try {
      await deleteHabit(request, id)
      habits.value = habits.value.filter(habit => habit.id !== id)
    } catch (cause) {
      error.value = errorMessage(cause, 'Nao foi possivel remover o habito.')
    }
  }
  return { habits, name, category, error, categories: habitCategories, load, add, remove }
}
