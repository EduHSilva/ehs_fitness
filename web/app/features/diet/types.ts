export type Food = { id: number, name: string }
export type MealFood = { food_id: number, name: string, quantity: number, obs: string }
export type Meal = {
  id: number
  name: string
  hour?: string
  foods?: { food?: { name?: string }, name?: string, quantity?: number | string, observation?: string, obs?: string }[]
}
