import type { Macros, Meal } from './types'

export function hasMacros(food: Macros) {
  return [food.calories, food.protein, food.carbs, food.fat]
    .some(value => typeof value === 'number' && value > 0)
}

export function scaledMacros(food: Macros, quantity: number): Required<Macros> {
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

export function macrosLabel(macros: Required<Macros>) {
  return `${Math.round(macros.calories)} kcal · P ${decimalLabel(macros.protein)} g · C ${decimalLabel(macros.carbs)} g · G ${decimalLabel(macros.fat)} g`
}

export function foodMacros(food: Macros, quantity: number) {
  return hasMacros(food) ? macrosLabel(scaledMacros(food, quantity)) : ''
}

export function emptyMacros(): Required<Macros> {
  return { calories: 0, protein: 0, carbs: 0, fat: 0, fiber: 0 }
}

export function addMacros(total: Required<Macros>, food: Macros, quantity: number): Required<Macros> {
  const scaled = scaledMacros(food, quantity)
  return {
    calories: total.calories + scaled.calories,
    protein: total.protein + scaled.protein,
    carbs: total.carbs + scaled.carbs,
    fat: total.fat + scaled.fat,
    fiber: total.fiber + scaled.fiber
  }
}

export function mealTotal(meal: Meal): Required<Macros> {
  return (meal.foods ?? []).filter(hasMacros).reduce<Required<Macros>>(
    (sum, food) => addMacros(sum, food, Number(food.quantity) || 0),
    emptyMacros()
  )
}

export function mealMacros(meal: Meal) {
  if (!(meal.foods ?? []).some(hasMacros)) return ''
  return macrosLabel(mealTotal(meal))
}

export function dayTotal(meals: Meal[]): Required<Macros> {
  return meals.reduce<Required<Macros>>(
    (sum, meal) => {
      const total = mealTotal(meal)
      return {
        calories: sum.calories + total.calories,
        protein: sum.protein + total.protein,
        carbs: sum.carbs + total.carbs,
        fat: sum.fat + total.fat,
        fiber: sum.fiber + total.fiber
      }
    },
    emptyMacros()
  )
}

export function dayMacros(meals: Meal[]) {
  return meals.some(meal => (meal.foods ?? []).some(hasMacros))
    ? macrosLabel(dayTotal(meals))
    : ''
}
