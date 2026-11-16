package com.ehs.fitness.domain.diet

object MealDraft {
    fun formatQuantity(quantity: Int): String = "$quantity g"

    fun itemsLabel(count: Int): String = if (count == 1) "1 item" else "$count itens"
}
