export type Food = { id: number, name: string }
export type MealFood = { food_id: number, name: string, quantity: number, observation: string }
export type MealFoodResponse = {
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
