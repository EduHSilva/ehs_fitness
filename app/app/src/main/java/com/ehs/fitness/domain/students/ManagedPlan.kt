package com.ehs.fitness.domain.students

import com.ehs.fitness.data.model.ManagedLink

/** Vínculos ativos do aluno; o nome do profissional substitui o "acesse a web". */
object ManagedPlan {
    const val PERSONAL = "PERSONAL"
    const val NUTRITIONIST = "NUTRITIONIST"

    fun manager(links: List<ManagedLink>, domain: String): String? =
        links.firstOrNull { it.domain.equals(domain, ignoreCase = true) }
            ?.professionalName
            ?.takeIf { it.isNotBlank() }
}
