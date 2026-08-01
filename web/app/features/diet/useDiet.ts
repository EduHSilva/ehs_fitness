import { errorMessage } from '~/types/api'
import { createMeal, fetchMeals, searchFoods } from './diet.api'
import type { Food, Meal, MealFood } from './types'

export function useDiet(studentId: Ref<string>) {
  const { request } = useFitnessApi()
  const meals = ref<Meal[]>([])
  const foods = ref<Food[]>([])
  const loading = ref(true)
  const saving = ref(false)
  const searching = ref(false)
  const error = ref('')
  const formError = ref('')
  const showForm = ref(false)
  const mealName = ref('')
  const mealHour = ref('')
  const foodQuery = ref('')
  const selectedFoods = ref<MealFood[]>([])

  async function load() {
    loading.value = true
    error.value = ''
    try {
      meals.value = (await fetchMeals(request)).data ?? []
    } catch (cause) {
      error.value = errorMessage(cause, 'Nao foi possivel carregar suas refeicoes.')
    } finally {
      loading.value = false
    }
  }

  async function findFoods(query: string) {
    searching.value = true
    try {
      foods.value = (await searchFoods(request, query)).data ?? []
    } catch {
      foods.value = []
    } finally {
      searching.value = false
    }
  }

  let searchTimer: ReturnType<typeof setTimeout>
  watch(foodQuery, (query) => {
    clearTimeout(searchTimer)
    if (query.trim().length < 2) {
      foods.value = []
      return
    }
    searchTimer = setTimeout(() => findFoods(query), 300)
  })

  function addFood(food: Food) {
    if (selectedFoods.value.some(item => item.food_id === food.id)) return
    selectedFoods.value.push({ food_id: food.id, name: food.name, quantity: 100, obs: '' })
    foodQuery.value = ''
    foods.value = []
  }

  function removeFood(id: number) {
    selectedFoods.value = selectedFoods.value.filter(item => item.food_id !== id)
  }

  function cancelForm() {
    showForm.value = false
    mealName.value = ''
    mealHour.value = ''
    selectedFoods.value = []
    foodQuery.value = ''
    formError.value = ''
  }

  async function save() {
    formError.value = ''
    if (!mealName.value.trim()) return void (formError.value = 'Informe o nome da refeicao.')
    if (!mealHour.value) return void (formError.value = 'Informe o horario da refeicao.')
    if (!selectedFoods.value.length) return void (formError.value = 'Adicione pelo menos um alimento.')
    saving.value = true
    try {
      const storedUser = import.meta.client ? localStorage.getItem('user') : null
      const currentUserId = storedUser ? JSON.parse(storedUser)?.id : undefined
      await createMeal(request, { name: mealName.value.trim(), hour: mealHour.value, foods: selectedFoods.value, userId: studentId.value || currentUserId })
      cancelForm()
      await load()
    } catch (cause) {
      formError.value = errorMessage(cause, 'Nao foi possivel salvar a refeicao.')
    } finally {
      saving.value = false
    }
  }

  return { meals, foods, loading, saving, searching, error, formError, showForm, mealName, mealHour, foodQuery, selectedFoods, load, addFood, removeFood, cancelForm, save }
}
