import { describe, expect, it } from 'vitest'
import { dayMacros, dayTotal, foodMacros, hasMacros, mealMacros, scaledMacros } from '~/features/diet/macros'
import type { Meal } from '~/features/diet/types'

const chicken = { calories: 165, protein: 31, carbs: 0, fat: 3.6 }
const rice = { calories: 130, protein: 2.7, carbs: 28, fat: 0.3 }

describe('macros da dieta', () => {
  it('escala os valores por 100 g', () => {
    expect(scaledMacros(chicken, 200)).toMatchObject({ calories: 330, protein: 62, fat: 7.2 })
    expect(scaledMacros(chicken, 0).calories).toBe(0)
  })

  it('detecta alimento sem macros', () => {
    expect(hasMacros({})).toBe(false)
    expect(hasMacros({ calories: 0, protein: 0 })).toBe(false)
    expect(hasMacros({ fiber: 5 })).toBe(false)
    expect(hasMacros(chicken)).toBe(true)
    expect(foodMacros({}, 100)).toBe('')
  })

  it('soma a refeição considerando a quantidade', () => {
    const meal: Meal = {
      id: 1,
      name: 'Almoço',
      foods: [
        { food_id: 1, quantity: 100, ...chicken },
        { food_id: 2, quantity: 200, ...rice }
      ]
    }
    expect(mealMacros(meal)).toContain('425 kcal')
    expect(mealMacros({ id: 2, name: 'Vazia' })).toBe('')
  })

  it('totaliza o dia inteiro', () => {
    const meals: Meal[] = [
      { id: 1, name: 'Almoço', foods: [{ food_id: 1, quantity: 100, ...chicken }] },
      { id: 2, name: 'Jantar', foods: [{ food_id: 2, quantity: 100, ...rice }] }
    ]
    expect(dayTotal(meals).calories).toBe(295)
    expect(dayMacros(meals)).toContain('295 kcal')
    expect(dayMacros([])).toBe('')
  })
})
