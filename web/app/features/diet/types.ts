export type Macros = {
  calories?: number
  protein?: number
  carbs?: number
  fat?: number
  fiber?: number
}
export type Portion = {
  portion_label?: string
  portion_grams?: number
}
export type Food = Macros & Portion & { id: number, name: string }
export type MealFood = Macros & Portion & { food_id: number, name: string, quantity: number, observation: string }
export type MealFoodResponse = Macros & Portion & {
  tag_id?: number
  food_id?: number
  food_name?: string
  food?: { name?: string }
  name?: string
  quantity?: number | string
  observation?: string
}
export type Meal = {
  id: number
  name: string
  hour?: string
  foods?: MealFoodResponse[]
}
