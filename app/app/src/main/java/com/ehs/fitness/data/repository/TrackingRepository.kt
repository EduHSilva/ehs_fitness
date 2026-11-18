package com.ehs.fitness.data.repository

import com.ehs.fitness.core.network.unwrap
import com.ehs.fitness.data.mappers.Mappers
import com.ehs.fitness.data.model.DailyTracking
import com.ehs.fitness.data.model.TrackingConfig
import com.ehs.fitness.data.remote.TrackingApi
import com.ehs.fitness.data.remote.UpdateHistoryRequest
import retrofit2.HttpException

class TrackingRepository(private val api: TrackingApi) {
    suspend fun config(): TrackingConfig =
        Mappers.config(api.config().unwrap())

    /** O backend responde 404 enquanto não há registro no dia; isso vale como zero. */
    suspend fun today(): DailyTracking = try {
        Mappers.dailyTracking(api.today().unwrap())
    } catch (e: HttpException) {
        if (e.code() == 404) DailyTracking() else throw e
    }

    suspend fun register(tracking: DailyTracking): DailyTracking =
        Mappers.dailyTracking(
            api.updateToday(
                UpdateHistoryRequest(
                    creatine = tracking.creatine,
                    cardio = tracking.cardio,
                    trained = tracking.trained,
                ),
            ).unwrap(),
        )
}
