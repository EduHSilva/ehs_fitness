package com.ehs.fitness.data.repository

import com.ehs.fitness.core.network.unwrapList
import com.ehs.fitness.data.mappers.Mappers
import com.ehs.fitness.data.model.Meal
import com.ehs.fitness.data.remote.DietApi

class DietRepository(private val api: DietApi) {
    suspend fun meals(): List<Meal> =
        api.meals().unwrapList().mapNotNull(Mappers::meal)
}
