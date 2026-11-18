package com.ehs.fitness.domain.diet

import com.ehs.fitness.data.model.Meal
import com.ehs.fitness.data.model.MealFood
import java.util.Locale
import kotlin.math.round
import kotlin.math.roundToInt

/** Formata os macros do catálogo (por 100 g) para a quantidade consumida. */
object NutritionFormat {
    fun scaled(value: Double, quantity: Int): Double =
        if (quantity <= 0) 0.0 else value * quantity / 100.0

    fun macros(food: MealFood): String =
        macros(food.calories, food.protein, food.carbs, food.fat, food.quantity)

    fun macros(calories: Double, protein: Double, carbs: Double, fat: Double, quantity: Int): String {
        if (calories <= 0.0 && protein <= 0.0 && carbs <= 0.0 && fat <= 0.0) return ""
        val kcal = scaled(calories, quantity).roundToInt()
        return "$kcal kcal · P ${decimal(scaled(protein, quantity))} g · " +
            "C ${decimal(scaled(carbs, quantity))} g · G ${decimal(scaled(fat, quantity))} g"
    }

    fun totals(meal: Meal): String {
        val foods = meal.foods.filter { it.hasMacros }
        if (foods.isEmpty()) return ""
        val calories = foods.sumOf { scaled(it.calories, it.quantity) }
        val protein = foods.sumOf { scaled(it.protein, it.quantity) }
        val carbs = foods.sumOf { scaled(it.carbs, it.quantity) }
        val fat = foods.sumOf { scaled(it.fat, it.quantity) }
        return macros(calories, protein, carbs, fat, quantity = 100)
    }

    private val MealFood.hasMacros: Boolean
        get() = calories > 0.0 || protein > 0.0 || carbs > 0.0 || fat > 0.0

    private fun decimal(value: Double): String {
        val rounded = round(value * 10) / 10
        return if (rounded % 1.0 == 0.0) {
            rounded.toInt().toString()
        } else {
            String.format(Locale.forLanguageTag("pt-BR"), "%.1f", rounded)
        }
    }
}
