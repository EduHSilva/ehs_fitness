package com.ehs.fitness.di

import android.content.Context
import com.ehs.fitness.BuildConfig
import com.ehs.fitness.core.network.ApiClient
import com.ehs.fitness.core.prefs.AppPreferences
import com.ehs.fitness.core.session.SessionManager
import com.ehs.fitness.core.settings.AppSettings
import com.ehs.fitness.data.remote.AuthApi
import com.ehs.fitness.data.remote.DietApi
import com.ehs.fitness.data.remote.TrackingApi
import com.ehs.fitness.data.remote.WorkoutApi
import com.ehs.fitness.data.repository.AuthRepository
import com.ehs.fitness.data.repository.DietRepository
import com.ehs.fitness.data.repository.TrackingRepository
import com.ehs.fitness.data.repository.TrainingSessionRepository
import com.ehs.fitness.data.repository.WorkoutRepository
import kotlinx.serialization.json.Json

/** Injeção manual (sem framework): a aplicação cria e expõe as dependências. */
class AppContainer(context: Context) {
    private val prefs = AppPreferences(context)
    private val json = Json { ignoreUnknownKeys = true }

    val settings = AppSettings(prefs)
    val sessionManager = SessionManager(
        prefs = prefs,
        json = json,
    )

    private val apiClient = ApiClient(sessionManager)
    private val retrofit = apiClient.retrofit

    val authRepository = AuthRepository(retrofit.create(AuthApi::class.java), sessionManager)
    val workoutRepository = WorkoutRepository(retrofit.create(WorkoutApi::class.java))
    val dietRepository = DietRepository(retrofit.create(DietApi::class.java))
    val trackingRepository = TrackingRepository(retrofit.create(TrackingApi::class.java))
    val trainingRepository = TrainingSessionRepository(prefs, json)

    val googleWebClientId: String = BuildConfig.GOOGLE_WEB_CLIENT_ID
    val googleLoginEnabled: Boolean get() = googleWebClientId.isNotBlank()
    val appVersion: String = BuildConfig.VERSION_NAME
}
