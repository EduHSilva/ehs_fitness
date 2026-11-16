package com.ehs.fitness.domain.diet

import org.junit.Assert.assertEquals
import org.junit.Test

class MealDraftTest {
    @Test
    fun `formata quantidade e itens`() {
        assertEquals("120 g", MealDraft.formatQuantity(120))
        assertEquals("1 item", MealDraft.itemsLabel(1))
        assertEquals("3 itens", MealDraft.itemsLabel(3))
    }
}
