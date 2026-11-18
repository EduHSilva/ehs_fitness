import { errorMessage } from '~/types/api'
import { createMeal, deleteMeal, fetchMeals, searchFoods, updateMeal } from './diet.api'
import type { Food, Macros, Meal, MealFood, MealFoodResponse } from './types'

function hasMacros(food: Macros) {
  return [food.calories, food.protein, food.carbs, food.fat]
    .some(value => typeof value === 'number' && value > 0)
}

function scaledMacros(food: Macros, quantity: number): Required<Macros> {
  const factor = quantity > 0 ? quantity / 100 : 0
  return {
    calories: (food.calories ?? 0) * factor,
    protein: (food.protein ?? 0) * factor,
    carbs: (food.carbs ?? 0) * factor,
    fat: (food.fat ?? 0) * factor,
    fiber: (food.fiber ?? 0) * factor
  }
}

function decimalLabel(value: number) {
  return (Math.round(value * 10) / 10).toLocaleString('pt-BR', { maximumFractionDigits: 1 })
}

function macrosLabel(macros: Required<Macros>) {
  return `${Math.round(macros.calories)} kcal · P ${decimalLabel(macros.protein)} g · C ${decimalLabel(macros.carbs)} g · G ${decimalLabel(macros.fat)} g`
}

function foodMacros(food: Macros, quantity: number) {
  return hasMacros(food) ? macrosLabel(scaledMacros(food, quantity)) : ''
}

function mealMacros(meal: Meal) {
  const foods = (meal.foods ?? []).filter(hasMacros)
  if (!foods.length) return ''
  const total = foods.reduce<Required<Macros>>((sum, food) => {
    const scaled = scaledMacros(food, Number(food.quantity) || 0)
    return {
      calories: sum.calories + scaled.calories,
      protein: sum.protein + scaled.protein,
      carbs: sum.carbs + scaled.carbs,
      fat: sum.fat + scaled.fat,
      fiber: sum.fiber + scaled.fiber
    }
  }, { calories: 0, protein: 0, carbs: 0, fat: 0, fiber: 0 })
  return macrosLabel(total)
}

export function useDiet(studentId: Ref<string>, canManage: Ref<boolean>) {
  const { request } = useFitnessApi()
  const meals = ref<Meal[]>([])
  const foods = ref<Food[]>([])
  const loading = ref(true)
  const saving = ref(false)
  const searching = ref(false)
  const error = ref('')
  const actionError = ref('')
  const formError = ref('')
  const showForm = ref(false)
  const editingId = ref<number | null>(null)
  const mealName = ref('')
  const mealHour = ref('')
  const foodQuery = ref('')
  const selectedFoods = ref<MealFood[]>([])
  const pendingDelete = ref<Meal | null>(null)
  const deleting = ref(false)

  const readOnly = computed(() => !canManage.value)

  async function load() {
    loading.value = true
    error.value = ''
    try {
      meals.value = (await fetchMeals(request, studentId.value || undefined)).data ?? []
    } catch (cause) {
      error.value = errorMessage(cause, 'Nao foi possivel carregar as refeicoes.')
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
    selectedFoods.value.push({
      food_id: food.id,
      name: food.name,
      quantity: 100,
      observation: '',
      calories: food.calories,
      protein: food.protein,
      carbs: food.carbs,
      fat: food.fat,
      fiber: food.fiber
    })
    foodQuery.value = ''
    foods.value = []
  }

  function removeFood(id: number) {
    selectedFoods.value = selectedFoods.value.filter(item => item.food_id !== id)
  }

  function cancelForm() {
    showForm.value = false
    editingId.value = null
    mealName.value = ''
    mealHour.value = ''
    selectedFoods.value = []
    foodQuery.value = ''
    formError.value = ''
  }

  function foodId(food: MealFoodResponse): number {
    return food.food_id ?? food.tag_id ?? 0
  }

  function foodName(food: MealFoodResponse): string {
    return food.food_name || food.food?.name || food.name || 'Alimento'
  }

  function startEdit(meal: Meal) {
    if (readOnly.value) return
    actionError.value = ''
    const foodsOfMeal = meal.foods ?? []
    const missingId = foodsOfMeal.some(food => foodId(food) === 0)
    if (missingId) {
      actionError.value = 'Nao foi possivel abrir a refeicao para edicao: a API nao devolveu o alimento.'
      return
    }
    editingId.value = meal.id
    mealName.value = meal.name
    mealHour.value = meal.hour ?? ''
    selectedFoods.value = foodsOfMeal.map(food => ({
      food_id: foodId(food),
      name: foodName(food),
      quantity: Number(food.quantity) || 0,
      observation: food.observation ?? '',
      calories: food.calories,
      protein: food.protein,
      carbs: food.carbs,
      fat: food.fat,
      fiber: food.fiber
    }))
    showForm.value = true
    formError.value = ''
  }

  async function save() {
    if (readOnly.value) return
    formError.value = ''
    if (!mealName.value.trim()) return void (formError.value = 'Informe o nome da refeicao.')
    if (!mealHour.value) return void (formError.value = 'Informe o horario da refeicao.')
    if (!selectedFoods.value.length) return void (formError.value = 'Adicione pelo menos um alimento.')
    saving.value = true
    try {
      const input = {
        name: mealName.value.trim(),
        hour: mealHour.value,
        foods: selectedFoods.value,
        studentId: studentId.value || undefined
      }
      if (editingId.value) await updateMeal(request, editingId.value, input)
      else await createMeal(request, input)
      cancelForm()
      await load()
    } catch (cause) {
      formError.value = errorMessage(cause, 'Nao foi possivel salvar a refeicao.')
    } finally {
      saving.value = false
    }
  }

  function requestDelete(meal: Meal) {
    if (readOnly.value) return
    actionError.value = ''
    pendingDelete.value = meal
  }

  function cancelDelete() {
    pendingDelete.value = null
  }

  async function confirmDelete() {
    const meal = pendingDelete.value
    if (!meal || deleting.value || readOnly.value) return
    deleting.value = true
    actionError.value = ''
    try {
      await deleteMeal(request, meal.id, studentId.value || undefined)
      pendingDelete.value = null
      await load()
    } catch (cause) {
      actionError.value = errorMessage(cause, 'Nao foi possivel excluir a refeicao.')
    } finally {
      deleting.value = false
    }
  }

  return {
    meals,
    foods,
    loading,
    saving,
    searching,
    error,
    actionError,
    formError,
    showForm,
    editingId,
    mealName,
    mealHour,
    foodQuery,
    selectedFoods,
    pendingDelete,
    deleting,
    readOnly,
    load,
    addFood,
    removeFood,
    cancelForm,
    startEdit,
    save,
    requestDelete,
    cancelDelete,
    confirmDelete,
    foodName,
    foodMacros,
    mealMacros,
    hasMacros
  }
}
