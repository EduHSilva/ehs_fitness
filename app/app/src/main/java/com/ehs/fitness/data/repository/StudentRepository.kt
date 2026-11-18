package com.ehs.fitness.data.repository

import com.ehs.fitness.core.network.unwrapList
import com.ehs.fitness.data.mappers.Mappers
import com.ehs.fitness.data.model.ManagedLink
import com.ehs.fitness.data.remote.StudentApi

/** Vínculos ativos do aluno com os profissionais que gerenciam o plano. */
class StudentRepository(private val api: StudentApi) {
    suspend fun links(): List<ManagedLink> =
        api.links().unwrapList().mapNotNull(Mappers::managedLink)
}
