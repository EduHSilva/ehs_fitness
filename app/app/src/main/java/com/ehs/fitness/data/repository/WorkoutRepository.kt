package com.ehs.fitness.data.repository

import com.ehs.fitness.core.network.unwrapList
import com.ehs.fitness.data.mappers.Mappers
import com.ehs.fitness.data.model.WeekDay
import com.ehs.fitness.data.model.Workout
import com.ehs.fitness.data.remote.WorkoutApi

class WorkoutRepository(private val api: WorkoutApi) {
    suspend fun workouts(): List<Workout> =
        api.list().unwrapList().mapNotNull(Mappers::workout)

    suspend fun week(): List<WeekDay> =
        api.week().unwrapList().map(Mappers::weekDay)
}
