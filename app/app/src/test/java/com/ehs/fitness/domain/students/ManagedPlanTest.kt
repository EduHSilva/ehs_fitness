package com.ehs.fitness.domain.students

import com.ehs.fitness.data.model.ManagedLink
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ManagedPlanTest {
    private val links = listOf(
        ManagedLink(domain = "PERSONAL", professionalName = "Ana Personal"),
        ManagedLink(domain = "NUTRITIONIST", professionalName = "Bruno Nutri"),
    )

    @Test
    fun `encontra o profissional do dominio`() {
        assertEquals("Ana Personal", ManagedPlan.manager(links, ManagedPlan.PERSONAL))
        assertEquals("Bruno Nutri", ManagedPlan.manager(links, ManagedPlan.NUTRITIONIST))
    }

    @Test
    fun `sem vinculo no dominio nao ha gerenciador`() {
        assertNull(ManagedPlan.manager(listOf(links[0]), ManagedPlan.NUTRITIONIST))
        assertNull(ManagedPlan.manager(emptyList(), ManagedPlan.PERSONAL))
    }

    @Test
    fun `nome em branco nao conta como gerenciador`() {
        assertNull(ManagedPlan.manager(listOf(ManagedLink("PERSONAL", "  ")), ManagedPlan.PERSONAL))
    }
}
