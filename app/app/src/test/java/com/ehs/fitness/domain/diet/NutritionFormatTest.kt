package com.ehs.fitness.domain.diet

import com.ehs.fitness.data.model.Meal
import com.ehs.fitness.data.model.MealFood
import org.junit.Assert.assertEquals
import org.junit.Test

class NutritionFormatTest {
    private val arroz = MealFood(
        foodId = 1,
        name = "Arroz, integral, cozido",
        quantity = 150,
        calories = 123.53,
        protein = 2.59,
        carbs = 25.81,
        fat = 1.0,
        fiber = 2.75,
    )

    @Test
    fun `escala os macros por 100 g para a quantidade`() {
        assertEquals(185.295, NutritionFormat.scaled(123.53, 150), 0.0001)
        assertEquals("185 kcal · P 3,9 g · C 38,7 g · G 1,5 g", NutritionFormat.macros(arroz))
    }

    @Test
    fun `quantidade zerada nao exibe calorias`() {
        assertEquals(0.0, NutritionFormat.scaled(123.53, 0), 0.0001)
        assertEquals("0 kcal · P 0 g · C 0 g · G 0 g", NutritionFormat.macros(arroz.copy(quantity = 0)))
    }

    @Test
    fun `alimento sem macros fica em branco`() {
        assertEquals("", NutritionFormat.macros(arroz.copy(calories = 0.0, protein = 0.0, carbs = 0.0, fat = 0.0)))
    }

    @Test
    fun `totais somam as quantidades dos alimentos`() {
        val meal = Meal(
            id = 1,
            name = "Café",
            hour = "08:00",
            foods = listOf(
                MealFood(foodId = 1, name = "A", quantity = 100, calories = 100.0, protein = 10.0, carbs = 20.0, fat = 1.0),
                MealFood(foodId = 2, name = "B", quantity = 100, calories = 50.0, protein = 5.0, carbs = 0.0, fat = 2.0),
            ),
        )

        assertEquals("150 kcal · P 15 g · C 20 g · G 3 g", NutritionFormat.totals(meal))
    }

    @Test
    fun `refeicao sem macros fica em branco`() {
        val meal = Meal(id = 2, name = "Vazia", hour = "12:00", foods = emptyList())
        assertEquals("", NutritionFormat.totals(meal))
    }
}
